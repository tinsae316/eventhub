package com.example.eventhub.controller;

import com.example.eventhub.model.User;
import com.example.eventhub.repository.UserRepository;
import com.example.eventhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        Optional<User> userOptional = userService.findByUsername(username); // Correct usage of Optional
        return userOptional.orElse(null); // Handle the case where the user is not found
    }

    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        Optional<User> userOptional = userService.findByEmail(email); // Correct usage of Optional
        return userOptional.orElse(null); // Handle the case where the user is not found
    }

    // Signup Endpoint
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        // Check if the username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // Check if the email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the user to the database
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    // Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Find the user by username
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);

        // Check if the user exists and the password matches
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        // Return the login response
        return ResponseEntity.ok(new LoginResponse(user.isAdmin(), "Login successful"));
    }

    // Inner class for login request
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Inner class for login response
    public static class LoginResponse {
        private boolean isAdmin;
        private String message;

        public LoginResponse(boolean isAdmin, String message) {
            this.isAdmin = isAdmin;
            this.message = message;
        }

        // Getters and Setters
        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}