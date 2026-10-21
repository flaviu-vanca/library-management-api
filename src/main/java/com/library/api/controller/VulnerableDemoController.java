package com.library.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vulnerable")
public class VulnerableDemoController {

    private static final Logger log = LoggerFactory.getLogger(VulnerableDemoController.class);

    // ✅ FIX CWE-798: Hardcoded secret (Externalized to properties)
    @Value("${app.api-key:}")
    private String apiKey;

    @Autowired
    private EntityManager entityManager;

    // ✅ FIX CWE-89: SQL Injection (Parameterization)
    @GetMapping("/search")
    public ResponseEntity<List<?>> vulnerableSearch(@RequestParam String q) {
        log.info("Searching for books with title containing: " + q.replaceAll("[\\r\\n]", "_"));
        String sql = "SELECT * FROM book WHERE title LIKE :title";
        List<?> results = entityManager.createNativeQuery(sql)
                .setParameter("title", "%" + q + "%")
                .getResultList();
        return ResponseEntity.ok(results);
    }

    // ✅ FIX CWE-79: XSS (HTML Escaping)
    @GetMapping("/greet")
    public ResponseEntity<Map<String, String>> vulnerableGreet(@RequestParam String name) {
        Map<String, String> response = new HashMap<>();
        String safeName = HtmlUtils.htmlEscape(name);
        // NOTE: In a JSON REST API, XSS is generally not directly exploitable 
        // unless the frontend renders it via innerHTML.
        String html = "<h1>Hello " + safeName + "</h1><p>Welcome</p>";
        response.put("message", html);
        response.put("raw", safeName); 
        return ResponseEntity.ok(response);
    }

    // ✅ FIX CWE-22: Path Traversal (Path validation)
    @GetMapping("/files")
    public ResponseEntity<String> vulnerableFileRead(@RequestParam String filename) {
        try {
            Path base = Paths.get("/tmp/library-files").toAbsolutePath().normalize();
            Path target = base.resolve(filename).normalize().toAbsolutePath();
            
            if (!target.startsWith(base)) {
                return ResponseEntity.badRequest().body("Invalid file path");
            }
            if (!filename.matches("^[a-zA-Z0-9._-]+\\.(txt|pdf|json)$")) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            
            String content = Files.readString(target);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error reading file: " + e.getMessage());
        }
    }

    // ✅ FIX CWE-338: Weak Random (SecureRandom)
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> weakToken() {
        SecureRandom secureRandom = new SecureRandom();
        int tokenInt = secureRandom.nextInt(900000) + 100000;
        String token = String.valueOf(tokenInt);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // ✅ FIX CWE-117: Log Injection (Sanitization)
    @GetMapping("/log")
    public ResponseEntity<String> logInjection(@RequestParam String userInput) {
        String sanitized = userInput.replaceAll("[\\r\\n]", "_");
        log.info("User input: {}", sanitized);
        return ResponseEntity.ok("Logged: " + sanitized);
    }
}
