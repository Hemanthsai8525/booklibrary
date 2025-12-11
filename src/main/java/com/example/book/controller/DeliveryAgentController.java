package com.example.book.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.book.dto.DeliveryLoginRequest;
import com.example.book.model.DeliveryAgent;
import com.example.book.model.Order;
import com.example.book.model.User;
import com.example.book.service.DeliveryAgentService;
import com.example.book.service.OrderService;
import com.example.book.service.UserService;

@RestController
@RequestMapping("/delivery")
public class DeliveryAgentController {

	private final DeliveryAgentService svc;
	 private final OrderService Osvc;
	 private final UserService userService;

	public DeliveryAgentController(DeliveryAgentService svc, OrderService osvc, UserService userService) {
		this.svc = svc;
		this.Osvc = osvc;
		this.userService = userService;
	}

	@PostMapping("/register")
	public DeliveryAgent register(@RequestBody DeliveryAgent agent) {
		return svc.register(agent);
	}

	@PostMapping("/login")
	public Map<String, Object> login(@RequestBody DeliveryLoginRequest req) {
		return svc.login(req);
	}

	// Assigned orders to this agent
	@GetMapping("/assigned/{agentId}")
	public List<Order> assigned(@PathVariable Long agentId) {
		return svc.getAssignedOrders(agentId);
	}
	
	@GetMapping("/me/{id}")
	public DeliveryAgent getProfile(@PathVariable Long id) {
	    return svc.getById(id);
	}

	@PutMapping("/update/{id}")
	public DeliveryAgent updateProfile(
	        @PathVariable Long id,
	        @RequestBody DeliveryAgent updated
	) {
	    return svc.updateProfile(id, updated);
	}

	

	// Orders with no agent assigned
	@GetMapping("/available")
	public List<Order> available() {
		return svc.getAvailableOrders();
	}
	
	@GetMapping("/orders/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return Osvc.getOrder(orderId);
    }

	// Take delivery
	@PostMapping("/assign")
	public Order assign(@RequestBody Map<String, Long> body) {
		Long agentId = body.get("agentId");
		Long orderId = body.get("orderId");
		return svc.assignOrder(agentId, orderId);
	}
	
	 @DeleteMapping("/{id}")
	    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
	        // SecurityConfig should restrict delete to admin; double-checking
	        if (auth == null || auth.getName() == null) {
	            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
	        }
	        User loggedUser = userService.findByUsernameSingle(auth.getName());
	        if (loggedUser == null || !"ADMIN".equalsIgnoreCase(loggedUser.getRole())) {
	            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
	        }
	        svc.delete(id);
	        return ResponseEntity.ok(Map.of("message", "User deleted"));
	    }

	// Update status: SHIPPED, DELIVERED, CANCELLED
	@PostMapping("/status/{orderId}/{status}")
	public Order updateStatus(@PathVariable Long orderId, @PathVariable String status) {
		return svc.updateStatus(orderId, status.toUpperCase());
	}
}
