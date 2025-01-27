package com.example.eventhub.repository;

import com.example.eventhub.model.Registration;
import com.example.eventhub.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import com.example.eventhub.model.User;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByPhoneAndEvent(String phone, Event event);
    Optional<Registration> findByUserAndEvent(User user, Event event);
}