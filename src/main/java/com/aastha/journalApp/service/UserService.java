package com.aastha.journalApp.service;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void saveEntry(User user){
        userRepository.save(user);
    }

    public List<User> getEntry(){
        return userRepository.findAll();
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

}
