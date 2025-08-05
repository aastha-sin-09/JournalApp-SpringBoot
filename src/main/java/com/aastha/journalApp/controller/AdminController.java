package com.aastha.journalApp.controller;

import com.aastha.journalApp.dto.UserLogin;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserDetailsServiceImpl;
import com.aastha.journalApp.service.UserService;
import com.aastha.journalApp.util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Tag(name = "04. Admin Tools", description = "Admin-only operations")
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

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

    @Operation(summary = "Admin Login", description = "Login as admin and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> loginAsAdmin(@RequestBody UserLogin userLogin) {
        try {
            log.info("Admin login attempt: {}", userLogin.getUsername());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLogin.getUsername(),
                            userLogin.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getUsername());

            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                log.warn("User {} attempted admin login without ADMIN role", userLogin.getUsername());
                return new ResponseEntity<>("Access Denied: Not an admin", HttpStatus.FORBIDDEN);
            }

            String jwt = jwtUtil.generateToken(userDetails.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            response.put("message", "Admin login successful");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.warn("Admin login failed for username: {}. Reason: {}", userLogin.getUsername(), e.getMessage());
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.UNAUTHORIZED);
        }
    }
}
