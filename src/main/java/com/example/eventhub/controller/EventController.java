package com.example.eventhub.controller;

import com.example.eventhub.dto.RegistrationRequest;
import com.example.eventhub.model.Event;
import com.example.eventhub.model.Registration;
import com.example.eventhub.model.User;
import com.example.eventhub.repository.EventRepository;
import com.example.eventhub.repository.RegistrationRepository;
import com.example.eventhub.repository.UserRepository;
import com.example.eventhub.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:63342") // Allow requests from the frontend
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    // Get all events
    @GetMapping("/all")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    // Get events by user's age group
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEventsByUserAgeGroup(@PathVariable Long userId) {
        try {
            // Fetch the user by ID
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            User user = userOptional.get();

            // Determine the user's age group
            String ageGroup = user.getAge() < 18 ? "under18" : "18plus";

            // Fetch events matching the user's age group
            List<Event> events = eventService.getEventsByAgeGroup(ageGroup);

            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error fetching events: " + e.getMessage()));
        }
    }

    // Register for an event with phone number
    @PostMapping("/register-with-phone")
    public ResponseEntity<?> registerForEventWithPhone(@RequestBody RegistrationRequest request) {
        try {
            // Validate input
            if (request.getName() == null || request.getPhone() == null || request.getEventId() == null || request.getUserId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Name, phone, event ID, and user ID are required"));
            }

            // Fetch the event
            Event event = eventRepository.findById(request.getEventId()).orElse(null);
            if (event == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Event not found"));
            }

            // Fetch the user
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            // Check if the event is full
            if (event.getRegisteredUsers() >= event.getCapacity()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Event is full"));
            }

            // Check if the user is already registered for the event
            if (registrationRepository.findByUserAndEvent(user, event).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "You are already registered for this event"));
            }

            // Create and save the registration
            Registration registration = new Registration();
            registration.setName(request.getName());
            registration.setPhone(request.getPhone());
            registration.setEvent(event);
            registration.setUser(user);
            registrationRepository.save(registration);

            // Update event registration count
            event.setRegisteredUsers(event.getRegisteredUsers() + 1);
            eventRepository.save(event);

            return ResponseEntity.ok(Map.of("message", "Registration successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    // Add a new event (Admin only)
    @PostMapping("/admin/add")
    public ResponseEntity<String> addEvent(@RequestBody Event event) {
        try {
            // Validate event details
            if (event.getName() == null || event.getName().isEmpty()) {
                return ResponseEntity.badRequest().body("Event name is required");
            }
            if (event.getDescription() == null || event.getDescription().isEmpty()) {
                return ResponseEntity.badRequest().body("Event description is required");
            }
            if (event.getAgeGroup() == null ||
                    (!event.getAgeGroup().equals("under18") && !event.getAgeGroup().equals("18plus"))) {
                return ResponseEntity.badRequest().body("Invalid age group. Use 'under18' or '18plus'");
            }
            if (event.getCapacity() <= 0) {
                return ResponseEntity.badRequest().body("Capacity must be a positive number");
            }

            // Initialize registered users
            event.setRegisteredUsers(0);

            // Save the event
            eventService.saveEvent(event);

            return ResponseEntity.ok("Event added successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add event: " + e.getMessage());
        }
    }

    // Delete an event (Admin only)
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        try {
            if (!eventRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Event not found");
            }

            // Delete the event
            eventService.deleteEvent(id);

            return ResponseEntity.ok("Event deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete event: " + e.getMessage());
        }
    }
}
