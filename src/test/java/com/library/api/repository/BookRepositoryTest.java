package com.library.api.repository;

import com.library.api.model.Book;
import com.library.api.model.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BookRepository.
 * Uses @DataJpaTest — loads only JPA slice, no web layer.
 * Tests against real H2 database.
 */
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    private Library testLibrary;
    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
        // Create and persist a test library
        testLibrary = new Library();
        testLibrary.setName("Test Library");
        testLibrary.setAddress("123 Test Street");
        testLibrary.setPhone("+1-555-0100");
        testLibrary.setEmail("test@library.org");
        testLibrary.setEstablishedDate(LocalDate.of(1990, 5, 20));
        testLibrary = entityManager.persistAndFlush(testLibrary);

        defaultPageable = PageRequest.of(0, 10);
    }

    private Book createAndPersistBook(String isbn, String title, String author, String genre,
            LocalDate publicationDate, LocalDate acquisitionDate) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPublicationDate(publicationDate);
        book.setAcquisitionDate(acquisitionDate);
        book.setPages(300);
        book.setLibrary(testLibrary);
        return entityManager.persistAndFlush(book);
    }

    @Nested
    @DisplayName("findByLibraryId Tests")
    class FindByLibraryIdTests {

        @Test
        @DisplayName("Should return books for a given library")
        void findByLibraryId_returnsBooksForGivenLibrary() {
            // Arrange
            createAndPersistBook("9780000000001", "Book 1", "Author 1", "Fiction",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));
            createAndPersistBook("9780000000002", "Book 2", "Author 2", "Fiction",
                    LocalDate.of(2020, 2, 1), LocalDate.of(2020, 7, 1));

            // Act
            Page<Book> result = bookRepository.findByLibraryId(testLibrary.getId(), defaultPageable);

            // Assert
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(book -> book.getLibrary().getId().equals(testLibrary.getId()));
        }

        @Test
        @DisplayName("Should return empty page when library has no books")
        void findByLibraryId_returnsEmptyPageWhenLibraryHasNoBooks() {
            // Arrange - create another library with no books
            Library emptyLibrary = new Library();
            emptyLibrary.setName("Empty Library");
            emptyLibrary = entityManager.persistAndFlush(emptyLibrary);

            // Act
            Page<Book> result = bookRepository.findByLibraryId(emptyLibrary.getId(), defaultPageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIsbn Tests")
    class FindByIsbnTests {

        @Test
        @DisplayName("Should return correct book for existing ISBN")
        void findByIsbn_returnsCorrectBook() {
            // Arrange
            String targetIsbn = "9781234567890";
            createAndPersistBook(targetIsbn, "Target Book", "Target Author", "Fiction",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));

            // Act
            Optional<Book> result = bookRepository.findByIsbn(targetIsbn);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getIsbn()).isEqualTo(targetIsbn);
            assertThat(result.get().getTitle()).isEqualTo("Target Book");
        }

        @Test
        @DisplayName("Should return empty optional for non-existing ISBN")
        void findByIsbn_returnsEmptyForNonExistingIsbn() {
            // Act
            Optional<Book> result = bookRepository.findByIsbn("9999999999999");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByIsbn Tests")
    class ExistsByIsbnTests {

        @Test
        @DisplayName("Should return true for existing ISBN")
        void existsByIsbn_returnsTrueForExistingIsbn() {
            // Arrange
            String existingIsbn = "9780000000001";
            createAndPersistBook(existingIsbn, "Existing Book", "Author", "Fiction",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));

            // Act
            boolean result = bookRepository.existsByIsbn(existingIsbn);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existing ISBN")
        void existsByIsbn_returnsFalseForNonExistingIsbn() {
            // Act
            boolean result = bookRepository.existsByIsbn("9999999999999");

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findByPublicationDateBetween Tests")
    class FindByPublicationDateBetweenTests {

        @Test
        @DisplayName("Should return books within publication date range")
        void findByPublicationDateBetween_returnsBooksWithinRange() {
            // Arrange
            createAndPersistBook("9780000000001", "Book 2019", "Author", "Fiction",
                    LocalDate.of(2019, 6, 15), LocalDate.of(2020, 1, 1));
            createAndPersistBook("9780000000002", "Book 2020", "Author", "Fiction",
                    LocalDate.of(2020, 6, 15), LocalDate.of(2021, 1, 1));
            createAndPersistBook("9780000000003", "Book 2021", "Author", "Fiction",
                    LocalDate.of(2021, 6, 15), LocalDate.of(2022, 1, 1));

            LocalDate startDate = LocalDate.of(2020, 1, 1);
            LocalDate endDate = LocalDate.of(2020, 12, 31);

            // Act
            Page<Book> result = bookRepository.findByPublicationDateBetween(startDate, endDate, defaultPageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Book 2020");
        }

        @Test
        @DisplayName("Should include boundary dates in range")
        void findByPublicationDateBetween_includesBoundaryDates() {
            // Arrange
            LocalDate exactDate = LocalDate.of(2020, 6, 15);
            createAndPersistBook("9780000000001", "Exact Date Book", "Author", "Fiction",
                    exactDate, LocalDate.of(2020, 12, 1));

            // Act
            Page<Book> result = bookRepository.findByPublicationDateBetween(exactDate, exactDate, defaultPageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findByAcquisitionDateBetween Tests")
    class FindByAcquisitionDateBetweenTests {

        @Test
        @DisplayName("Should return books within acquisition date range")
        void findByAcquisitionDateBetween_returnsBooksWithinRange() {
            // Arrange - use future dates that won't conflict with sample data
            LocalDate futureStart = LocalDate.of(2030, 1, 1);
            LocalDate futureMiddle = LocalDate.of(2030, 6, 15);
            LocalDate futureEnd = LocalDate.of(2030, 12, 31);
            LocalDate beyondRange = LocalDate.of(2031, 6, 15);

            createAndPersistBook("9780000000001", "Future Book Q1", "Author", "Fiction",
                    LocalDate.of(2020, 1, 1), futureStart);
            createAndPersistBook("9780000000002", "Future Book Q3", "Author", "Fiction",
                    LocalDate.of(2020, 1, 1), futureMiddle);
            createAndPersistBook("9780000000003", "Future Book Beyond", "Author", "Fiction",
                    LocalDate.of(2020, 1, 1), beyondRange);

            // Act
            Page<Book> result = bookRepository.findByAcquisitionDateBetween(futureStart, futureEnd, defaultPageable);

            // Assert - should find exactly the 2 books within 2030
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(book -> !book.getAcquisitionDate().isBefore(futureStart)
                            && !book.getAcquisitionDate().isAfter(futureEnd));
        }
    }

    @Nested
    @DisplayName("findByAuthorContainingIgnoreCase Tests")
    class FindByAuthorContainingIgnoreCaseTests {

        @Test
        @DisplayName("Should be case-insensitive when searching by author")
        void findByAuthorContainingIgnoreCase_isCaseInsensitive() {
            // Arrange - use a unique author name to avoid conflicts with sample data
            String uniqueAuthor = "UniqueTestAuthor_" + System.nanoTime();
            createAndPersistBook("9780000000001", "The Unique Book", uniqueAuthor, "Fantasy",
                    LocalDate.of(1937, 9, 21), LocalDate.of(2020, 1, 1));

            // Act - search with different cases
            Page<Book> resultLower = bookRepository.findByAuthorContainingIgnoreCase(uniqueAuthor.toLowerCase(), defaultPageable);
            Page<Book> resultUpper = bookRepository.findByAuthorContainingIgnoreCase(uniqueAuthor.toUpperCase(), defaultPageable);

            // Assert - should find exactly 1 match regardless of case
            assertThat(resultLower.getContent()).hasSize(1);
            assertThat(resultUpper.getContent()).hasSize(1);
            assertThat(resultLower.getContent().get(0).getAuthor()).isEqualTo(uniqueAuthor);
        }

        @Test
        @DisplayName("Should find partial author name matches")
        void findByAuthorContainingIgnoreCase_findsPartialMatches() {
            // Arrange - use unique prefix to avoid sample data conflicts
            String uniquePrefix = "UniqueName_" + System.nanoTime();
            createAndPersistBook("9780000000001", "Book 1", uniquePrefix + " First", "Horror",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));
            createAndPersistBook("9780000000002", "Book 2", uniquePrefix + " Second", "Biography",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));

            // Act
            Page<Book> result = bookRepository.findByAuthorContainingIgnoreCase(uniquePrefix, defaultPageable);

            // Assert
            assertThat(result.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByGenreIgnoreCase Tests")
    class FindByGenreIgnoreCaseTests {

        @Test
        @DisplayName("Should be case-insensitive when searching by genre")
        void findByGenreIgnoreCase_isCaseInsensitive() {
            // Arrange - use a unique genre to avoid sample data conflicts
            String uniqueGenre = "UniqueGenre_" + System.nanoTime();
            createAndPersistBook("9780000000001", "Unique Genre Book", "Author", uniqueGenre,
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));

            // Act - search with different cases
            Page<Book> resultLower = bookRepository.findByGenreIgnoreCase(uniqueGenre.toLowerCase(), defaultPageable);
            Page<Book> resultUpper = bookRepository.findByGenreIgnoreCase(uniqueGenre.toUpperCase(), defaultPageable);

            // Assert
            assertThat(resultLower.getContent()).hasSize(1);
            assertThat(resultUpper.getContent()).hasSize(1);
            assertThat(resultLower.getContent().get(0).getGenre()).isEqualTo(uniqueGenre);
        }

        @Test
        @DisplayName("Should return empty when genre does not match")
        void findByGenreIgnoreCase_returnsEmptyWhenNoMatch() {
            // Use a genre that definitely doesn't exist in sample data
            String nonExistentGenre = "NonExistentGenre_" + System.nanoTime();

            // Act
            Page<Book> result = bookRepository.findByGenreIgnoreCase(nonExistentGenre, defaultPageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByLibraryId Tests")
    class CountByLibraryIdTests {

        @Test
        @DisplayName("Should return correct count of books for library")
        void countByLibraryId_returnsCorrectCount() {
            // Arrange
            createAndPersistBook("9780000000001", "Book 1", "Author", "Fiction",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));
            createAndPersistBook("9780000000002", "Book 2", "Author", "Fiction",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));
            createAndPersistBook("9780000000003", "Book 3", "Author", "Fiction",
                    LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1));

            // Act
            long count = bookRepository.countByLibraryId(testLibrary.getId());

            // Assert
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return zero for library with no books")
        void countByLibraryId_returnsZeroForEmptyLibrary() {
            // Arrange
            Library emptyLibrary = new Library();
            emptyLibrary.setName("Empty Library");
            emptyLibrary = entityManager.persistAndFlush(emptyLibrary);

            // Act
            long count = bookRepository.countByLibraryId(emptyLibrary.getId());

            // Assert
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Cascade Delete Tests")
    class CascadeDeleteTests {

        @Test
        @DisplayName("Should delete all books when library is deleted (cascade)")
        void deleteLibrary_cascadeDeletesAllBooks() {
            // Arrange - create a fresh library for this test
            Library cascadeLibrary = new Library();
            cascadeLibrary.setName("Cascade Test Library");
            cascadeLibrary.setAddress("123 Cascade Street");
            cascadeLibrary = entityManager.persistAndFlush(cascadeLibrary);
            Long libraryId = cascadeLibrary.getId();

            // Add books to this library
            Book book1 = new Book();
            book1.setIsbn("9780000000101");
            book1.setTitle("Cascade Book 1");
            book1.setAuthor("Author");
            book1.setGenre("Fiction");
            book1.setPublicationDate(LocalDate.of(2020, 1, 1));
            book1.setAcquisitionDate(LocalDate.of(2020, 6, 1));
            book1.setPages(300);
            book1.setLibrary(cascadeLibrary);
            entityManager.persistAndFlush(book1);

            Book book2 = new Book();
            book2.setIsbn("9780000000102");
            book2.setTitle("Cascade Book 2");
            book2.setAuthor("Author");
            book2.setGenre("Fiction");
            book2.setPublicationDate(LocalDate.of(2020, 1, 1));
            book2.setAcquisitionDate(LocalDate.of(2020, 6, 1));
            book2.setPages(300);
            book2.setLibrary(cascadeLibrary);
            entityManager.persistAndFlush(book2);

            // Verify books exist before delete
            assertThat(bookRepository.countByLibraryId(libraryId)).isEqualTo(2);

            // Clear the persistence context to avoid stale references
            entityManager.clear();

            // Act - delete the library (this should cascade to books)
            Library toDelete = entityManager.find(Library.class, libraryId);
            entityManager.remove(toDelete);
            entityManager.flush();
            entityManager.clear();

            // Assert - books should be deleted due to cascade
            assertThat(bookRepository.findByLibraryId(libraryId, defaultPageable).getContent()).isEmpty();
            assertThat(libraryRepository.findById(libraryId)).isEmpty();
        }
    }
}
