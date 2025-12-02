package com.example.book.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.book.model.Book;
import com.example.book.service.BookService;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService svc;

    public BookController(BookService svc) {
        this.svc = svc;
    }

    // ================= GET ALL BOOKS =================
    @GetMapping
    public List<Book> all() {
        return svc.findAll();
    }

    // ================= GET ONE BOOK ==================
    @GetMapping("/{id}")
    public ResponseEntity<Book> get(@PathVariable Long id) {
        return svc.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================= CREATE BOOK ====================
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Book book) {
        try {
            Book saved = svc.save(book);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================= UPDATE BOOK ====================
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book book) {
        return svc.updateBook(id, book);
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<?> createBulk(@RequestBody List<Book> books) {

        List<Book> inserted = new java.util.ArrayList<>();
        List<String> skipped = new java.util.ArrayList<>();

        for (Book b : books) {
            try {
                // Skip duplicates
                if (svc.existsByTitle(b.getTitle())) {
                    skipped.add(b.getTitle());
                    continue;
                }

                inserted.add(svc.save(b));

            } catch (Exception ex) {
                skipped.add(b.getTitle());
            }
        }

        return ResponseEntity.ok(
                Map.of(
                        "insertedCount", inserted.size(),
                        "skippedCount", skipped.size(),
                        "insertedBooks", inserted,
                        "skippedBooks", skipped
                )
        );
    }


    // ================= DELETE BOOK ====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (svc.findById(id).isPresent()) {
            svc.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ================= IMAGE UPLOAD ===================
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file selected");
        }

        try {
            String uploadDir = "uploads/";

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + filename);

            Files.write(path, file.getBytes());

            String fileUrl = "/uploads/" + filename; // This URL will be saved in the book object

            return ResponseEntity.ok(fileUrl);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
