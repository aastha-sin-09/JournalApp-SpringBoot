package com.aastha.journalApp.controller;

import com.aastha.journalApp.dto.UserLogin;
import com.aastha.journalApp.dto.UserSignUp;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.UserDetailsServiceImpl;
import com.aastha.journalApp.service.UserService;
import com.aastha.journalApp.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/public")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;


    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check endpoint hit");
        return new ResponseEntity<>("Journal App is running ✅", HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserSignUp userSignUp) {
        try{
            log.info("Creating new user: {}", userSignUp.getUsername());

            User user = new User();
            user.setEmail(userSignUp.getEmail());
            user.setUsername(userSignUp.getUsername());
            user.setPassword(userSignUp.getPassword());

            userService.saveEntry(user);
            log.info("User saved successfully: {}", userSignUp.getUsername());
            return new ResponseEntity<>("User created", HttpStatus.CREATED);
        }catch(Exception e){
            log.error("Failed to create user: {}. Reason: {}", userSignUp.getUsername(), e.getMessage());
            return new ResponseEntity<>("Signup failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLogin userLogin) {
        try{
            log.info("Logging in user: {}", userLogin.getUsername());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getUsername());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        }catch(Exception e){
            log.warn("Login failed for username: {}. Reason: {}", userLogin.getUsername(), e.getMessage());
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.NOT_FOUND);
        }
    }
}