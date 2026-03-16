package com.library.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.api.dto.request.CreateBookRequest;
import com.library.api.dto.request.UpdateBookRequest;
import com.library.api.dto.response.BookResponse;
import com.library.api.dto.response.PageResponse;
import com.library.api.exception.BadRequestException;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.service.BookService;
import com.library.api.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for BookController.
 * Uses @WebMvcTest — loads only web slice, mocks service layer.
 */
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private BookResponse testBookResponse;
    private PageResponse<BookResponse> testPageResponse;

    @BeforeEach
    void setUp() {
        testBookResponse = TestDataFactory.createBookResponse(1L, "9780000000001");
        testPageResponse = TestDataFactory.createPageResponse(List.of(testBookResponse));
    }

    @Nested
    @DisplayName("GET /api/books Tests")
    class GetAllBooksTests {

        @Test
        @DisplayName("Should return 200 with PageResponse body")
        void getAllBooks_returns200WithPageResponseBody() throws Exception {
            // Arrange
            when(bookService.getAllBooks(any(Pageable.class))).thenReturn(testPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].isbn", is("9780000000001")))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.pageNumber", is(0)));
        }

        @Test
        @DisplayName("Should route to searchBooksByAuthor when author param is provided")
        void getAllBooks_withAuthorParam_routesToSearchBooksByAuthor() throws Exception {
            // Arrange
            when(bookService.searchBooksByAuthor(eq("tolkien"), any(Pageable.class))).thenReturn(testPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .param("author", "tolkien")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(bookService).searchBooksByAuthor(eq("tolkien"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should route to getBooksByGenre when genre param is provided")
        void getAllBooks_withGenreParam_routesToGetBooksByGenre() throws Exception {
            // Arrange
            when(bookService.getBooksByGenre(eq("fantasy"), any(Pageable.class))).thenReturn(testPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/books")
                            .param("genre", "fantasy")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(bookService).getBooksByGenre(eq("fantasy"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/books/{id} Tests")
    class GetBookByIdTests {

        @Test
        @DisplayName("Should return 200 with BookResponse")
        void getBookById_returns200WithBookResponse() throws Exception {
            // Arrange
            when(bookService.getBookById(1L)).thenReturn(testBookResponse);

            // Act & Assert
            mockMvc.perform(get("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.isbn", is("9780000000001")));
        }

        @Test
        @DisplayName("Should return 404 when ResourceNotFoundException thrown")
        void getBookById_returns404WhenResourceNotFoundExceptionThrown() throws Exception {
            // Arrange
            when(bookService.getBookById(999L)).thenThrow(new ResourceNotFoundException("Book", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/books/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", containsString("Book")));
        }
    }

    @Nested
    @DisplayName("POST /api/books Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should return 201 with created BookResponse")
        void createBook_returns201WithCreatedBookResponse() throws Exception {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("9780000000002", 1L);
            BookResponse createdResponse = TestDataFactory.createBookResponse(2L, "9780000000002");
            when(bookService.createBook(any(CreateBookRequest.class))).thenReturn(createdResponse);

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.isbn", is("9780000000002")));
        }

        @Test
        @DisplayName("Should return 400 when ISBN is blank (validation)")
        void createBook_returns400WhenIsbnIsBlank() throws Exception {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("", 1L);

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.validationErrors.isbn", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 when libraryId is null (validation)")
        void createBook_returns400WhenLibraryIdIsNull() throws Exception {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("9780000000001", null);

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.validationErrors.libraryId", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 when BadRequestException thrown (duplicate ISBN)")
        void createBook_returns400WhenBadRequestExceptionThrown() throws Exception {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("9780000000001", 1L);
            when(bookService.createBook(any(CreateBookRequest.class)))
                    .thenThrow(new BadRequestException("Book with ISBN 9780000000001 already exists"));

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.message", containsString("ISBN")));
        }

        @Test
        @DisplayName("Should return 400 when pages is out of valid range")
        void createBook_returns400WhenPagesOutOfRange() throws Exception {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("9780000000001", 1L);
            request.setPages(15000); // exceeds max of 10000

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.pages", notNullValue()));
        }
    }

    @Nested
    @DisplayName("PUT /api/books/{id} Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should return 200 with updated BookResponse")
        void updateBook_returns200WithUpdatedBookResponse() throws Exception {
            // Arrange
            UpdateBookRequest request = TestDataFactory.createUpdateBookRequest("9780000000001", 1L);
            BookResponse updatedResponse = TestDataFactory.createBookResponse(1L, "9780000000001");
            when(bookService.updateBook(eq(1L), any(UpdateBookRequest.class))).thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("Should return 404 when book not found")
        void updateBook_returns404WhenBookNotFound() throws Exception {
            // Arrange
            UpdateBookRequest request = TestDataFactory.createUpdateBookRequest("9780000000001", 1L);
            when(bookService.updateBook(eq(999L), any(UpdateBookRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Book", 999L));

            // Act & Assert
            mockMvc.perform(put("/api/books/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{id} Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should return 204 on successful delete")
        void deleteBook_returns204OnSuccessfulDelete() throws Exception {
            // Arrange
            doNothing().when(bookService).deleteBook(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(bookService).deleteBook(1L);
        }

        @Test
        @DisplayName("Should return 404 when ResourceNotFoundException thrown")
        void deleteBook_returns404WhenResourceNotFoundExceptionThrown() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Book", 999L)).when(bookService).deleteBook(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/books/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/books/by-publication-date Tests")
    class GetBooksByPublicationDateTests {

        @Test
        @DisplayName("Should return 200 with date params")
        void getBooksByPublicationDate_returns200WithDateParams() throws Exception {
            // Arrange
            when(bookService.getBooksByPublicationDateRange(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(testPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/books/by-publication-date")
                            .param("startDate", "2020-01-01")
                            .param("endDate", "2020-12-31")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 400 when date range is invalid")
        void getBooksByPublicationDate_returns400WhenDateRangeInvalid() throws Exception {
            // Arrange
            when(bookService.getBooksByPublicationDateRange(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                    .thenThrow(new BadRequestException("Start date must be before or equal to end date"));

            // Act & Assert
            mockMvc.perform(get("/api/books/by-publication-date")
                            .param("startDate", "2020-12-31")
                            .param("endDate", "2020-01-01")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/books/by-acquisition-date Tests")
    class GetBooksByAcquisitionDateTests {

        @Test
        @DisplayName("Should return 200 with date params")
        void getBooksByAcquisitionDate_returns200WithDateParams() throws Exception {
            // Arrange
            when(bookService.getBooksByAcquisitionDateRange(any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                    .thenReturn(testPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/books/by-acquisition-date")
                            .param("startDate", "2020-01-01")
                            .param("endDate", "2020-12-31")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }
    }
}
