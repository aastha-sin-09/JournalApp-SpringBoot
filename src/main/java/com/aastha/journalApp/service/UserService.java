package com.aastha.journalApp.service;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public void saveEntry(User user){
        if (!user.getPassword().startsWith("$2a$")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }
        user.setRoles(Arrays.asList("USER"));
        userRepository.save(user);
    }

    public List<User> getEntry(){
        return userRepository.findAll();
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveAdmin(User user){
        if (!user.getPassword().startsWith("$2a$")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }
        user.setRoles(Arrays.asList("ADMIN"));
        userRepository.save(user);
    }

}
