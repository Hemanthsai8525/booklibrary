package com.example.book.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.book.model.Book;
import com.example.book.model.CartItem;
import com.example.book.model.Order;
import com.example.book.repository.BookRepository;
import com.example.book.repository.CartRepository;
import com.example.book.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository repo;
    private final CartRepository cartRepo;
    private final BookRepository bookRepo;

    public OrderService(OrderRepository repo, CartRepository cartRepo, BookRepository bookRepo) {
        this.repo = repo;
        this.cartRepo = cartRepo;
        this.bookRepo = bookRepo;
    }

    // PLACE ORDER (Correct logic)
    public Order placeOrder(Long userId, String address, String phone) {

        if (userId == null) {
            throw new RuntimeException("Invalid user");
        }

        List<CartItem> items = cartRepo.findByUserIdAndOrderIsNull(userId);

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("No items in cart");
        }

        double total = 0;

        for (CartItem ci : items) {
            Book b = bookRepo.findById(ci.getBookId())
                    .orElseThrow(() -> new RuntimeException("Book not found: " + ci.getBookId()));
            total += b.getPrice() * ci.getQuantity();
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setTotal(total);
        order.setAddress(address);
        order.setPhone(phone);

        Order savedOrder = repo.save(order);

        for (CartItem ci : items) {
            ci.setOrder(savedOrder);
            cartRepo.save(ci);
        }

        return savedOrder;
    }


    // DELETE ORDER
    public void deleteOrder(Long orderId) {
        Order order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        List<CartItem> items = cartRepo.findByOrderId(orderId);
        cartRepo.deleteAll(items);

        repo.delete(order);
    }

    public List<Order> findByUserId(Long userId) {
        List<Order> orders = repo.findByUserId(userId);
        for (Order o : orders) {
            List<CartItem> items = cartRepo.findByOrderId(o.getId());
            o.setItems(items);
        }
        return orders;
    }

    public List<Order> findAll() {
        return repo.findAll();
    }

    public Order getOrder(Long orderId) {
        return repo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

}
