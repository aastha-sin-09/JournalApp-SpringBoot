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


@Tag(name = "03. User Dashboard", description = "View & Update Profile")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserEntryController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Update User Profile", description = "Allows logged-in user to update their own profile")
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

            if (!user.getUsername().isBlank()) {
                userInDB.setUsername(user.getUsername());
            }
            if (!user.getPassword().isBlank()) {
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