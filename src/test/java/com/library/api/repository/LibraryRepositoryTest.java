package com.library.api.repository;

import com.library.api.model.Library;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LibraryRepository.
 * Uses @DataJpaTest — loads only JPA slice, no web layer.
 * Tests against real H2 database.
 */
@DataJpaTest
class LibraryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LibraryRepository libraryRepository;

    private Library createLibrary(String name) {
        Library library = new Library();
        library.setName(name);
        library.setAddress("123 Test Street");
        library.setPhone("+1-555-0100");
        library.setEmail(name.toLowerCase().replace(" ", "") + "@library.org");
        library.setEstablishedDate(LocalDate.of(1990, 5, 20));
        return library;
    }

    @Nested
    @DisplayName("findByName Tests")
    class FindByNameTests {

        @Test
        @DisplayName("Should return correct library for existing name")
        void findByName_returnsCorrectLibrary() {
            // Arrange
            Library library = createLibrary("Dublin Central Library");
            entityManager.persistAndFlush(library);

            // Act
            Optional<Library> result = libraryRepository.findByName("Dublin Central Library");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Dublin Central Library");
            assertThat(result.get().getEmail()).isEqualTo("dublincentrallibrary@library.org");
        }

        @Test
        @DisplayName("Should return empty optional for non-existing name")
        void findByName_returnsEmptyForNonExistingName() {
            // Act
            Optional<Library> result = libraryRepository.findByName("Non Existing Library");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should be case-sensitive for name lookup")
        void findByName_isCaseSensitive() {
            // Arrange
            Library library = createLibrary("Cork City Library");
            entityManager.persistAndFlush(library);

            // Act
            Optional<Library> exactMatch = libraryRepository.findByName("Cork City Library");
            Optional<Library> lowerCase = libraryRepository.findByName("cork city library");
            Optional<Library> upperCase = libraryRepository.findByName("CORK CITY LIBRARY");

            // Assert
            assertThat(exactMatch).isPresent();
            assertThat(lowerCase).isEmpty();
            assertThat(upperCase).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByName Tests")
    class ExistsByNameTests {

        @Test
        @DisplayName("Should return true for existing name")
        void existsByName_returnsTrueForExistingName() {
            // Arrange
            Library library = createLibrary("Galway University Library");
            entityManager.persistAndFlush(library);

            // Act
            boolean result = libraryRepository.existsByName("Galway University Library");

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existing name")
        void existsByName_returnsFalseForNonExistingName() {
            // Act
            boolean result = libraryRepository.existsByName("Non Existing Library");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should be case-sensitive")
        void existsByName_isCaseSensitive() {
            // Arrange
            Library library = createLibrary("Limerick Library");
            entityManager.persistAndFlush(library);

            // Act & Assert
            assertThat(libraryRepository.existsByName("Limerick Library")).isTrue();
            assertThat(libraryRepository.existsByName("limerick library")).isFalse();
            assertThat(libraryRepository.existsByName("LIMERICK LIBRARY")).isFalse();
        }
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should persist library and auto-generate id")
        void save_persistsLibraryAndGeneratesId() {
            // Arrange
            Library library = createLibrary("New Test Library");
            assertThat(library.getId()).isNull();

            // Act
            Library savedLibrary = libraryRepository.save(library);
            entityManager.flush();

            // Assert
            assertThat(savedLibrary.getId()).isNotNull();
            assertThat(savedLibrary.getId()).isPositive();
            assertThat(savedLibrary.getName()).isEqualTo("New Test Library");
        }

        @Test
        @DisplayName("Should persist library with all fields")
        void save_persistsLibraryWithAllFields() {
            // Arrange - use a modern date to avoid timezone edge cases
            Library library = new Library();
            library.setName("Complete Library");
            library.setAddress("789 Complete Street, Complete City");
            library.setPhone("+353-1-555-0999");
            library.setEmail("complete@library.ie");
            library.setEstablishedDate(LocalDate.of(2000, 6, 15));

            // Act
            Library savedLibrary = libraryRepository.save(library);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Library foundLibrary = entityManager.find(Library.class, savedLibrary.getId());
            assertThat(foundLibrary.getName()).isEqualTo("Complete Library");
            assertThat(foundLibrary.getAddress()).isEqualTo("789 Complete Street, Complete City");
            assertThat(foundLibrary.getPhone()).isEqualTo("+353-1-555-0999");
            assertThat(foundLibrary.getEmail()).isEqualTo("complete@library.ie");
            assertThat(foundLibrary.getEstablishedDate()).isEqualTo(LocalDate.of(2000, 6, 15));
        }

        @Test
        @DisplayName("Should update existing library when saved again")
        void save_updatesExistingLibrary() {
            // Arrange
            Library library = createLibrary("Original Name");
            Library savedLibrary = entityManager.persistAndFlush(library);
            Long originalId = savedLibrary.getId();

            // Act
            savedLibrary.setName("Updated Name");
            savedLibrary.setAddress("Updated Address");
            libraryRepository.save(savedLibrary);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Library updatedLibrary = entityManager.find(Library.class, originalId);
            assertThat(updatedLibrary.getId()).isEqualTo(originalId);
            assertThat(updatedLibrary.getName()).isEqualTo("Updated Name");
            assertThat(updatedLibrary.getAddress()).isEqualTo("Updated Address");
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should remove library from database")
        void deleteById_removesLibrary() {
            // Arrange
            Library library = createLibrary("Library To Delete");
            Library savedLibrary = entityManager.persistAndFlush(library);
            Long libraryId = savedLibrary.getId();

            // Verify library exists
            assertThat(libraryRepository.findById(libraryId)).isPresent();

            // Act
            libraryRepository.deleteById(libraryId);
            entityManager.flush();

            // Assert
            assertThat(libraryRepository.findById(libraryId)).isEmpty();
        }

        @Test
        @DisplayName("Should not affect other libraries when one is deleted")
        void deleteById_doesNotAffectOtherLibraries() {
            // Arrange
            Library library1 = createLibrary("Library One");
            Library library2 = createLibrary("Library Two");
            Library savedLibrary1 = entityManager.persistAndFlush(library1);
            Library savedLibrary2 = entityManager.persistAndFlush(library2);

            // Act
            libraryRepository.deleteById(savedLibrary1.getId());
            entityManager.flush();

            // Assert
            assertThat(libraryRepository.findById(savedLibrary1.getId())).isEmpty();
            assertThat(libraryRepository.findById(savedLibrary2.getId())).isPresent();
            assertThat(libraryRepository.findById(savedLibrary2.getId()).get().getName()).isEqualTo("Library Two");
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all libraries including sample data")
        void findAll_returnsAllLibraries() {
            // Get the initial count (sample data)
            int initialCount = (int) libraryRepository.count();

            // Arrange - add more libraries
            entityManager.persistAndFlush(createLibrary("Library A Extra"));
            entityManager.persistAndFlush(createLibrary("Library B Extra"));
            entityManager.persistAndFlush(createLibrary("Library C Extra"));

            // Act
            var result = libraryRepository.findAll();

            // Assert - should have 3 more than the initial count
            assertThat(result).hasSize(initialCount + 3);
        }

        @Test
        @DisplayName("Should include newly added libraries in results")
        void findAll_includesNewlyAddedLibraries() {
            // Arrange - add a uniquely named library
            String uniqueName = "UniqueLibrary_" + System.nanoTime();
            entityManager.persistAndFlush(createLibrary(uniqueName));

            // Act
            var result = libraryRepository.findAll();

            // Assert - verify our new library is included
            assertThat(result).anyMatch(lib -> lib.getName().equals(uniqueName));
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return library for valid id")
        void findById_returnsLibraryForValidId() {
            // Arrange
            Library library = createLibrary("Find By Id Library");
            Library savedLibrary = entityManager.persistAndFlush(library);

            // Act
            Optional<Library> result = libraryRepository.findById(savedLibrary.getId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Find By Id Library");
        }

        @Test
        @DisplayName("Should return empty optional for non-existing id")
        void findById_returnsEmptyForNonExistingId() {
            // Act
            Optional<Library> result = libraryRepository.findById(999999L);

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
