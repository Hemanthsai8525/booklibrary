package com.example.book.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.book.model.User;
import com.example.book.service.JwtService;
import com.example.book.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {
	
	private final String secret = "hfuybiehv7812bjhjhdfhvjkdKJHJsdfghsdfjkhfdV8785485412";

	private final UserService svc;

	private final JwtService jwtService;

	public UserController(UserService svc, JwtService jwtService) {
		this.svc = svc;
		this.jwtService = jwtService;
	}

	
	@GetMapping
	public List<User> all() {
		return svc.findAll();

	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody User user) {
		try {
			User saved = svc.register(user);
			return ResponseEntity.ok(saved);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
	    try {
	        String input = body.get("input");
	        String password = body.get("password");

	        if (input == null || password == null) {
	            return ResponseEntity.badRequest().body("Input + password required");
	        }

	        Map<String, Object> response = svc.login(input, password);
	        System.out.println("response:"+response);

	        return ResponseEntity.ok(response);

	    } catch (RuntimeException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}

	
	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
	    try {
	        String refreshToken = body.get("refreshToken");
	        String username = JwtService.validateRefreshToken(refreshToken);

	        User user = svc.findByUsername(username);
	        if (user == null) {
	            return ResponseEntity.status(403).body("Invalid refresh token");
	        }

	        String newAccess = JwtService.generateToken(username, user.getRole());
	        return ResponseEntity.ok(Map.of("accessToken", newAccess));

	    } catch (Exception e) {
	        return ResponseEntity.status(403).body("Invalid refresh token");
	    }
	}




	@GetMapping("/me")
	public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
		try {
			String token = authHeader.replace("Bearer ", "");
			String username = jwtService.extractUsername(token);
			return ResponseEntity.ok("Token is valid. User: " + username);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Invalid token");
		}
	}

	@PutMapping("/{id}/updateprofile")
	public ResponseEntity<?> updateProfile(@PathVariable Long id,
	                                       @RequestBody User updated,
	                                       HttpServletRequest request) {
	    try {
	        String loggedUser = (String) request.getAttribute("username");
	        String loggedRole = (String) request.getAttribute("role");

	        if (loggedUser == null || loggedRole == null) {
	            return ResponseEntity.status(401).body("Invalid or missing token");
	        }

	        User target = svc.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

	        // USER can update only themselves
	        if (!"ADMIN".equals(loggedRole) && !target.getUsername().equals(loggedUser)) {
	            return ResponseEntity.status(403).body("You cannot update another user’s profile");
	        }

	        User updatedUser = svc.updateProfile(id, updated);

	        // ⭐ IMPORTANT: Generate NEW TOKEN after username change
	        String newToken = JwtService.generateToken(updatedUser.getUsername(), updatedUser.getRole());

	        return ResponseEntity.ok(Map.of(
	                "message", "Profile updated",
	                "user", updatedUser,
	                "token", newToken
	        ));

	    } catch (RuntimeException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}


	@GetMapping("/{id}")
	public ResponseEntity<?> getUser(@PathVariable Long id, HttpServletRequest request) {
		try {
			String loggedUsername = (String) request.getAttribute("username");
			String loggedRole = (String) request.getAttribute("role");

			User target = svc.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

			// USER can only view himself
			if (!"ADMIN".equals(loggedRole) && !loggedUsername.equals(target.getUsername())) {
				return ResponseEntity.status(403).body("You cannot view another user’s profile");
			}

			return ResponseEntity.ok(target);

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
		svc.delete(id);
		return "User deleted successfully";
	}

}