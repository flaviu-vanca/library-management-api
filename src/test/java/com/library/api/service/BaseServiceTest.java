package com.library.api.service;

import com.library.api.exception.BadRequestException;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BaseService abstract class.
 * Uses a concrete test subclass to test protected methods.
 * JUnit 5 + Mockito — NO Spring context.
 */
@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    /**
     * Concrete subclass of BaseService for testing purposes.
     * Exposes protected methods as public for test access.
     */
    static class TestableBaseService extends BaseService {

        public <T, ID> T testFindByIdOrThrow(JpaRepository<T, ID> repository, ID id, String entityName) {
            return findByIdOrThrow(repository, id, entityName);
        }

        public void testValidateDateRange(LocalDate startDate, LocalDate endDate) {
            validateDateRange(startDate, endDate);
        }

        public void testValidateAcquisitionDate(LocalDate publicationDate, LocalDate acquisitionDate) {
            validateAcquisitionDate(publicationDate, acquisitionDate);
        }

        public <T, ID> void testEnsureEntityExists(JpaRepository<T, ID> repository, ID id, String entityName) {
            ensureEntityExists(repository, id, entityName);
        }
    }

    @Mock
    private JpaRepository<Book, Long> mockRepository;

    private TestableBaseService baseService;

    @BeforeEach
    void setUp() {
        baseService = new TestableBaseService();
    }

    @Nested
    @DisplayName("findByIdOrThrow Tests")
    class FindByIdOrThrowTests {

        @Test
        @DisplayName("Should return entity when found")
        void findByIdOrThrow_returnsEntityWhenFound() {
            // Arrange
            Book expectedBook = new Book();
            expectedBook.setId(1L);
            expectedBook.setTitle("Test Book");
            when(mockRepository.findById(1L)).thenReturn(Optional.of(expectedBook));

            // Act
            Book result = baseService.testFindByIdOrThrow(mockRepository, 1L, "Book");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Book");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when empty")
        void findByIdOrThrow_throwsResourceNotFoundExceptionWhenEmpty() {
            // Arrange
            when(mockRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> baseService.testFindByIdOrThrow(mockRepository, 999L, "Book"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Book")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should include entity name in exception message")
        void findByIdOrThrow_includesEntityNameInExceptionMessage() {
            // Arrange
            when(mockRepository.findById(42L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> baseService.testFindByIdOrThrow(mockRepository, 42L, "Library"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Library")
                    .hasMessageContaining("42");
        }
    }

    @Nested
    @DisplayName("validateDateRange Tests")
    class ValidateDateRangeTests {

        @Test
        @DisplayName("Should throw BadRequestException when startDate is after endDate")
        void validateDateRange_throwsBadRequestExceptionWhenStartDateAfterEndDate() {
            // Arrange
            LocalDate startDate = LocalDate.of(2020, 12, 31);
            LocalDate endDate = LocalDate.of(2020, 1, 1);

            // Act & Assert
            assertThatThrownBy(() -> baseService.testValidateDateRange(startDate, endDate))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Start date")
                    .hasMessageContaining("before or equal");
        }

        @Test
        @DisplayName("Should pass when startDate equals endDate")
        void validateDateRange_passesWhenStartDateEqualsEndDate() {
            // Arrange
            LocalDate sameDate = LocalDate.of(2020, 6, 15);

            // Act & Assert - no exception should be thrown
            baseService.testValidateDateRange(sameDate, sameDate);
        }

        @Test
        @DisplayName("Should pass when startDate is before endDate")
        void validateDateRange_passesWhenStartDateBeforeEndDate() {
            // Arrange
            LocalDate startDate = LocalDate.of(2020, 1, 1);
            LocalDate endDate = LocalDate.of(2020, 12, 31);

            // Act & Assert - no exception should be thrown
            baseService.testValidateDateRange(startDate, endDate);
        }

        @Test
        @DisplayName("Should pass when dates are one day apart")
        void validateDateRange_passesWhenDatesAreOneDayApart() {
            // Arrange
            LocalDate startDate = LocalDate.of(2020, 6, 14);
            LocalDate endDate = LocalDate.of(2020, 6, 15);

            // Act & Assert - no exception should be thrown
            baseService.testValidateDateRange(startDate, endDate);
        }
    }

    @Nested
    @DisplayName("validateAcquisitionDate Tests")
    class ValidateAcquisitionDateTests {

        @Test
        @DisplayName("Should throw BadRequestException when acquisitionDate before publicationDate")
        void validateAcquisitionDate_throwsBadRequestExceptionWhenAcquisitionDateBeforePublicationDate() {
            // Arrange
            LocalDate publicationDate = LocalDate.of(2020, 6, 1);
            LocalDate acquisitionDate = LocalDate.of(2020, 1, 1); // Before publication

            // Act & Assert
            assertThatThrownBy(() -> baseService.testValidateAcquisitionDate(publicationDate, acquisitionDate))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Acquisition date")
                    .hasMessageContaining("before");
        }

        @Test
        @DisplayName("Should pass when acquisitionDate equals publicationDate")
        void validateAcquisitionDate_passesWhenAcquisitionDateEqualsPublicationDate() {
            // Arrange
            LocalDate sameDate = LocalDate.of(2020, 6, 15);

            // Act & Assert - no exception should be thrown
            baseService.testValidateAcquisitionDate(sameDate, sameDate);
        }

        @Test
        @DisplayName("Should pass when acquisitionDate is after publicationDate")
        void validateAcquisitionDate_passesWhenAcquisitionDateAfterPublicationDate() {
            // Arrange
            LocalDate publicationDate = LocalDate.of(2020, 1, 1);
            LocalDate acquisitionDate = LocalDate.of(2020, 6, 1);

            // Act & Assert - no exception should be thrown
            baseService.testValidateAcquisitionDate(publicationDate, acquisitionDate);
        }

        @Test
        @DisplayName("Should pass when both dates are null")
        void validateAcquisitionDate_passesWhenBothDatesAreNull() {
            // Act & Assert - no exception should be thrown
            baseService.testValidateAcquisitionDate(null, null);
        }

        @Test
        @DisplayName("Should pass when publicationDate is null but acquisitionDate is provided")
        void validateAcquisitionDate_passesWhenPublicationDateIsNull() {
            // Arrange
            LocalDate acquisitionDate = LocalDate.of(2020, 6, 1);

            // Act & Assert - no exception should be thrown (can't compare)
            baseService.testValidateAcquisitionDate(null, acquisitionDate);
        }

        @Test
        @DisplayName("Should pass when acquisitionDate is null but publicationDate is provided")
        void validateAcquisitionDate_passesWhenAcquisitionDateIsNull() {
            // Arrange
            LocalDate publicationDate = LocalDate.of(2020, 1, 1);

            // Act & Assert - no exception should be thrown (can't compare)
            baseService.testValidateAcquisitionDate(publicationDate, null);
        }
    }

    @Nested
    @DisplayName("ensureEntityExists Tests")
    class EnsureEntityExistsTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when entity not found")
        void ensureEntityExists_throwsResourceNotFoundExceptionWhenEntityNotFound() {
            // Arrange
            when(mockRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> baseService.testEnsureEntityExists(mockRepository, 999L, "Library"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Library");
        }

        @Test
        @DisplayName("Should pass when entity exists")
        void ensureEntityExists_passesWhenEntityExists() {
            // Arrange
            when(mockRepository.existsById(1L)).thenReturn(true);

            // Act & Assert - no exception should be thrown
            baseService.testEnsureEntityExists(mockRepository, 1L, "Library");
        }
    }
}
