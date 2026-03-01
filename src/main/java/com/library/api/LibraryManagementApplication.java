package com.library.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Class
 * Entry point for the Library Management REST API
 * 
 * @SpringBootApplication includes:
 * - @Configuration: Marks this as a source of bean definitions
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 * - @ComponentScan: Scans for components in this package and sub-packages
 */
@SpringBootApplication
public class LibraryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementApplication.class, args);
        
        System.out.println("""
                \
                ===========================================================
                   Library Management REST API - Successfully Started
                ===========================================================
                   Application is running on: http://localhost:8080
                   H2 Console available at: http://localhost:8080/h2-console
                  \s
                   API Endpoints:
                   - GET    /api/libraries
                   - GET    /api/libraries/{id}
                   - POST   /api/libraries
                   - PUT    /api/libraries/{id}
                   - DELETE /api/libraries/{id}
                   - GET    /api/libraries/{id}/books
                  \s
                   - GET    /api/books
                   - GET    /api/books/{id}
                   - POST   /api/books
                   - PUT    /api/books/{id}
                   - DELETE /api/books/{id}
                   - GET    /api/books/by-publication-date
                   - GET    /api/books/by-acquisition-date
                ===========================================================\
               \s""");
    }
}
