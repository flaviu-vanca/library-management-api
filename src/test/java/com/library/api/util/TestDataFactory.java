package com.library.api.util;

import com.library.api.dto.request.CreateBookRequest;
import com.library.api.dto.request.CreateLibraryRequest;
import com.library.api.dto.request.UpdateBookRequest;
import com.library.api.dto.request.UpdateLibraryRequest;
import com.library.api.dto.response.BookResponse;
import com.library.api.dto.response.LibraryResponse;
import com.library.api.dto.response.PageResponse;
import com.library.api.model.Book;
import com.library.api.model.Library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data factory providing static methods to create test entities and DTOs.
 * Uses fixed dates (LocalDate.of()) for deterministic tests.
 */
public final class TestDataFactory {

    // Fixed dates for deterministic tests
    public static final LocalDate FIXED_PUBLICATION_DATE = LocalDate.of(2020, 1, 15);
    public static final LocalDate FIXED_ACQUISITION_DATE = LocalDate.of(2020, 6, 1);
    public static final LocalDate FIXED_ESTABLISHED_DATE = LocalDate.of(1990, 5, 20);

    private TestDataFactory() {
        // Prevent instantiation
    }

    // ==================== Library Entity ====================

    public static Library createLibrary(Long id, String name) {
        Library library = new Library();
        library.setId(id);
        library.setName(name);
        library.setAddress("123 Test Street, Test City");
        library.setPhone("+1-555-0100");
        library.setEmail(name.toLowerCase().replace(" ", "") + "@library.org");
        library.setEstablishedDate(FIXED_ESTABLISHED_DATE);
        library.setBooks(new ArrayList<>());
        return library;
    }

    public static Library createLibraryWithBooks(Long id, String name, int bookCount) {
        Library library = createLibrary(id, name);
        for (int i = 1; i <= bookCount; i++) {
            Book book = createBook((long) i, "978000000000" + i, library);
            library.getBooks().add(book);
        }
        return library;
    }

    // ==================== Book Entity ====================

    public static Book createBook(Long id, String isbn, Library library) {
        Book book = new Book();
        book.setId(id);
        book.setIsbn(isbn);
        book.setTitle("Test Book " + id);
        book.setAuthor("Test Author");
        book.setGenre("Fiction");
        book.setPublicationDate(FIXED_PUBLICATION_DATE);
        book.setAcquisitionDate(FIXED_ACQUISITION_DATE);
        book.setPages(300);
        book.setLibrary(library);
        return book;
    }

    public static Book createBookWithDetails(Long id, String isbn, String title,
            String author, String genre, LocalDate publicationDate,
            LocalDate acquisitionDate, Integer pages, Library library) {
        Book book = new Book();
        book.setId(id);
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPublicationDate(publicationDate);
        book.setAcquisitionDate(acquisitionDate);
        book.setPages(pages);
        book.setLibrary(library);
        return book;
    }

    // ==================== BookResponse DTO ====================

    public static BookResponse createBookResponse(Long id, String isbn) {
        return BookResponse.builder()
                .id(id)
                .isbn(isbn)
                .title("Test Book " + id)
                .author("Test Author")
                .genre("Fiction")
                .publicationDate(FIXED_PUBLICATION_DATE)
                .acquisitionDate(FIXED_ACQUISITION_DATE)
                .pages(300)
                .libraryId(1L)
                .libraryName("Test Library")
                .build();
    }

    public static BookResponse createBookResponseWithLibrary(Long id, String isbn,
            Long libraryId, String libraryName) {
        return BookResponse.builder()
                .id(id)
                .isbn(isbn)
                .title("Test Book " + id)
                .author("Test Author")
                .genre("Fiction")
                .publicationDate(FIXED_PUBLICATION_DATE)
                .acquisitionDate(FIXED_ACQUISITION_DATE)
                .pages(300)
                .libraryId(libraryId)
                .libraryName(libraryName)
                .build();
    }

    // ==================== LibraryResponse DTO ====================

    public static LibraryResponse createLibraryResponse(Long id, String name) {
        return LibraryResponse.builder()
                .id(id)
                .name(name)
                .address("123 Test Street, Test City")
                .phone("+1-555-0100")
                .email(name.toLowerCase().replace(" ", "") + "@library.org")
                .establishedDate(FIXED_ESTABLISHED_DATE)
                .totalBooks(0)
                .build();
    }

    public static LibraryResponse createLibraryResponseWithBooks(Long id, String name, int totalBooks) {
        return LibraryResponse.builder()
                .id(id)
                .name(name)
                .address("123 Test Street, Test City")
                .phone("+1-555-0100")
                .email(name.toLowerCase().replace(" ", "") + "@library.org")
                .establishedDate(FIXED_ESTABLISHED_DATE)
                .totalBooks(totalBooks)
                .build();
    }

    // ==================== CreateBookRequest DTO ====================

    public static CreateBookRequest createCreateBookRequest(String isbn, Long libraryId) {
        CreateBookRequest request = new CreateBookRequest();
        request.setIsbn(isbn);
        request.setTitle("New Test Book");
        request.setAuthor("New Author");
        request.setGenre("Fiction");
        request.setPublicationDate(FIXED_PUBLICATION_DATE);
        request.setAcquisitionDate(FIXED_ACQUISITION_DATE);
        request.setPages(250);
        request.setLibraryId(libraryId);
        return request;
    }

    public static CreateBookRequest createCreateBookRequestWithDates(String isbn, Long libraryId,
            LocalDate publicationDate, LocalDate acquisitionDate) {
        CreateBookRequest request = createCreateBookRequest(isbn, libraryId);
        request.setPublicationDate(publicationDate);
        request.setAcquisitionDate(acquisitionDate);
        return request;
    }

    // ==================== UpdateBookRequest DTO ====================

    public static UpdateBookRequest createUpdateBookRequest(String isbn, Long libraryId) {
        UpdateBookRequest request = new UpdateBookRequest();
        request.setIsbn(isbn);
        request.setTitle("Updated Test Book");
        request.setAuthor("Updated Author");
        request.setGenre("Non-Fiction");
        request.setPublicationDate(FIXED_PUBLICATION_DATE);
        request.setAcquisitionDate(FIXED_ACQUISITION_DATE);
        request.setPages(400);
        request.setLibraryId(libraryId);
        return request;
    }

    // ==================== CreateLibraryRequest DTO ====================

    public static CreateLibraryRequest createCreateLibraryRequest(String name) {
        CreateLibraryRequest request = new CreateLibraryRequest();
        request.setName(name);
        request.setAddress("456 New Street, New City");
        request.setPhone("+1-555-0200");
        request.setEmail(name.toLowerCase().replace(" ", "") + "@newlibrary.org");
        request.setEstablishedDate(FIXED_ESTABLISHED_DATE);
        return request;
    }

    // ==================== UpdateLibraryRequest DTO ====================

    public static UpdateLibraryRequest createUpdateLibraryRequest(String name) {
        UpdateLibraryRequest request = new UpdateLibraryRequest();
        request.setName(name);
        request.setAddress("789 Updated Street, Updated City");
        request.setPhone("+1-555-0300");
        request.setEmail(name.toLowerCase().replace(" ", "") + "@updatedlibrary.org");
        request.setEstablishedDate(FIXED_ESTABLISHED_DATE);
        return request;
    }

    // ==================== PageResponse ====================

    public static <T> PageResponse<T> createPageResponse(List<T> items) {
        return PageResponse.<T>builder()
                .content(items)
                .pageNumber(0)
                .pageSize(items.size() > 0 ? items.size() : 10)
                .totalElements(items.size())
                .totalPages(items.size() > 0 ? 1 : 0)
                .first(true)
                .last(true)
                .empty(items.isEmpty())
                .build();
    }

    public static <T> PageResponse<T> createPageResponse(List<T> items, int pageNumber,
            int pageSize, long totalElements, int totalPages) {
        return PageResponse.<T>builder()
                .content(items)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageNumber == 0)
                .last(pageNumber == totalPages - 1)
                .empty(items.isEmpty())
                .build();
    }
}
