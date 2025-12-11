package com.example.book.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.book.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByUserId(Long userId);
	Optional<Order> findFirstByUserId(Long userId);
	  Optional<Order> findById(Long id);
	List<Order> findByAssignedAgentId(Long agentId);
	List<Order> findByStatusAndAssignedAgentIsNull(String status);

}