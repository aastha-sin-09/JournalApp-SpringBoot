package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(){
        logger.info("Fetching all users (admin access)");
        List<User> all = userService.getEntry();
        if(all != null && !all.isEmpty()){
            logger.debug("Total users found: {}", all.size());
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        logger.warn("No users found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin-user")
    public void createUser(@RequestBody User user){
        logger.info("Creating admin user: {}", user.getUsername());
        userService.saveAdmin(user);
    }
}
