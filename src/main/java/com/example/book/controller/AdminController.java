package com.example.book.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.book.model.Book;
import com.example.book.model.Order;
import com.example.book.repository.BookRepository;
import com.example.book.repository.OrderRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final BookRepository bookRepo;
    private final OrderRepository orderRepo;

    public AdminController(BookRepository bookRepo, OrderRepository orderRepo) {
        this.bookRepo = bookRepo;
        this.orderRepo = orderRepo;
    }

    // add book
    @PostMapping("/books")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book saved = bookRepo.save(book);
        return ResponseEntity.ok(saved);
    }

    // edit book
    @PutMapping("/books/{id}")
    public ResponseEntity<Book> editBook(@PathVariable Long id, @RequestBody Book book) {
        return bookRepo.findById(id).map(existing -> {
            existing.setTitle(book.getTitle());
            existing.setAuthor(book.getAuthor());
            existing.setPrice(book.getPrice());
            // set other fields as needed
            bookRepo.save(existing);
            return ResponseEntity.ok(existing);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // view all orders
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> allOrders() {
        return ResponseEntity.ok(orderRepo.findAll());
    }
}
