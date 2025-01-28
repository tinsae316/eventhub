package com.example.eventhub.controller;

import com.example.eventhub.model.Event;
import com.example.eventhub.model.User;
import com.example.eventhub.repository.UserRepository;
import com.example.eventhub.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:63342") // Allow requests from the frontend
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventService eventService;

    // Login Endpoint with Age-Based Event Filtering
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Find the user by username
            Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid username or password");
            }

            User user = userOptional.get();

            // Validate password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid username or password");
            }

            // Determine the user's age group
            String ageGroup = user.getAge() < 18 ? "under18" : "18plus";

            // Fetch events for the user's age group
            List<Event> events = eventService.getEventsByAgeGroup(ageGroup);

            // Prepare the login response with the userId
            LoginResponse response = new LoginResponse(user.getId(), user.isAdmin(), ageGroup, "Login successful", events);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    // Signup Endpoint
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            // Check if the username already exists
            Optional<User> existingUser = userRepository.findByUsername(signupRequest.getUsername());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            // Create a new User object
            User newUser = new User();
            newUser.setUsername(signupRequest.getUsername());
            newUser.setEmail(signupRequest.getEmail());
            newUser.setAge(signupRequest.getAge());
            newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword())); // Encrypt the password

            // Save the new user to the database
            userRepository.save(newUser);

            return ResponseEntity.ok("User registered successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Signup failed: " + e.getMessage());
        }
    }

    // Inner class for signup request
    public static class SignupRequest {
        private String username;
        private String password;
        private String email;
        private int age;

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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
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
        private Long userId;
        private boolean isAdmin;
        private String ageGroup;
        private String message;
        private List<Event> events;

        // Constructor
        public LoginResponse(Long userId, boolean isAdmin, String ageGroup, String message, List<Event> events) {
            this.userId = userId;
            this.isAdmin = isAdmin;
            this.ageGroup = ageGroup;
            this.message = message;
            this.events = events;
        }

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }

        public String getAgeGroup() {
            return ageGroup;
        }

        public void setAgeGroup(String ageGroup) {
            this.ageGroup = ageGroup;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Event> getEvents() {
            return events;
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }
    }
}

