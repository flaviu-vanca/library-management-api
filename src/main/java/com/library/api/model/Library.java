package com.library.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Library Entity - Parent Entity in One-to-Many relationship with Book
 * Represents a library that can contain multiple books
 */
@Entity
@Table(name = "library")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Library {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "established_date")
    private LocalDate establishedDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * One-to-Many Relationship: One Library can have many Books
     * - mappedBy: Indicates that Book entity owns the relationship
     * - cascade: ALL operations cascade to child entities
     * - orphanRemoval: Removes books that are no longer referenced
     * - fetch: LAZY loading for better performance
     */
    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();

    /**
     * Helper method to add a book to this library
     * Maintains bidirectional relationship consistency
     */
    public void addBook(Book book) {
        books.add(book);
        book.setLibrary(this);
    }

    /**
     * Helper method to remove a book from this library
     * Maintains bidirectional relationship consistency
     */
    public void removeBook(Book book) {
        books.remove(book);
        book.setLibrary(null);
    }

    @Override
    public String toString() {
        return """
                Library{id=%d, name='%s', address='%s', phone='%s', email='%s', establishedDate=%s, createdAt=%s, updatedAt=%s}
                """.formatted(id, name, address, phone, email, establishedDate, createdAt, updatedAt);
    }
}
