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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:63342")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private static final String IMAGE_UPLOAD_DIR = "src/main/resources/static/uploads/images/";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @GetMapping("/all")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/age-group/{ageGroup}")
    public ResponseEntity<?> getEventsByAgeGroup(@PathVariable String ageGroup) {
        try {
            if (!ageGroup.equals("under18") && !ageGroup.equals("18plus")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid age group. Use 'under18' or '18plus'"));
            }
            List<Event> events = eventService.getEventsByAgeGroup(ageGroup);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error fetching events: " + e.getMessage()));
        }
    }

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

    @PostMapping("/admin/add")
    public ResponseEntity<String> addEvent(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String ageGroup,
            @RequestParam int capacity,
            @RequestParam MultipartFile photoUrl) {
        try {
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

            // Generate a unique file name based on the current time
            String fileName = photoUrl.getOriginalFilename();
            Path filePath = Path.of(IMAGE_UPLOAD_DIR + fileName);
            Files.createDirectories(filePath.getParent());
            Files.copy(photoUrl.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Event event = new Event();
            event.setName(name);
            event.setDescription(description);
            event.setAgeGroup(ageGroup);
            event.setCapacity(capacity);
            event.setRegisteredUsers(0);
            event.setPhotoUrl(fileName);

            eventService.saveEvent(event);

            return ResponseEntity.ok("Event added successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image: " + e.getMessage());
        }
    }

    @GetMapping("/uploads/images/{imageName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String imageName) {
        try {
            Path file = Paths.get("src/main/resources/static/uploads/images/" + imageName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String mimeType = Files.probeContentType(file);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mimeType))
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        Optional<Event> eventOptional = eventRepository.findById(id);

        if (eventOptional.isPresent()) {
            eventRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Event not found"));
        }
    }
}
