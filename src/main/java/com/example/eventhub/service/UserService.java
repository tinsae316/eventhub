package com.example.eventhub.service;

import com.example.eventhub.model.User;
import com.example.eventhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class UserService {

    @Autowired // Autowire the UserRepository
    private UserRepository userRepository;

    public User registerUser(User user) {
        return userRepository.save(user); // Use userRepository to save the user
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username); // Use userRepository to find a user by username
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email); // Use userRepository to find a user by email
    }
}