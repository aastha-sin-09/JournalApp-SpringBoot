package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
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

    @PostMapping
    public void createUser(@RequestBody User user){
        userService.saveEntry(user);
    }

    @PutMapping("/{username}")
    public ResponseEntity<User> updateUser(@RequestBody User user, @PathVariable String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        if (!username.equals(currentUser)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // 🔒 Only allow self-update
        }

        User userInDB = userService.findByUserName(username);
        if (userInDB != null) {
            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                userInDB.setUsername(user.getUsername());
            }
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                userInDB.setPassword(user.getPassword());
            }
            userService.saveEntry(userInDB);
            return new ResponseEntity<>(userInDB, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


}
