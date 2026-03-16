package com.library.api.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Karate test runner that starts the Spring Boot application
 * and runs all .feature files in the karate resources directory.
 *
 * Karate is a BDD-style API testing framework that writes tests
 * in plain English Gherkin syntax, with no Java code needed for
 * the test steps themselves.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KarateRunner {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testAll() {
        // Set the server port as a system property for karate-config.js to use
        System.setProperty("server.port", String.valueOf(port));
        return Karate.run("classpath:karate").relativeTo(getClass());
    }

    @Karate.Test
    Karate testLibraries() {
        System.setProperty("server.port", String.valueOf(port));
        return Karate.run("classpath:karate/libraries.feature").relativeTo(getClass());
    }

    @Karate.Test
    Karate testBooks() {
        System.setProperty("server.port", String.valueOf(port));
        return Karate.run("classpath:karate/books.feature").relativeTo(getClass());
    }
}
