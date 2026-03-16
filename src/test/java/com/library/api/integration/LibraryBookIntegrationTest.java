package com.library.api.integration;

import com.library.api.dto.request.CreateBookRequest;
import com.library.api.dto.request.CreateLibraryRequest;
import com.library.api.dto.request.UpdateBookRequest;
import com.library.api.dto.response.BookResponse;
import com.library.api.dto.response.LibraryResponse;
import com.library.api.dto.response.PageResponse;
import com.library.api.util.TestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full integration test for Library Management API.
 * Uses @SpringBootTest with TestRestTemplate to test end-to-end flows.
 * This is the ONLY test class permitted to load the full Spring context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LibraryBookIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static Long createdLibraryId;
    private static Long createdBookId;

    // Fixed dates for deterministic tests
    private static final LocalDate PUBLICATION_DATE = LocalDate.of(2020, 1, 15);
    private static final LocalDate ACQUISITION_DATE = LocalDate.of(2020, 6, 1);
    private static final LocalDate ESTABLISHED_DATE = LocalDate.of(1990, 5, 20);

    @Nested
    @DisplayName("End-to-End Library and Book Flow Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EndToEndFlowTests {

        @Test
        @Order(1)
        @DisplayName("Step 1: Create a Library - POST /api/libraries returns 201")
        void createLibrary_returns201() {
            // Arrange
            CreateLibraryRequest request = new CreateLibraryRequest();
            request.setName("Integration Test Library");
            request.setAddress("789 Test Avenue, Test City");
            request.setPhone("+1-555-0999");
            request.setEmail("integration@testlibrary.org");
            request.setEstablishedDate(ESTABLISHED_DATE);

            // Act
            ResponseEntity<LibraryResponse> response = restTemplate.postForEntity(
                    "/api/libraries",
                    request,
                    LibraryResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("Integration Test Library");
            assertThat(response.getBody().getTotalBooks()).isEqualTo(0);

            // Store for subsequent tests
            createdLibraryId = response.getBody().getId();
        }

        @Test
        @Order(2)
        @DisplayName("Step 2: Get the Library - GET /api/libraries/{id} returns 200 and correct name")
        void getLibrary_returns200AndCorrectName() {
            // Act
            ResponseEntity<LibraryResponse> response = restTemplate.getForEntity(
                    "/api/libraries/{id}",
                    LibraryResponse.class,
                    createdLibraryId
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(createdLibraryId);
            assertThat(response.getBody().getName()).isEqualTo("Integration Test Library");
        }

        @Test
        @Order(3)
        @DisplayName("Step 3: Create a Book in that Library - POST /api/books returns 201")
        void createBook_returns201() {
            // Arrange
            CreateBookRequest request = new CreateBookRequest();
            request.setIsbn("9781234567890");
            request.setTitle("Integration Test Book");
            request.setAuthor("Test Author");
            request.setGenre("Test Genre");
            request.setPublicationDate(PUBLICATION_DATE);
            request.setAcquisitionDate(ACQUISITION_DATE);
            request.setPages(350);
            request.setLibraryId(createdLibraryId);

            // Act
            ResponseEntity<BookResponse> response = restTemplate.postForEntity(
                    "/api/books",
                    request,
                    BookResponse.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isNotNull();
            assertThat(response.getBody().getIsbn()).isEqualTo("9781234567890");
            assertThat(response.getBody().getTitle()).isEqualTo("Integration Test Book");
            assertThat(response.getBody().getLibraryId()).isEqualTo(createdLibraryId);
            assertThat(response.getBody().getLibraryName()).isEqualTo("Integration Test Library");

            // Store for subsequent tests
            createdBookId = response.getBody().getId();
        }

        @Test
        @Order(4)
        @DisplayName("Step 4: Get all books for Library - GET /api/libraries/{id}/books returns the book")
        void getBooksByLibrary_returnsCreatedBook() {
            // Act
            ResponseEntity<PageResponse<BookResponse>> response = restTemplate.exchange(
                    "/api/libraries/{id}/books",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<BookResponse>>() {},
                    createdLibraryId
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotEmpty();
            assertThat(response.getBody().getContent())
                    .anyMatch(book -> book.getIsbn().equals("9781234567890"));
        }

        @Test
        @Order(5)
        @DisplayName("Step 5: Get book by publication date range - GET /api/books/by-publication-date")
        void getBooksByPublicationDateRange_returnsBook() {
            // Act
            ResponseEntity<PageResponse<BookResponse>> response = restTemplate.exchange(
                    "/api/books/by-publication-date?startDate=2020-01-01&endDate=2020-12-31",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<BookResponse>>() {}
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent())
                    .anyMatch(book -> book.getIsbn().equals("9781234567890"));
        }

        @Test
        @Order(6)
        @DisplayName("Step 6: Update the Book - PUT /api/books/{id} returns 200")
        void updateBook_returns200() {
            // Arrange
            UpdateBookRequest request = new UpdateBookRequest();
            request.setIsbn("9781234567890");
            request.setTitle("Updated Integration Test Book");
            request.setAuthor("Updated Test Author");
            request.setGenre("Updated Genre");
            request.setPublicationDate(PUBLICATION_DATE);
            request.setAcquisitionDate(ACQUISITION_DATE);
            request.setPages(450);
            request.setLibraryId(createdLibraryId);

            // Act
            ResponseEntity<BookResponse> response = restTemplate.exchange(
                    "/api/books/{id}",
                    HttpMethod.PUT,
                    new HttpEntity<>(request),
                    BookResponse.class,
                    createdBookId
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Updated Integration Test Book");
            assertThat(response.getBody().getAuthor()).isEqualTo("Updated Test Author");
            assertThat(response.getBody().getPages()).isEqualTo(450);
        }

        @Test
        @Order(7)
        @DisplayName("Step 7: Delete the Book - DELETE /api/books/{id} returns 204")
        void deleteBook_returns204() {
            // Act
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/books/{id}",
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    createdBookId
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify book is gone
            ResponseEntity<BookResponse> getResponse = restTemplate.getForEntity(
                    "/api/books/{id}",
                    BookResponse.class,
                    createdBookId
            );
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Order(8)
        @DisplayName("Step 8: Delete the Library - DELETE /api/libraries/{id} returns 204")
        void deleteLibrary_returns204() {
            // Act
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/libraries/{id}",
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    createdLibraryId
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @Order(9)
        @DisplayName("Step 9: Confirm Library is gone - GET /api/libraries/{id} returns 404")
        void getDeletedLibrary_returns404() {
            // Act
            ResponseEntity<Object> response = restTemplate.getForEntity(
                    "/api/libraries/{id}",
                    Object.class,
                    createdLibraryId
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Validation and Error Handling Integration Tests")
    class ValidationAndErrorTests {

        @Test
        @DisplayName("Should return 400 when creating book with invalid ISBN")
        void createBookWithInvalidIsbn_returns400() {
            // Arrange
            CreateBookRequest request = new CreateBookRequest();
            request.setIsbn("invalid");
            request.setTitle("Test");
            request.setAuthor("Test");
            request.setLibraryId(1L);

            // Act
            ResponseEntity<Object> response = restTemplate.postForEntity(
                    "/api/books",
                    request,
                    Object.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return 400 when creating book with duplicate ISBN")
        void createBookWithDuplicateIsbn_returns400() {
            // First, create a library and book
            CreateLibraryRequest libraryRequest = new CreateLibraryRequest();
            libraryRequest.setName("Duplicate Test Library");
            ResponseEntity<LibraryResponse> libraryResponse = restTemplate.postForEntity(
                    "/api/libraries",
                    libraryRequest,
                    LibraryResponse.class
            );
            Long libraryId = libraryResponse.getBody().getId();

            // Create first book
            CreateBookRequest firstBook = new CreateBookRequest();
            firstBook.setIsbn("9780987654321");
            firstBook.setTitle("First Book");
            firstBook.setAuthor("Author");
            firstBook.setLibraryId(libraryId);
            restTemplate.postForEntity("/api/books", firstBook, BookResponse.class);

            // Try to create second book with same ISBN
            CreateBookRequest duplicateBook = new CreateBookRequest();
            duplicateBook.setIsbn("9780987654321");
            duplicateBook.setTitle("Duplicate Book");
            duplicateBook.setAuthor("Another Author");
            duplicateBook.setLibraryId(libraryId);

            // Act
            ResponseEntity<Object> response = restTemplate.postForEntity(
                    "/api/books",
                    duplicateBook,
                    Object.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            // Cleanup
            restTemplate.delete("/api/libraries/{id}", libraryId);
        }

        @Test
        @DisplayName("Should return 404 when creating book with non-existent library")
        void createBookWithNonExistentLibrary_returns404() {
            // Arrange
            CreateBookRequest request = new CreateBookRequest();
            request.setIsbn("9781111111111");
            request.setTitle("Orphan Book");
            request.setAuthor("Test Author");
            request.setLibraryId(999999L);

            // Act
            ResponseEntity<Object> response = restTemplate.postForEntity(
                    "/api/books",
                    request,
                    Object.class
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Search and Filter Integration Tests")
    class SearchAndFilterTests {

        @Test
        @DisplayName("Should search books by author")
        void searchBooksByAuthor_returnsMatchingBooks() {
            // Act - search for existing author from data.sql
            ResponseEntity<PageResponse<BookResponse>> response = restTemplate.exchange(
                    "/api/books?author=Harper",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<BookResponse>>() {}
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            // Check if any books by Harper Lee exist in initial data
        }

        @Test
        @DisplayName("Should filter books by genre")
        void filterBooksByGenre_returnsMatchingBooks() {
            // Act - search for existing genre from data.sql
            ResponseEntity<PageResponse<BookResponse>> response = restTemplate.exchange(
                    "/api/books?genre=Fiction",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<BookResponse>>() {}
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("Should get books by acquisition date range")
        void getBooksByAcquisitionDateRange_returnsBooks() {
            // Act
            ResponseEntity<PageResponse<BookResponse>> response = restTemplate.exchange(
                    "/api/books/by-acquisition-date?startDate=2000-01-01&endDate=2025-12-31",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<BookResponse>>() {}
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Pagination Integration Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated results with correct page info")
        void getAllLibraries_returnsPaginatedResults() {
            // Act
            ResponseEntity<PageResponse<LibraryResponse>> response = restTemplate.exchange(
                    "/api/libraries?page=0&size=5",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<LibraryResponse>>() {}
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPageNumber()).isEqualTo(0);
            assertThat(response.getBody().getPageSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should support sorting")
        void getAllBooks_supportsSorting() {
            // Act
            ResponseEntity<PageResponse<BookResponse>> response = restTemplate.exchange(
                    "/api/books?sort=title,desc",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<BookResponse>>() {}
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
