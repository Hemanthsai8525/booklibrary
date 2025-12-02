package com.example.book.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.book.model.User;
import com.example.book.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository repo;

	public UserService(UserRepository repo) {
		this.repo = repo;
	}
	
	public List<User> findAll(){
		return repo.findAll();
	}

	public User register(User user) {

	    if (repo.existsByEmail(user.getEmail())) {
	        throw new RuntimeException("Email already registered");
	    }

	    if (repo.existsByPhone(user.getPhone())) {
	        throw new RuntimeException("Phone number already registered");
	    }

	    return repo.save(user);
	}


	public User login(String input, String password) {

	    
	    User byEmail = repo.findByEmail(input);
	    if (byEmail != null) {
	        if (byEmail.getPassword().equals(password)) return byEmail;
	        throw new RuntimeException("Invalid password");
	    }

	    
	    User byPhone = repo.findByPhone(input);
	    if (byPhone != null) {
	        if (byPhone.getPassword().equals(password)) return byPhone;
	        throw new RuntimeException("Invalid password");
	    }

	    
	    List<User> list = repo.findByUsername(input);
	    if (!list.isEmpty()) {

	        
	        for (User u : list) {
	            if (u.getPassword().equals(password)) {
	                return u;  
	            }
	        }

	        throw new RuntimeException("Invalid password");
	    }

	    throw new RuntimeException("User not found");
	}
	
	public User updateProfile(Long id, User updated) {

	    User user = repo.findById(id)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    
	    if (updated.getEmail() == null || !updated.getEmail().endsWith("@gmail.com")) {
	        throw new RuntimeException("Email must be a valid @gmail.com address");
	    }

	    
	    if (updated.getPhone() == null || !updated.getPhone().matches("\\d{10}")) {
	        throw new RuntimeException("Phone must be 10 digits");
	    }

	    
	    user.setUsername(updated.getUsername());
	    user.setEmail(updated.getEmail());
	    user.setPhone(updated.getPhone());
	    user.setAddress(updated.getAddress());

	    
	    if (updated.getPassword() != null && !updated.getPassword().trim().isEmpty()) {
	        user.setPassword(updated.getPassword());
	    }

	    return repo.save(user);
	}
	
	public Optional<User> findById(Long id) {
	    return repo.findById(id);
	}



	public void delete(Long id) {
	    repo.deleteById(id);
	}

}
