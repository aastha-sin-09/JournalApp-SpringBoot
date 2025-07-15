package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserEntryController {

    @Autowired
    private UserService userService;

    @PostMapping
    public void createUser(@RequestBody User user) {
        log.info("Creating user: {}", user.getUsername());
        userService.saveEntry(user);
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        log.info("User {} attempting to update profile of {}", currentUser, username);

        if (!username.equals(currentUser)) {
            log.warn("User {} attempted to update another user's data: {}", currentUser, username);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // 🔒 Only allow self-update
        }

        User userInDB = userService.findByUserName(username);
        if (userInDB != null) {
            log.debug("Found user in DB: {}", username);

            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                userInDB.setUsername(user.getUsername());
            }
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                userInDB.setPassword(user.getPassword());
            }

            userService.saveEntry(userInDB);
            log.debug("Updated user details for: {}", username);
            return new ResponseEntity<>(userInDB, HttpStatus.OK);
        }

        log.warn("User with username {} not found in DB", username);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}