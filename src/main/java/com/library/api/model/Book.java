package com.library.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Book Entity - Child Entity in One-to-Many relationship with Library
 * Represents a book that belongs to a library
 */
@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "author", nullable = false, length = 255)
    private String author;

    @Column(name = "genre", length = 100)
    private String genre;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "pages")
    private Integer pages;

    /**
     * Many-to-One Relationship: Many Books belong to One Library
     * - fetch: EAGER loading to avoid LazyInitializationException
     * - optional: false means every book MUST have a library
     * - Foreign key creates the relationship
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "library_id", nullable = false, foreignKey = @ForeignKey(name = "fk_book_library"))
    private Library library;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Override toString to avoid infinite recursion with bidirectional relationship
     * (Library has books, Book has library)
     */
    @Override
    public String toString() {
        return """
                Book{id=%d, isbn='%s', title='%s', author='%s', genre='%s', publicationDate=%s, acquisitionDate=%s, pages=%d, libraryId=%d}
                """.formatted(id, isbn, title, author, genre, publicationDate, acquisitionDate, pages, library != null ? library.getId() : null);
    }
}
