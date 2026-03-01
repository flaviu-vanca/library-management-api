package com.library.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new Book
 * Used in POST /api/books
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(97[89])?\\d{10,13}$",
             message = "ISBN must be 10 or 13 digits, optionally starting with 978 or 979")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;

    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;

    @PastOrPresent(message = "Acquisition date cannot be in the future")
    private LocalDate acquisitionDate;

    @Min(value = 1, message = "Pages must be at least 1")
    @Max(value = 10000, message = "Pages must not exceed 10,000")
    private Integer pages;

    @NotNull(message = "Library ID is required")
    @Positive(message = "Library ID must be positive")
    private Long libraryId;
}
