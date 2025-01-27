package com.example.eventhub.controller;

import com.example.eventhub.dto.RegistrationRequest;
import com.example.eventhub.model.Event;
import com.example.eventhub.model.Registration;
import com.example.eventhub.repository.EventRepository;
import com.example.eventhub.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.eventhub.model.User;
import com.example.eventhub.repository.UserRepository;

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

    @GetMapping("/all")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

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
            registration.setUser(user); // Set the user
            registrationRepository.save(registration);

            // Update event registration count
            event.setRegisteredUsers(event.getRegisteredUsers() + 1);
            eventRepository.save(event);

            return ResponseEntity.ok().body(Map.of("message", "Registration successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/check-registration")
    public ResponseEntity<Boolean> checkRegistration(@RequestParam String eventId, @RequestParam String phone) {
        try {
            Event event = eventRepository.findById(Long.parseLong(eventId)).orElse(null);
            if (event == null) {
                return ResponseEntity.ok(false);
            }

            boolean isRegistered = registrationRepository.findByPhoneAndEvent(phone, event).isPresent();
            return ResponseEntity.ok(isRegistered);
        } catch (NumberFormatException e) {
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/admin/add")
    public ResponseEntity<String> addEvent(@RequestBody Event event) {
        try {
            event.setRegisteredUsers(0);
            eventRepository.save(event);
            return ResponseEntity.ok("Event added successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add event: " + e.getMessage());
        }
    }


    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        try {
            if (!eventRepository.existsById(id)) {
                return ResponseEntity.badRequest().body("Event not found");
            }
            eventRepository.deleteById(id);
            return ResponseEntity.ok("Event deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete event: " + e.getMessage());
        }
    }
}