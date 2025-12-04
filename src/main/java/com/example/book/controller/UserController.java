package com.example.book.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.example.book.dto.UserDto;
import com.example.book.mapper.UserMapper;
import com.example.book.model.User;
import com.example.book.service.JwtService;
import com.example.book.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // -------------------- List users (ADMIN only) --------------------
    @GetMapping
    public ResponseEntity<?> all(Authentication auth) {
        // SecurityConfig already restricts this endpoint to ADMIN.
        List<User> users = userService.findAll();
        // map to DTOs
        List<UserDto> dtoList = users.stream().map(UserMapper::toDto).toList();
        return ResponseEntity.ok(dtoList);
    }

    // -------------------- Register --------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User saved = userService.register(user);
            return ResponseEntity.ok(UserMapper.toDto(saved));
        } catch (RuntimeException ex) {
            log.warn("Register failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // -------------------- Login --------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String input = body.get("input");
            String password = body.get("password");
            if (input == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "input and password required"));
            }
            Map<String, Object> resp = userService.login(input, password);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException ex) {
            log.warn("Login failed: {}", ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
        }
    }

    // -------------------- Refresh --------------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        try {
            String refreshToken = body.get("refreshToken");
            if (refreshToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "refreshToken required"));
            }
            // static helper returns subject (username) if valid
            String username = JwtService.validateRefreshToken(refreshToken);
            if (username == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Invalid refresh token"));
            }

            // find user and issue new access token
            User user = userService.findByUsernameSingle(username);
            if (user == null) return ResponseEntity.status(403).body(Map.of("error", "Invalid refresh token"));

            String newAccess = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(Map.of("accessToken", newAccess));
        } catch (Exception ex) {
            log.warn("Refresh token failed: {}", ex.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", "Invalid refresh token"));
        }
    }

    // -------------------- Me --------------------
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth, HttpServletRequest request) {
        // If authenticated, auth.getName() will be the username (as set by JwtFilter)
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
        }
        String username = auth.getName();
        User user = userService.findByUsernameSingle(username);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        return ResponseEntity.ok(UserMapper.toDto(user));
    }

    // -------------------- Get user by id --------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id, Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
            }
            String loggedName = auth.getName();
            User loggedUser = userService.findByUsernameSingle(loggedName);
            if (loggedUser == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));

            User target = userService.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

            // If not admin, only allow self
            if (!"ADMIN".equalsIgnoreCase(loggedUser.getRole())) {
                if (!loggedUser.getId().equals(id)) {
                    return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
                }
            }
            return ResponseEntity.ok(UserMapper.toDto(target));
        } catch (RuntimeException ex) {
            log.warn("Get user failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // -------------------- Update profile --------------------
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                           @RequestBody User updated,
                                           Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));
            }
            String loggedName = auth.getName();
            User loggedUser = userService.findByUsernameSingle(loggedName);
            if (loggedUser == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthenticated"));

            // Non-admins can only update their own profile
            if (!"ADMIN".equalsIgnoreCase(loggedUser.getRole())) {
                if (!loggedUser.getId().equals(id)) {
                    return ResponseEntity.status(403).body(Map.of("error", "You can update only your own profile"));
                }
            }

            User saved = userService.updateProfile(id, updated);

            // Issue a new access token if username or role changed (safe to always issue)
            String newToken = jwtService.generateToken(saved.getId(), saved.getUsername(), saved.getRole());

            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated",
                    "user", UserMapper.toDto(saved),
                    "accessToken", newToken
            ));
        } catch (RuntimeException ex) {
            log.warn("Update profile failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // -------------------- Delete user (ADMIN) --------------------
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

        userService.delete(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
