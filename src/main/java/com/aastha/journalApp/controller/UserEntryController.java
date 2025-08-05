package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@Tag(name = "03. User Dashboard", description = "View & Update Profile")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserEntryController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get User Profile", description = "Get logged-in user's profile data")
    @GetMapping
    public ResponseEntity<User> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Fetching profile for user: {}", username);
        User user = userService.findByUserName(username);

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }

        log.warn("User not found: {}", username);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Update User Profile", description = "Allows logged-in user to update their own profile")
    @PutMapping("/{username}")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody User user, @PathVariable String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        log.info("User {} attempting to update profile of {}", currentUser, username);

        if (!username.equals(currentUser)) {
            log.warn("User {} attempted to update another user's data: {}", currentUser, username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "message", "You can only update your own profile"
                    ));
        }

        try {
            User updatedUser = userService.updateUser(user);
            log.debug("Updated user details for: {}", username);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", updatedUser
            ));
        } catch (RuntimeException e) {
            log.error("Failed to update user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Unexpected error updating profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Internal server error"
                    ));
        }
    }

    @Operation(summary = "Check username availability", description = "Check if a username is available")
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(
            @RequestParam String username) {
        boolean available = userService.findByUserName(username) == null;
        return ResponseEntity.ok(Collections.singletonMap("available", available));
    }
}