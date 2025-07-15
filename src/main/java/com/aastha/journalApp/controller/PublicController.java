package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserService userService;


    @GetMapping("/health-check")
    public String healthCheck() {
        log.info("Health check endpoint hit");
        return "Ok";
    }

    @PostMapping("/create-user")
    public void createUser(@RequestBody User user) {
        log.info("Creating new user: {}", user.getUsername());
        userService.saveEntry(user);
    }
}