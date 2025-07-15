package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
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


@RestController
@RequestMapping("/user")
public class UserEntryController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserEntryController.class);

    @PostMapping
    public void createUser(@RequestBody User user) {
        logger.info("Creating user: {}", user.getUsername());
        userService.saveEntry(user);
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        logger.info("User {} attempting to update profile of {}", currentUser, username);

        if (!username.equals(currentUser)) {
            logger.warn("User {} attempted to update another user's data: {}", currentUser, username);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // 🔒 Only allow self-update
        }

        User userInDB = userService.findByUserName(username);
        if (userInDB != null) {
            logger.debug("Found user in DB: {}", username);

            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                userInDB.setUsername(user.getUsername());
            }
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                userInDB.setPassword(user.getPassword());
            }

            userService.saveEntry(userInDB);
            logger.debug("Updated user details for: {}", username);
            return new ResponseEntity<>(userInDB, HttpStatus.OK);
        }

        logger.warn("User with username {} not found in DB", username);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}