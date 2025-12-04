package com.example.book.mapper;

import com.example.book.dto.UserDto;
import com.example.book.model.User;

public class UserMapper {
	
	 public static UserDto toDto(User user) {
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
}
