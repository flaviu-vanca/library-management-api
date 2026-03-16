package com.library.api.service;

import com.library.api.dto.request.CreateLibraryRequest;
import com.library.api.dto.request.UpdateLibraryRequest;
import com.library.api.dto.response.BookResponse;
import com.library.api.dto.response.LibraryResponse;
import com.library.api.dto.response.PageResponse;
import com.library.api.exception.ResourceNotFoundException;
import com.library.api.mapper.BookMapper;
import com.library.api.mapper.LibraryMapper;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LibraryServiceImpl.
 * Uses Mockito only — NO Spring context loaded.
 */
@ExtendWith(MockitoExtension.class)
class LibraryServiceImplTest {

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LibraryMapper libraryMapper;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    private Library testLibrary;
    private LibraryResponse testLibraryResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testLibrary = TestDataFactory.createLibrary(1L, "Central Library");
        testLibraryResponse = TestDataFactory.createLibraryResponse(1L, "Central Library");
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("getAllLibraries Tests")
    class GetAllLibrariesTests {

        @Test
        @DisplayName("Should return PageResponse with libraries")
        void getAllLibraries_returnsPageResponse() {
            // Arrange
            List<Library> libraries = List.of(testLibrary);
            Page<Library> libraryPage = new PageImpl<>(libraries, pageable, libraries.size());
            when(libraryRepository.findAll(pageable)).thenReturn(libraryPage);
            when(libraryMapper.toResponse(testLibrary)).thenReturn(testLibraryResponse);

            // Act
            PageResponse<LibraryResponse> result = libraryService.getAllLibraries(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Central Library");
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(libraryRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return empty PageResponse when no libraries exist")
        void getAllLibraries_returnsEmptyPageWhenNoLibrariesExist() {
            // Arrange
            Page<Library> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(libraryRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            PageResponse<LibraryResponse> result = libraryService.getAllLibraries(pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getLibraryById Tests")
    class GetLibraryByIdTests {

        @Test
        @DisplayName("Should return LibraryResponse for valid id")
        void getLibraryById_returnsLibraryResponse() {
            // Arrange
            when(libraryRepository.findById(1L)).thenReturn(Optional.of(testLibrary));
            when(libraryMapper.toResponse(testLibrary)).thenReturn(testLibraryResponse);

            // Act
            LibraryResponse result = libraryService.getLibraryById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Central Library");
            verify(libraryRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for missing id")
        void getLibraryById_throwsResourceNotFoundExceptionForMissingId() {
            // Arrange
            when(libraryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> libraryService.getLibraryById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Library")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("createLibrary Tests")
    class CreateLibraryTests {

        @Test
        @DisplayName("Should save and return LibraryResponse")
        void createLibrary_savesAndReturnsLibraryResponse() {
            // Arrange
            CreateLibraryRequest request = TestDataFactory.createCreateLibraryRequest("New Library");
            Library newLibrary = TestDataFactory.createLibrary(2L, "New Library");
            LibraryResponse expectedResponse = TestDataFactory.createLibraryResponse(2L, "New Library");

            when(libraryMapper.toEntity(request)).thenReturn(newLibrary);
            when(libraryRepository.save(any(Library.class))).thenReturn(newLibrary);
            when(libraryMapper.toResponse(newLibrary)).thenReturn(expectedResponse);

            // Act
            LibraryResponse result = libraryService.createLibrary(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Library");
            verify(libraryRepository).save(any(Library.class));
        }
    }

    @Nested
    @DisplayName("updateLibrary Tests")
    class UpdateLibraryTests {

        @Test
        @DisplayName("Should update and return LibraryResponse")
        void updateLibrary_updatesAndReturnsLibraryResponse() {
            // Arrange
            UpdateLibraryRequest request = TestDataFactory.createUpdateLibraryRequest("Updated Library");
            LibraryResponse updatedResponse = TestDataFactory.createLibraryResponse(1L, "Updated Library");

            when(libraryRepository.findById(1L)).thenReturn(Optional.of(testLibrary));
            when(libraryRepository.save(any(Library.class))).thenReturn(testLibrary);
            when(libraryMapper.toResponse(any(Library.class))).thenReturn(updatedResponse);

            // Act
            LibraryResponse result = libraryService.updateLibrary(1L, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Library");
            verify(libraryMapper).updateEntity(any(Library.class), eq(request));
            verify(libraryRepository).save(any(Library.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for missing id")
        void updateLibrary_throwsResourceNotFoundExceptionForMissingId() {
            // Arrange
            UpdateLibraryRequest request = TestDataFactory.createUpdateLibraryRequest("Updated Library");
            when(libraryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> libraryService.updateLibrary(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Library");

            verify(libraryRepository, never()).save(any(Library.class));
        }
    }

    @Nested
    @DisplayName("deleteLibrary Tests")
    class DeleteLibraryTests {

        @Test
        @DisplayName("Should call libraryRepository.delete() for valid id")
        void deleteLibrary_callsRepositoryDelete() {
            // Arrange
            when(libraryRepository.findById(1L)).thenReturn(Optional.of(testLibrary));

            // Act
            libraryService.deleteLibrary(1L);

            // Assert
            verify(libraryRepository).delete(testLibrary);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for missing id")
        void deleteLibrary_throwsResourceNotFoundExceptionForMissingId() {
            // Arrange
            when(libraryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> libraryService.deleteLibrary(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Library");

            verify(libraryRepository, never()).delete(any(Library.class));
        }
    }

    @Nested
    @DisplayName("getBooksByLibraryId Tests")
    class GetBooksByLibraryIdTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when library doesn't exist")
        void getBooksByLibraryId_throwsResourceNotFoundExceptionWhenLibraryNotExists() {
            // Arrange
            when(libraryRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> libraryService.getBooksByLibraryId(999L, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Library");
        }

        @Test
        @DisplayName("Should return paged books for valid libraryId")
        void getBooksByLibraryId_returnsPagedBooksForValidLibraryId() {
            // Arrange
            Book testBook = TestDataFactory.createBook(1L, "9780000000001", testLibrary);
            BookResponse testBookResponse = TestDataFactory.createBookResponse(1L, "9780000000001");
            List<Book> books = List.of(testBook);
            Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

            when(libraryRepository.existsById(1L)).thenReturn(true);
            when(bookRepository.findByLibraryId(1L, pageable)).thenReturn(bookPage);
            when(bookMapper.toResponse(testBook)).thenReturn(testBookResponse);

            // Act
            PageResponse<BookResponse> result = libraryService.getBooksByLibraryId(1L, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getIsbn()).isEqualTo("9780000000001");
            verify(bookRepository).findByLibraryId(1L, pageable);
        }

        @Test
        @DisplayName("Should return empty page when library has no books")
        void getBooksByLibraryId_returnsEmptyPageWhenLibraryHasNoBooks() {
            // Arrange
            Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(libraryRepository.existsById(1L)).thenReturn(true);
            when(bookRepository.findByLibraryId(1L, pageable)).thenReturn(emptyPage);

            // Act
            PageResponse<BookResponse> result = libraryService.getBooksByLibraryId(1L, pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
        }
    }
}
