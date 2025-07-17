package com.aastha.journalApp.service;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void saveEntry(User user) {
        log.info("Saving user with username: {}", user.getUsername());

        // Password encoding check
        if (!user.getPassword().startsWith("$2a$")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            log.debug("Password encoded for user: {}", user.getUsername());
        }

        // Set default USER role
        user.setRoles(Arrays.asList("USER"));
        userRepository.save(user);
        log.debug("User '{}' saved with USER role", user.getUsername());
    }

    public List<User> getEntry() {
        log.info("Fetching all users (admin access likely)");
        return userRepository.findAll();
    }

    public User findByUserName(String username) {
        log.info("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public void saveAdmin(User user) {
        log.info("Saving admin user: {}", user.getUsername());

        // Password encoding
        if (!user.getPassword().startsWith("$2a$")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            log.debug("Password encoded for admin user: {}", user.getUsername());
        }

        // Set ADMIN role
        user.setRoles(Arrays.asList("ADMIN"));
        userRepository.save(user);
        log.debug("Admin '{}' saved successfully", user.getUsername());
    }

    public void sendInactivityReminderToUsers() {
        List<User> inactiveUsers = userRepository.findUsersWithNoJournalInLastNDays(7);
        // Later step: send email to each user
    }
}