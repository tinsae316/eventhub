package com.example.eventhub.repository;

import com.example.eventhub.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByAgeGroup(String ageGroup); // Add this method
}