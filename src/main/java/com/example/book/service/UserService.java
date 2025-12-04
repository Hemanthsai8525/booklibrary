package com.example.book.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.book.dto.UserDto;
import com.example.book.model.User;
import com.example.book.repository.UserRepository;

@Service
public class UserService {
	
	public UserDto toDto(User user) {
	    if (user == null) return null;

	    return new UserDto(
	        user.getId(),
	        user.getUsername(),
	        user.getEmail(),
	        user.getPhone(),
	        user.getAddress(),
	        user.getRole()
	    );
	}


    private final UserRepository repo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    // ================= REGISTER =================
    public User register(User user) {

        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (repo.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }


    // ============= HELPER: BUILD LOGIN RESPONSE =============
    public Map<String, Object> buildLoginResponse(User user) {

        String token = JwtService.generateToken(user.getUsername(), user.getRole());
        String refresh = JwtService.generateRefreshToken(user.getUsername());

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("email", user.getEmail());
        resp.put("phone", user.getPhone());
        resp.put("role", user.getRole());
        resp.put("address", user.getAddress());
        resp.put("token", token);
        resp.put("refreshToken", refresh);

        return resp;
    }



    // ================= LOGIN (EMAIL + PHONE + USERNAME) =================
    public Map<String, Object> login(String input, String password) {

        System.out.println("LOGIN INPUT = " + input);
        System.out.println("LOGIN PASSWORD = " + password);

        // 1️⃣ LOGIN BY EMAIL
        User user = repo.findByEmail(input);
        if (user != null) {
            System.out.println("Found by email: " + user.getUsername());
            System.out.println("Password match = " + encoder.matches(password, user.getPassword()));

            if (encoder.matches(password, user.getPassword())) {
            	return buildLoginResponse(user);
            }
            throw new RuntimeException("Invalid password");
        }


        // 2️⃣ LOGIN BY PHONE
        user = repo.findByPhone(input);
        if (user != null) {
            System.out.println("Found by phone: " + user.getUsername());
            System.out.println("Password match = " + encoder.matches(password, user.getPassword()));
            if (encoder.matches(password, user.getPassword())) {
                return buildLoginResponse(user);
            }
            throw new RuntimeException("Invalid password");
        }

        // 3️⃣ LOGIN BY USERNAME
        List<User> users = repo.findByUsername(input);
        if (!users.isEmpty()) {
            for (User u : users) {
                System.out.println("Checking username: " + u.getUsername());
                System.out.println("Password match? " + encoder.matches(password, u.getPassword()));

                if (encoder.matches(password, u.getPassword())) {
                    return buildLoginResponse(u);
                }
            }
            throw new RuntimeException("Invalid password");
        }

        throw new RuntimeException("User not found");
    }



    // ================= UPDATE PROFILE =================
    public User updateProfile(Long id, User updated) {

        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // EMAIL VALIDATION + DUPLICATE CHECK
        if (!updated.getEmail().equals(user.getEmail()) &&
                repo.existsByEmail(updated.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // PHONE VALIDATION + DUPLICATE CHECK
        if (!updated.getPhone().equals(user.getPhone()) &&
                repo.existsByPhone(updated.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // USERNAME VALIDATION + DUPLICATE CHECK
        if (!updated.getUsername().equals(user.getUsername())) {
            if (!repo.findByUsername(updated.getUsername()).isEmpty()) {
                throw new RuntimeException("Username already exists");
            }
        }

        user.setUsername(updated.getUsername());
        user.setEmail(updated.getEmail());
        user.setPhone(updated.getPhone());
        user.setAddress(updated.getAddress());

        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            user.setPassword(encoder.encode(updated.getPassword()));
        }

        return repo.save(user);
    }
    
    private Map<String, Object> generateLoginResponse(User user) {
        String accessToken = JwtService.generateToken(user.getUsername(), user.getRole());
        String refreshToken = JwtService.generateRefreshToken(user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole());
        response.put("address", user.getAddress());

        return response;
    }

    


    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

	public User findByUsername(String username) {
		// TODO Auto-generated method stub
		return (User) repo.findByUsername(username);
	}
}