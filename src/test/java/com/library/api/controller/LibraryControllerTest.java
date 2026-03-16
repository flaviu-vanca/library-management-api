package com.library.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.api.dto.request.CreateLibraryRequest;
import com.library.api.dto.request.UpdateLibraryRequest;
import com.library.api.dto.response.BookResponse;
import com.library.api.dto.response.LibraryResponse;
import com.library.api.dto.response.PageResponse;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.service.LibraryService;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for LibraryController.
 * Uses @WebMvcTest — loads only web slice, mocks service layer.
 */
@WebMvcTest(LibraryController.class)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LibraryService libraryService;

    private LibraryResponse testLibraryResponse;
    private PageResponse<LibraryResponse> testPageResponse;

    @BeforeEach
    void setUp() {
        testLibraryResponse = TestDataFactory.createLibraryResponse(1L, "Central Library");
        testPageResponse = TestDataFactory.createPageResponse(List.of(testLibraryResponse));
    }

    @Nested
    @DisplayName("GET /api/libraries Tests")
    class GetAllLibrariesTests {

        @Test
        @DisplayName("Should return 200 with PageResponse body")
        void getAllLibraries_returns200WithPageResponseBody() throws Exception {
            // Arrange
            when(libraryService.getAllLibraries(any(Pageable.class))).thenReturn(testPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/libraries")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Central Library")))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.pageNumber", is(0)));
        }

        @Test
        @DisplayName("Should accept pagination parameters")
        void getAllLibraries_acceptsPaginationParameters() throws Exception {
            // Arrange
            when(libraryService.getAllLibraries(any(Pageable.class))).thenReturn(testPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/libraries")
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "name,desc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(libraryService).getAllLibraries(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/libraries/{id} Tests")
    class GetLibraryByIdTests {

        @Test
        @DisplayName("Should return 200 with LibraryResponse")
        void getLibraryById_returns200() throws Exception {
            // Arrange
            when(libraryService.getLibraryById(1L)).thenReturn(testLibraryResponse);

            // Act & Assert
            mockMvc.perform(get("/api/libraries/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Central Library")));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void getLibraryById_returns404WhenNotFound() throws Exception {
            // Arrange
            when(libraryService.getLibraryById(999L)).thenThrow(new ResourceNotFoundException("Library", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/libraries/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", containsString("Library")));
        }
    }

    @Nested
    @DisplayName("POST /api/libraries Tests")
    class CreateLibraryTests {

        @Test
        @DisplayName("Should return 201 with created LibraryResponse")
        void createLibrary_returns201() throws Exception {
            // Arrange
            CreateLibraryRequest request = TestDataFactory.createCreateLibraryRequest("New Library");
            LibraryResponse createdResponse = TestDataFactory.createLibraryResponse(2L, "New Library");
            when(libraryService.createLibrary(any(CreateLibraryRequest.class))).thenReturn(createdResponse);

            // Act & Assert
            mockMvc.perform(post("/api/libraries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.name", is("New Library")));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void createLibrary_returns400WhenNameIsBlank() throws Exception {
            // Arrange
            CreateLibraryRequest request = TestDataFactory.createCreateLibraryRequest("");

            // Act & Assert
            mockMvc.perform(post("/api/libraries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.validationErrors.name", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void createLibrary_returns400WhenEmailIsInvalid() throws Exception {
            // Arrange
            CreateLibraryRequest request = TestDataFactory.createCreateLibraryRequest("Valid Name");
            request.setEmail("invalid-email-format");

            // Act & Assert
            mockMvc.perform(post("/api/libraries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.email", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 when phone format is invalid")
        void createLibrary_returns400WhenPhoneFormatIsInvalid() throws Exception {
            // Arrange
            CreateLibraryRequest request = TestDataFactory.createCreateLibraryRequest("Valid Name");
            request.setPhone("invalid phone!!!");

            // Act & Assert
            mockMvc.perform(post("/api/libraries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.phone", notNullValue()));
        }
    }

    @Nested
    @DisplayName("PUT /api/libraries/{id} Tests")
    class UpdateLibraryTests {

        @Test
        @DisplayName("Should return 200 with updated LibraryResponse")
        void updateLibrary_returns200() throws Exception {
            // Arrange
            UpdateLibraryRequest request = TestDataFactory.createUpdateLibraryRequest("Updated Library");
            LibraryResponse updatedResponse = TestDataFactory.createLibraryResponse(1L, "Updated Library");
            when(libraryService.updateLibrary(eq(1L), any(UpdateLibraryRequest.class))).thenReturn(updatedResponse);

            // Act & Assert
            mockMvc.perform(put("/api/libraries/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Updated Library")));
        }

        @Test
        @DisplayName("Should return 404 when library not found")
        void updateLibrary_returns404WhenNotFound() throws Exception {
            // Arrange
            UpdateLibraryRequest request = TestDataFactory.createUpdateLibraryRequest("Updated Library");
            when(libraryService.updateLibrary(eq(999L), any(UpdateLibraryRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Library", 999L));

            // Act & Assert
            mockMvc.perform(put("/api/libraries/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when name is blank in update")
        void updateLibrary_returns400WhenNameIsBlank() throws Exception {
            // Arrange
            UpdateLibraryRequest request = TestDataFactory.createUpdateLibraryRequest("");

            // Act & Assert
            mockMvc.perform(put("/api/libraries/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/libraries/{id} Tests")
    class DeleteLibraryTests {

        @Test
        @DisplayName("Should return 204 on successful delete")
        void deleteLibrary_returns204() throws Exception {
            // Arrange
            doNothing().when(libraryService).deleteLibrary(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/libraries/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(libraryService).deleteLibrary(1L);
        }

        @Test
        @DisplayName("Should return 404 when library not found")
        void deleteLibrary_returns404WhenNotFound() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Library", 999L)).when(libraryService).deleteLibrary(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/libraries/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/libraries/{id}/books Tests")
    class GetBooksByLibraryIdTests {

        @Test
        @DisplayName("Should return 200 with paged books")
        void getBooksByLibraryId_returns200WithPagedBooks() throws Exception {
            // Arrange
            BookResponse bookResponse = TestDataFactory.createBookResponse(1L, "9780000000001");
            PageResponse<BookResponse> booksPageResponse = TestDataFactory.createPageResponse(List.of(bookResponse));
            when(libraryService.getBooksByLibraryId(eq(1L), any(Pageable.class))).thenReturn(booksPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/libraries/1/books")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].isbn", is("9780000000001")));
        }

        @Test
        @DisplayName("Should return 404 when library not found")
        void getBooksByLibraryId_returns404WhenLibraryNotFound() throws Exception {
            // Arrange
            when(libraryService.getBooksByLibraryId(eq(999L), any(Pageable.class)))
                    .thenThrow(new ResourceNotFoundException("Library", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/libraries/999/books")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should accept pagination parameters")
        void getBooksByLibraryId_acceptsPaginationParameters() throws Exception {
            // Arrange
            PageResponse<BookResponse> emptyPageResponse = TestDataFactory.createPageResponse(List.of());
            when(libraryService.getBooksByLibraryId(eq(1L), any(Pageable.class))).thenReturn(emptyPageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/libraries/1/books")
                            .param("page", "0")
                            .param("size", "5")
                            .param("sort", "title,asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
