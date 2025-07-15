package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @GetMapping("/health-check")
    public String healthCheck() {
        logger.info("Health check endpoint hit");
        return "Ok";
    }

    @PostMapping("/create-user")
    public void createUser(@RequestBody User user) {
        logger.info("Creating new user: {}", user.getUsername());
        userService.saveEntry(user);
    }
}