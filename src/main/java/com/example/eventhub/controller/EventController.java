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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    private final String IMAGE_UPLOAD_DIR = "uploads/images/";

    // Get all events
    @GetMapping("/all")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    // Get events by user's age group
    @GetMapping("/age-group/{ageGroup}")
    public ResponseEntity<?> getEventsByAgeGroup(@PathVariable String ageGroup) {
        try {
            if (!ageGroup.equals("under18") && !ageGroup.equals("18plus")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid age group. Use 'under18' or '18plus'."));
            }
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
            if (request.getName() == null || request.getPhone() == null || request.getEventId() == null || request.getUserId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Name, phone, event ID, and user ID are required"));
            }

            Event event = eventRepository.findById(request.getEventId()).orElse(null);
            if (event == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Event not found"));
            }

            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            if (event.getRegisteredUsers() >= event.getCapacity()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Event is full"));
            }

            if (registrationRepository.findByUserAndEvent(user, event).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "You are already registered for this event"));
            }

            Registration registration = new Registration();
            registration.setName(request.getName());
            registration.setPhone(request.getPhone());
            registration.setEvent(event);
            registration.setUser(user);
            registrationRepository.save(registration);

            event.setRegisteredUsers(event.getRegisteredUsers() + 1);
            eventRepository.save(event);

            return ResponseEntity.ok(Map.of("message", "Registration successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    // Add a new event (Admin only) - Updated to handle image upload
    @PostMapping("/admin/add")
    public ResponseEntity<String> addEvent(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String ageGroup,
            @RequestParam int capacity,
            @RequestParam MultipartFile photoUrl) {
        try {
            // Validate event details
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body("Event name is required");
            }
            if (description == null || description.isEmpty()) {
                return ResponseEntity.badRequest().body("Event description is required");
            }
            if (ageGroup == null || (!ageGroup.equals("under18") && !ageGroup.equals("18plus"))) {
                return ResponseEntity.badRequest().body("Invalid age group. Use 'under18' or '18plus'");
            }
            if (capacity <= 0) {
                return ResponseEntity.badRequest().body("Capacity must be a positive number");
            }

            // Save the image file
            String fileName = System.currentTimeMillis() + "_" + photoUrl.getOriginalFilename();
            Path filePath = Path.of(IMAGE_UPLOAD_DIR + fileName);
            Files.createDirectories(filePath.getParent()); // Create directories if not exist
            Files.copy(photoUrl.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create and save the event
            Event event = new Event();
            event.setName(name);
            event.setDescription(description);
            event.setAgeGroup(ageGroup);
            event.setCapacity(capacity);
            event.setRegisteredUsers(0);
            event.setPhotoUrl(filePath.toString());  // Store the path of the uploaded image

            eventService.saveEvent(event);

            return ResponseEntity.ok("Event added successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image: " + e.getMessage());
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

            eventService.deleteEvent(id);

            return ResponseEntity.ok("Event deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete event: " + e.getMessage());
        }
    }
}
