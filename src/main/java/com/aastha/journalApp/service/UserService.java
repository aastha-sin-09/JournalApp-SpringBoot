package com.aastha.journalApp.service;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        // Check if user already exists
        if (userRepository.findByUsername(user.getUsername()) != null) {
            log.warn("Signup failed: Username '{}' already exists", user.getUsername());
            throw new RuntimeException("Username already exists");
        }

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

        if (userRepository.findByUsername(user.getUsername()) != null) {
            log.warn("Admin creation failed: Username '{}' already exists", user.getUsername());
            throw new RuntimeException("Admin username already exists");
        }

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
    }

    public User updateUser(User user) {
        log.info("Updating user with data: {}", user);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        log.info("Current authenticated user: {}", currentUsername);

        // Find the existing user by current authenticated username
        User existingUser = userRepository.findByUsername(currentUsername);
        if (existingUser == null) {
            log.warn("User not found: {}", currentUsername);
            throw new RuntimeException("User not found");
        }

        // Password update logic
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            if (user.getCurrentPassword() == null ||
                    !passwordEncoder.matches(user.getCurrentPassword(), existingUser.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Username update logic - only update if new username is different and not empty
        if (!user.getUsername().isBlank() && !user.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.findByUsername(user.getUsername()) != null) {
                throw new RuntimeException("Username already taken");
            }
            existingUser.setUsername(user.getUsername());
        }

        // Profile picture update
        if (user.getProfilePicture() != null && !user.getProfilePicture().isBlank()) {
            existingUser.setProfilePicture(user.getProfilePicture());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Updated user: {}", updatedUser);
        return updatedUser;
    }
}