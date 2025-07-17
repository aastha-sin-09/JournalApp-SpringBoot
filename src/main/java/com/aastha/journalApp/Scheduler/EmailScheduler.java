package com.aastha.journalApp.Scheduler;

import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.UserRepositoryImpl;
import com.aastha.journalApp.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EmailScheduler {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Scheduled(cron = "0 0 10 * * ?")
    public void fetchUsersAndSendMail(){
        log.info("🔔 Scheduled task triggered at {}", java.time.LocalTime.now());
        log.info("Schedular triggered to check inactive users");

        List<User> inactiveUsers = userRepository.findUsersWithNoJournalInLastNDays(7);

        for(User user : inactiveUsers){
            if(user.getEmail() == null){
                log.warn("⚠️ Skipping user {} because email is null", user.getUsername());
                continue;
            }

            String to = user.getEmail();
            String subject = "We miss you on Journal App!";
            String body = """
        <html>
            <body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #007BFF;">Hey %s 👋</h2>
                <p>We noticed it's been <strong>a week</strong> since you last penned down your thoughts on <em>Journal App</em>.</p>
                <p>Remember, even a few words can bring you <strong>clarity, calmness, and control</strong> over your day.</p>
                
                <blockquote style="border-left: 4px solid #ccc; padding-left: 10px; color: #666;">
                    “The act of writing is the act of discovering what you believe.” – David Hare
                </blockquote>
                
                <p>So why not take a minute, open the app, and just write how you're feeling?</p>
                
                <a href="https://journalapp.com/write" style="display: inline-block; background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;">Start Writing Now</a>

                <p style="margin-top: 20px;">We’re here for you,<br><strong>Team Journal App 💙</strong></p>
            </body>
        </html>
        """.formatted(user.getUsername());

            emailService.sendEmail(to,subject,body);
        }

        log.info("Reminder emails sent to {} inactive users", inactiveUsers.size());
    }

}
