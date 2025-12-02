package com.example.book.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.example.book.model.User;
import com.example.book.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService svc;

    public UserController(UserService svc) {
        this.svc = svc;
    }
    
    @GetMapping
    public List<User> all(){
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
        String input = body.get("input");
        String password = body.get("password");
        return ResponseEntity.ok(svc.login(input, password));
    }
    
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody User updated
    ) {
        try {
            return ResponseEntity.ok(svc.updateProfile(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return svc.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }




    
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        svc.delete(id);
        return "User deleted successfully";
    }

}
