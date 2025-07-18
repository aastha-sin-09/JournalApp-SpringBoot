package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. Admin Tools", description = "Admin-only operations")
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired UserService userService;

    @Operation(summary = "Get All Users", description = "Returns a list of all users (admin-only)")
    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers(){
        log.info("Fetching all users (admin access)");
        List<User> all = userService.getEntry();
        if(all != null && !all.isEmpty()){
            log.debug("Total users found: {}", all.size());
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        log.warn("No users found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Create Admin User", description = "Create a new admin user (admin-only)")
    @PostMapping("/create-admin-user")
    public void createUser(@RequestBody User user){
        log.info("Creating admin user: {}", user.getUsername());
        userService.saveAdmin(user);
    }
}
