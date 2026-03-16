package com.library.api.service;

import com.library.api.util.PaginationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaginationUtils.
 * Plain JUnit 5 — NO mocks, NO Spring context.
 */
class PaginationUtilsTest {

    @Nested
    @DisplayName("createPageable Tests")
    class CreatePageableTests {

        @Test
        @DisplayName("Should produce correct PageRequest with 'id,asc' sort")
        void createPageable_withIdAsc_producesCorrectPageRequest() {
            // Act
            Pageable result = PaginationUtils.createPageable(0, 10, "id,asc");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPageNumber()).isEqualTo(0);
            assertThat(result.getPageSize()).isEqualTo(10);
            assertThat(result.getSort()).isNotNull();
            assertThat(result.getSort().getOrderFor("id")).isNotNull();
            assertThat(result.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("Should produce DESC sort with 'title,desc'")
        void createPageable_withTitleDesc_producesDescSort() {
            // Act
            Pageable result = PaginationUtils.createPageable(0, 20, "title,desc");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPageNumber()).isEqualTo(0);
            assertThat(result.getPageSize()).isEqualTo(20);
            assertThat(result.getSort().getOrderFor("title")).isNotNull();
            assertThat(result.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("Should default to ASC with single field (no comma)")
        void createPageable_withSingleFieldNoComma_defaultsToAsc() {
            // Act
            Pageable result = PaginationUtils.createPageable(1, 15, "author");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPageNumber()).isEqualTo(1);
            assertThat(result.getPageSize()).isEqualTo(15);
            assertThat(result.getSort().getOrderFor("author")).isNotNull();
            assertThat(result.getSort().getOrderFor("author").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("Should handle uppercase direction")
        void createPageable_withUppercaseDirection_parsesCorrectly() {
            // Act
            Pageable result = PaginationUtils.createPageable(0, 10, "name,DESC");

            // Assert
            assertThat(result.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("Should handle mixed case direction")
        void createPageable_withMixedCaseDirection_parsesCorrectly() {
            // Act
            Pageable result = PaginationUtils.createPageable(0, 10, "email,Desc");

            // Assert
            assertThat(result.getSort().getOrderFor("email").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("Should handle different page numbers correctly")
        void createPageable_withDifferentPageNumbers_setsPageCorrectly() {
            // Act
            Pageable page0 = PaginationUtils.createPageable(0, 10, "id,asc");
            Pageable page5 = PaginationUtils.createPageable(5, 10, "id,asc");
            Pageable page99 = PaginationUtils.createPageable(99, 10, "id,asc");

            // Assert
            assertThat(page0.getPageNumber()).isEqualTo(0);
            assertThat(page5.getPageNumber()).isEqualTo(5);
            assertThat(page99.getPageNumber()).isEqualTo(99);
        }

        @Test
        @DisplayName("Should handle different page sizes correctly")
        void createPageable_withDifferentPageSizes_setsSizeCorrectly() {
            // Act
            Pageable size5 = PaginationUtils.createPageable(0, 5, "id,asc");
            Pageable size50 = PaginationUtils.createPageable(0, 50, "id,asc");
            Pageable size100 = PaginationUtils.createPageable(0, 100, "id,asc");

            // Assert
            assertThat(size5.getPageSize()).isEqualTo(5);
            assertThat(size50.getPageSize()).isEqualTo(50);
            assertThat(size100.getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should handle field names with underscores")
        void createPageable_withUnderscoreFieldName_parsesCorrectly() {
            // Act
            Pageable result = PaginationUtils.createPageable(0, 10, "publication_date,asc");

            // Assert
            assertThat(result.getSort().getOrderFor("publication_date")).isNotNull();
            assertThat(result.getSort().getOrderFor("publication_date").getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        @DisplayName("Should handle camelCase field names")
        void createPageable_withCamelCaseFieldName_parsesCorrectly() {
            // Act
            Pageable result = PaginationUtils.createPageable(0, 10, "publicationDate,desc");

            // Assert
            assertThat(result.getSort().getOrderFor("publicationDate")).isNotNull();
            assertThat(result.getSort().getOrderFor("publicationDate").getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("Should default to ASC for invalid direction value")
        void createPageable_withInvalidDirection_defaultsToAsc() {
            // Act
            Pageable result = PaginationUtils.createPageable(0, 10, "id,invalid");

            // Assert
            assertThat(result.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }
}
