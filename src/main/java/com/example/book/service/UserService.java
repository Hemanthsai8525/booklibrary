package com.example.book.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.book.mapper.UserMapper;
import com.example.book.model.User;
import com.example.book.repository.UserRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repo;
    private final JwtService jwtService;
    // For simplicity keep local encoder; can be replaced by injected PasswordEncoder bean.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    // ---------- Public operations ----------

    public List<User> findAll() {
        return repo.findAll();
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public User findByUsernameSingle(String username) {
        // Your repo might return a list for username; pick first or null.
        // Adjust if your repository exposes a different API.
        List<User> list = repo.findByUsername(username);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public User findByPhone(String phone) {
        return repo.findByPhone(phone);
    }

    // ---------- Registration ----------
    public User register(User user) {
        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (repo.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }
        // encode password
        user.setPassword(encoder.encode(user.getPassword()));
        // Assign default role if not present
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        return repo.save(user);
    }

    // ---------- Login ----------
    /**
     * Login using input (email | phone | username) and password.
     * Returns a map containing userDto, accessToken, refreshToken.
     */
    public Map<String, Object> login(String input, String password) {
        User user = null;

        // 1) by email
        user = repo.findByEmail(input);
        if (user != null) {
            if (encoder.matches(password, user.getPassword())) {
                return buildAuthResponse(user);
            }
            throw new RuntimeException("Invalid password");
        }

        // 2) by phone
        user = repo.findByPhone(input);
        if (user != null) {
            if (encoder.matches(password, user.getPassword())) {
                return buildAuthResponse(user);
            }
            throw new RuntimeException("Invalid password");
        }

        // 3) by username (repo returns list)
        List<User> users = repo.findByUsername(input);
        if (users != null && !users.isEmpty()) {
            for (User u : users) {
                if (encoder.matches(password, u.getPassword())) {
                    return buildAuthResponse(u);
                }
            }
            throw new RuntimeException("Invalid password");
        }

        throw new RuntimeException("User not found");
    }

    private Map<String, Object> buildAuthResponse(User user) {
        // Use instance JwtService to generate tokens including user id
        String access = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getUsername());

        Map<String, Object> resp = new HashMap<>();
        resp.put("user", UserMapper.toDto(user));
        resp.put("accessToken", access);
        resp.put("refreshToken", refresh);
        return resp;
    }

    // ---------- Update profile ----------
    /**
     * Update profile fields for user id. Only safe fields are updated here.
     * Password update (if provided) will be encoded.
     */
    public User updateProfile(Long id, User updated) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Email duplicate check
        if (updated.getEmail() != null && !updated.getEmail().equals(existing.getEmail())) {
            if (repo.existsByEmail(updated.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            existing.setEmail(updated.getEmail());
        }

        // Phone duplicate check
        if (updated.getPhone() != null && !updated.getPhone().equals(existing.getPhone())) {
            if (repo.existsByPhone(updated.getPhone())) {
                throw new RuntimeException("Phone number already exists");
            }
            existing.setPhone(updated.getPhone());
        }

        // Username duplicate check
        if (updated.getUsername() != null && !updated.getUsername().equals(existing.getUsername())) {
            List<User> found = repo.findByUsername(updated.getUsername());
            if (found != null && !found.isEmpty()) {
                throw new RuntimeException("Username already exists");
            }
            existing.setUsername(updated.getUsername());
        }

        if (updated.getAddress() != null) {
            existing.setAddress(updated.getAddress());
        }

        // Password update
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(encoder.encode(updated.getPassword()));
        }

        // Role change should be controlled by admin endpoints only; do NOT change role here.

        return repo.save(existing);
    }

    // ---------- Delete ----------
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
