package com.example.eventhub.repository;

import com.example.eventhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Find user by username
    Optional<User> findByEmail(String email); // Find user by email

}