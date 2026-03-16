package com.library.api.service;

import com.library.api.dto.request.CreateBookRequest;
import com.library.api.dto.request.UpdateBookRequest;
import com.library.api.dto.response.BookResponse;
import com.library.api.dto.response.PageResponse;
import com.library.api.exception.BadRequestException;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.mapper.BookMapper;
import com.library.api.model.Book;
import com.library.api.model.Library;
import com.library.api.repository.BookRepository;
import com.library.api.repository.LibraryRepository;
import com.library.api.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookServiceImpl.
 * Uses Mockito only — NO Spring context loaded.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Library testLibrary;
    private Book testBook;
    private BookResponse testBookResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testLibrary = TestDataFactory.createLibrary(1L, "Test Library");
        testBook = TestDataFactory.createBook(1L, "9780000000001", testLibrary);
        testBookResponse = TestDataFactory.createBookResponse(1L, "9780000000001");
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("getAllBooks Tests")
    class GetAllBooksTests {

        @Test
        @DisplayName("Should return PageResponse with books when books exist")
        void getAllBooks_returnsPageResponseCorrectly() {
            // Arrange
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
            when(bookRepository.findAll(pageable)).thenReturn(bookPage);
            when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

            // Act
            PageResponse<BookResponse> result = bookService.getAllBooks(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getIsbn()).isEqualTo("9780000000001");
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(bookRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty PageResponse when no books exist")
        void getAllBooks_returnsEmptyPageWhenNoBooksExist() {
            // Arrange
            Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(bookRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            PageResponse<BookResponse> result = bookService.getAllBooks(pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getBookById Tests")
    class GetBookByIdTests {

        @Test
        @DisplayName("Should return BookResponse for valid id")
        void getBookById_returnsBookResponseForValidId() {
            // Arrange
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

            // Act
            BookResponse result = bookService.getBookById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getIsbn()).isEqualTo("9780000000001");
            verify(bookRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for missing id")
        void getBookById_throwsResourceNotFoundExceptionForMissingId() {
            // Arrange
            when(bookRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.getBookById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Book")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("createBook Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should succeed with valid request and valid libraryId")
        void createBook_succeedsWithValidRequestAndLibraryId() {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("9780000000002", 1L);
            Book newBook = TestDataFactory.createBook(2L, "9780000000002", testLibrary);
            BookResponse expectedResponse = TestDataFactory.createBookResponse(2L, "9780000000002");

            when(bookRepository.existsByIsbn("9780000000002")).thenReturn(false);
            when(libraryRepository.findById(1L)).thenReturn(Optional.of(testLibrary));
            when(bookMapper.toEntity(request)).thenReturn(newBook);
            when(bookRepository.save(any(Book.class))).thenReturn(newBook);
            when(bookMapper.toResponse(newBook)).thenReturn(expectedResponse);

            // Act
            BookResponse result = bookService.createBook(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getIsbn()).isEqualTo("9780000000002");
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when ISBN already exists")
        void createBook_throwsBadRequestExceptionWhenIsbnExists() {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("9780000000001", 1L);
            when(bookRepository.existsByIsbn("9780000000001")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> bookService.createBook(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("ISBN")
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when libraryId does not exist")
        void createBook_throwsResourceNotFoundExceptionWhenLibraryIdNotExists() {
            // Arrange
            CreateBookRequest request = TestDataFactory.createCreateBookRequest("9780000000002", 999L);
            when(bookRepository.existsByIsbn("9780000000002")).thenReturn(false);
            when(libraryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.createBook(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Library");
        }

        @Test
        @DisplayName("Should throw BadRequestException when acquisitionDate is before publicationDate")
        void createBook_throwsBadRequestExceptionWhenAcquisitionDateBeforePublicationDate() {
            // Arrange
            LocalDate publicationDate = LocalDate.of(2020, 6, 1);
            LocalDate acquisitionDate = LocalDate.of(2020, 1, 1); // Before publication
            CreateBookRequest request = TestDataFactory.createCreateBookRequestWithDates(
                    "9780000000002", 1L, publicationDate, acquisitionDate);
            when(bookRepository.existsByIsbn("9780000000002")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> bookService.createBook(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Acquisition date")
                    .hasMessageContaining("before");
        }
    }

    @Nested
    @DisplayName("updateBook Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should succeed and update fields")
        void updateBook_succeedsAndUpdatesFields() {
            // Arrange
            UpdateBookRequest request = TestDataFactory.createUpdateBookRequest("9780000000001", 1L);
            BookResponse updatedResponse = TestDataFactory.createBookResponse(1L, "9780000000001");
            updatedResponse = BookResponse.builder()
                    .id(1L)
                    .isbn("9780000000001")
                    .title("Updated Test Book")
                    .author("Updated Author")
                    .genre("Non-Fiction")
                    .publicationDate(TestDataFactory.FIXED_PUBLICATION_DATE)
                    .acquisitionDate(TestDataFactory.FIXED_ACQUISITION_DATE)
                    .pages(400)
                    .libraryId(1L)
                    .libraryName("Test Library")
                    .build();

            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.existsByIsbn("9780000000001")).thenReturn(true);
            when(bookRepository.findByIsbn("9780000000001")).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);
            when(bookMapper.toResponse(any(Book.class))).thenReturn(updatedResponse);

            // Act
            BookResponse result = bookService.updateBook(1L, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Test Book");
            verify(bookMapper).updateEntity(any(Book.class), eq(request));
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for missing book id")
        void updateBook_throwsResourceNotFoundExceptionForMissingBookId() {
            // Arrange
            UpdateBookRequest request = TestDataFactory.createUpdateBookRequest("9780000000001", 1L);
            when(bookRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.updateBook(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Book");
        }

        @Test
        @DisplayName("Should throw BadRequestException for duplicate ISBN on a different book")
        void updateBook_throwsBadRequestExceptionForDuplicateIsbnOnDifferentBook() {
            // Arrange
            Book anotherBook = TestDataFactory.createBook(2L, "9780000000002", testLibrary);
            UpdateBookRequest request = TestDataFactory.createUpdateBookRequest("9780000000002", 1L);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.existsByIsbn("9780000000002")).thenReturn(true);
            when(bookRepository.findByIsbn("9780000000002")).thenReturn(Optional.of(anotherBook));

            // Act & Assert
            assertThatThrownBy(() -> bookService.updateBook(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("ISBN")
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should not throw when ISBN belongs to same book")
        void updateBook_doesNotThrowWhenIsbnBelongsToSameBook() {
            // Arrange
            UpdateBookRequest request = TestDataFactory.createUpdateBookRequest("9780000000001", 1L);
            BookResponse expectedResponse = TestDataFactory.createBookResponse(1L, "9780000000001");

            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.existsByIsbn("9780000000001")).thenReturn(true);
            when(bookRepository.findByIsbn("9780000000001")).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);
            when(bookMapper.toResponse(any(Book.class))).thenReturn(expectedResponse);

            // Act
            BookResponse result = bookService.updateBook(1L, request);

            // Assert
            assertThat(result).isNotNull();
            verify(bookRepository).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("deleteBook Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should call bookRepository.delete() for valid id")
        void deleteBook_callsRepositoryDeleteForValidId() {
            // Arrange
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

            // Act
            bookService.deleteBook(1L);

            // Assert
            verify(bookRepository).delete(testBook);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for missing id")
        void deleteBook_throwsResourceNotFoundExceptionForMissingId() {
            // Arrange
            when(bookRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.deleteBook(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Book");

            verify(bookRepository, never()).delete(any(Book.class));
        }
    }

    @Nested
    @DisplayName("getBooksByPublicationDateRange Tests")
    class GetBooksByPublicationDateRangeTests {

        @Test
        @DisplayName("Should throw BadRequestException when startDate is after endDate")
        void getBooksByPublicationDateRange_throwsBadRequestExceptionWhenStartDateAfterEndDate() {
            // Arrange
            LocalDate startDate = LocalDate.of(2020, 12, 31);
            LocalDate endDate = LocalDate.of(2020, 1, 1);

            // Act & Assert
            assertThatThrownBy(() -> bookService.getBooksByPublicationDateRange(startDate, endDate, pageable))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Start date")
                    .hasMessageContaining("before or equal");
        }

        @Test
        @DisplayName("Should return results for valid date range")
        void getBooksByPublicationDateRange_returnsResultsForValidRange() {
            // Arrange
            LocalDate startDate = LocalDate.of(2020, 1, 1);
            LocalDate endDate = LocalDate.of(2020, 12, 31);
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

            when(bookRepository.findByPublicationDateBetween(startDate, endDate, pageable)).thenReturn(bookPage);
            when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

            // Act
            PageResponse<BookResponse> result = bookService.getBooksByPublicationDateRange(startDate, endDate, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(bookRepository).findByPublicationDateBetween(startDate, endDate, pageable);
        }

        @Test
        @DisplayName("Should pass when startDate equals endDate")
        void getBooksByPublicationDateRange_passesWhenStartDateEqualsEndDate() {
            // Arrange
            LocalDate sameDate = LocalDate.of(2020, 6, 15);
            Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(bookRepository.findByPublicationDateBetween(sameDate, sameDate, pageable)).thenReturn(emptyPage);

            // Act
            PageResponse<BookResponse> result = bookService.getBooksByPublicationDateRange(sameDate, sameDate, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(bookRepository).findByPublicationDateBetween(sameDate, sameDate, pageable);
        }
    }

    @Nested
    @DisplayName("getBooksByAcquisitionDateRange Tests")
    class GetBooksByAcquisitionDateRangeTests {

        @Test
        @DisplayName("Should return results for valid acquisition date range")
        void getBooksByAcquisitionDateRange_returnsResultsForValidRange() {
            // Arrange
            LocalDate startDate = LocalDate.of(2020, 1, 1);
            LocalDate endDate = LocalDate.of(2020, 12, 31);
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

            when(bookRepository.findByAcquisitionDateBetween(startDate, endDate, pageable)).thenReturn(bookPage);
            when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

            // Act
            PageResponse<BookResponse> result = bookService.getBooksByAcquisitionDateRange(startDate, endDate, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(bookRepository).findByAcquisitionDateBetween(startDate, endDate, pageable);
        }
    }

    @Nested
    @DisplayName("searchBooksByAuthor Tests")
    class SearchBooksByAuthorTests {

        @Test
        @DisplayName("Should return matching books")
        void searchBooksByAuthor_returnsMatchingBooks() {
            // Arrange
            String author = "Tolkien";
            Book tolkienBook = TestDataFactory.createBookWithDetails(
                    1L, "9780000000001", "The Hobbit", "J.R.R. Tolkien",
                    "Fantasy", LocalDate.of(1937, 9, 21),
                    LocalDate.of(2000, 1, 1), 310, testLibrary);
            BookResponse tolkienResponse = BookResponse.builder()
                    .id(1L)
                    .isbn("9780000000001")
                    .title("The Hobbit")
                    .author("J.R.R. Tolkien")
                    .genre("Fantasy")
                    .build();

            List<Book> books = List.of(tolkienBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

            when(bookRepository.findByAuthorContainingIgnoreCase(author, pageable)).thenReturn(bookPage);
            when(bookMapper.toResponse(tolkienBook)).thenReturn(tolkienResponse);

            // Act
            PageResponse<BookResponse> result = bookService.searchBooksByAuthor(author, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAuthor()).contains("Tolkien");
            verify(bookRepository).findByAuthorContainingIgnoreCase(author, pageable);
        }
    }

    @Nested
    @DisplayName("getBooksByGenre Tests")
    class GetBooksByGenreTests {

        @Test
        @DisplayName("Should return matching books")
        void getBooksByGenre_returnsMatchingBooks() {
            // Arrange
            String genre = "Fantasy";
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

            when(bookRepository.findByGenreIgnoreCase(genre, pageable)).thenReturn(bookPage);
            when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

            // Act
            PageResponse<BookResponse> result = bookService.getBooksByGenre(genre, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(bookRepository).findByGenreIgnoreCase(genre, pageable);
        }
    }
}
