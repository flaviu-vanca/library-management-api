package com.library.api.controller;

import com.library.api.exception.BadRequestException;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler.
 * Uses @WebMvcTest to test exception handling through the web layer.
 */
@WebMvcTest(BookController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Nested
    @DisplayName("ResourceNotFoundException Handler Tests")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Should map ResourceNotFoundException to 404 with correct JSON structure")
        void resourceNotFoundException_mapsTo404WithCorrectJsonStructure() throws Exception {
            // Arrange
            when(bookService.getBookById(999L))
                    .thenThrow(new ResourceNotFoundException("Book with ID 999 not found"));

            // Act & Assert
            mockMvc.perform(get("/api/books/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Book")))
                    .andExpect(jsonPath("$.message", containsString("999")))
                    .andExpect(jsonPath("$.path", is("/api/books/999")));
        }

        @Test
        @DisplayName("Should include correct path in error response")
        void resourceNotFoundException_includesCorrectPath() throws Exception {
            // Arrange
            when(bookService.getBookById(42L))
                    .thenThrow(new ResourceNotFoundException("Book", 42L));

            // Act & Assert
            mockMvc.perform(get("/api/books/42")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.path", is("/api/books/42")));
        }
    }

    @Nested
    @DisplayName("BadRequestException Handler Tests")
    class BadRequestExceptionTests {

        @Test
        @DisplayName("Should map BadRequestException to 400")
        void badRequestException_mapsTo400() throws Exception {
            // Arrange
            when(bookService.getBookById(anyLong()))
                    .thenThrow(new BadRequestException("ISBN already exists"));

            // Act & Assert
            mockMvc.perform(get("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", containsString("ISBN")));
        }

        @Test
        @DisplayName("Should include timestamp in error response")
        void badRequestException_includesTimestamp() throws Exception {
            // Arrange
            when(bookService.getBookById(anyLong()))
                    .thenThrow(new BadRequestException("Invalid request"));

            // Act & Assert
            mockMvc.perform(get("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException Handler Tests")
    class ValidationExceptionTests {

        @Test
        @DisplayName("Should map MethodArgumentNotValidException to 400 with validationErrors map")
        void validationException_mapsTo400WithValidationErrorsMap() throws Exception {
            // Arrange - send invalid JSON that will fail validation
            String invalidJson = """
                    {
                        "isbn": "",
                        "title": "",
                        "author": "",
                        "libraryId": null
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.validationErrors", notNullValue()))
                    .andExpect(jsonPath("$.validationErrors.isbn", notNullValue()))
                    .andExpect(jsonPath("$.validationErrors.title", notNullValue()))
                    .andExpect(jsonPath("$.validationErrors.author", notNullValue()))
                    .andExpect(jsonPath("$.validationErrors.libraryId", notNullValue()));
        }

        @Test
        @DisplayName("Should include field-specific error messages")
        void validationException_includesFieldSpecificErrorMessages() throws Exception {
            // Arrange - send JSON with invalid email
            String invalidEmailJson = """
                    {
                        "isbn": "9780000000001",
                        "title": "Valid Title",
                        "author": "Valid Author",
                        "pages": -5,
                        "libraryId": 1
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidEmailJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.pages", notNullValue()));
        }

        @Test
        @DisplayName("Should include message explaining validation failure")
        void validationException_includesValidationFailureMessage() throws Exception {
            // Arrange
            String invalidJson = """
                    {
                        "isbn": "invalid",
                        "title": "Test",
                        "author": "Test",
                        "libraryId": 1
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", notNullValue()));
        }
    }

    @Nested
    @DisplayName("Generic Exception Handler Tests")
    class GenericExceptionTests {

        @Test
        @DisplayName("Should map generic Exception to 500")
        void genericException_mapsTo500() throws Exception {
            // Arrange
            when(bookService.getBookById(anyLong()))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            // Act & Assert
            mockMvc.perform(get("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status", is(500)))
                    .andExpect(jsonPath("$.error", is("Internal Server Error")));
        }

        @Test
        @DisplayName("Should include path in 500 error response")
        void genericException_includesPath() throws Exception {
            // Arrange
            when(bookService.getBookById(anyLong()))
                    .thenThrow(new RuntimeException("System failure"));

            // Act & Assert
            mockMvc.perform(get("/api/books/123")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.path", is("/api/books/123")));
        }
    }

    @Nested
    @DisplayName("Error Response Structure Tests")
    class ErrorResponseStructureTests {

        @Test
        @DisplayName("Should have consistent error response structure across all exception types")
        void errorResponse_hasConsistentStructure() throws Exception {
            // Test 404 structure
            when(bookService.getBookById(1L))
                    .thenThrow(new ResourceNotFoundException("Book", 1L));

            mockMvc.perform(get("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").exists());
        }

        @Test
        @DisplayName("Should format timestamp correctly")
        void errorResponse_formatsTimestampCorrectly() throws Exception {
            // Arrange
            when(bookService.getBookById(anyLong()))
                    .thenThrow(new ResourceNotFoundException("Book", 1L));

            // Act & Assert
            mockMvc.perform(get("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")));
        }
    }
}
