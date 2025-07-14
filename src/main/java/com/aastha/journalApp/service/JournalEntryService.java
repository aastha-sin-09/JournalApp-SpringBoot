package com.aastha.journalApp.service;

import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JournalEntryService {
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String username) {
        User byUserName = userService.findByUserName(username);
        journalEntry.setUser(byUserName);  // ✅ Add this line to set user also
        journalEntry.setDate(LocalDateTime.now());
        JournalEntry saved = journalEntryRepository.save(journalEntry);
        byUserName.getJournalEntries().add(saved);
        userService.saveEntry(byUserName);
    }

    public void saveEntry(JournalEntry journalEntry){
        journalEntryRepository.save(journalEntry);
    }
    public List<JournalEntry> getEntry(){
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id){
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUserName(username);

        boolean removed = user.getJournalEntries().removeIf(entry -> entry.getId().equals(id));
        if (removed) {
            userService.saveEntry(user);
            journalEntryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
