package com.library.api.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ⚠️ CONTROLEUR PEDAGOGIQUE VULNERABLE — NE JAMAIS DEPLOYER EN PROD
 * Branche : workshop/vuln-demo uniquement — Ne jamais merger dans main
 * Objectif : Demontrer 5 vulnerabilites pour comparaison SAST
 */
@RestController
@RequestMapping("/api/vuln-demo")
@RequiredArgsConstructor
@Slf4j
public class VulnerableDemoController {

    @PersistenceContext
    private EntityManager entityManager;

    // CWE-798 : Hardcoded Secret — SonarQube java:S6418, Semgrep generic.secrets
    private static final String API_KEY = "STRIPE_SECRET_KEY_PLACEHOLDER"; // NOSONAR
    private static final String DB_PASSWORD = "SuperSecret123!"; // NOSONAR

    /**
     * CWE-89 : SQL Injection via concatenation
     * SonarQube: java:S2077, Semgrep: owasp.java.injection.sql
     */
    @GetMapping("/search")
    public ResponseEntity<List> vulnerableSearch(@RequestParam String q) {
        log.info("VULN-DEMO search: {}", q);
        String sql = "SELECT * FROM book WHERE title LIKE '%" + q + "%'";
        List results = entityManager.createNativeQuery(sql).getResultList();
        return ResponseEntity.ok(results);
    }

    /**
     * CWE-79 : XSS Reflechi — NON EXPLOITABLE en JSON pur
     * A expliquer a l'oral : dangereux uniquement si HTML injecte dans page web
     */
    @GetMapping("/greet")
    public ResponseEntity<Map<String, String>> vulnerableGreet(@RequestParam String name) {
        Map<String, String> response = new HashMap<>();
        String html = "<h1>Hello " + name + "</h1><p>Welcome to library</p>";
        response.put("message", html);
        response.put("raw", name);
        return ResponseEntity.ok(response);
    }

    /**
     * CWE-22 : Path Traversal
     * SonarQube: java:S2083, Semgrep: path-traversal
     */
    @GetMapping("/file")
    public ResponseEntity<String> vulnerableFileRead(@RequestParam String filename) throws IOException {
        Path base = Paths.get("/tmp/library-files");
        Path target = base.resolve(filename).normalize();
        String content = Files.readString(target);
        return ResponseEntity.ok(content);
    }

    /**
     * CWE-338 : Weak PRNG
     * SonarQube: java:S2245
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> weakToken() {
        Random random = new Random();
        String token = String.valueOf(random.nextInt(999999));
        Map<String, String> res = new HashMap<>();
        res.put("token", token);
        return ResponseEntity.ok(res);
    }

    /**
     * CWE-117 : Log Injection
     * SonarQube: java:S5145
     */
    @GetMapping("/log")
    public ResponseEntity<String> logInjection(@RequestParam String userInput) {
        log.info("User input: " + userInput);
        return ResponseEntity.ok("Logged: " + userInput);
    }
}
