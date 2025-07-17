package com.aastha.journalApp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTests {
    @Autowired
    private EmailService emailService;

    @Test
    void testSendMail(){
        emailService.sendEmail("fs11and12@gmail.com",
                "first email through SpringBoot Applictaion",
                "Hey! how do you feel after sending first email through SpringBoot Applictaion");
    }
}
