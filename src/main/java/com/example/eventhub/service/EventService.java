package com.example.eventhub.service;

import com.example.eventhub.model.Event;
import com.example.eventhub.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByAgeGroup(String ageGroup) {
        if (ageGroup.equals("under18")) { // Match database value
            return eventRepository.findByAgeGroup("under18");
        } else if (ageGroup.equals("18plus")) { // Match database value
            return eventRepository.findByAgeGroup("18plus");
        } else {
            return new ArrayList<>();
        }
    }


    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}