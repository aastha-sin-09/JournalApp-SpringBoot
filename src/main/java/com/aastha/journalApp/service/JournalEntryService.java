package com.aastha.journalApp.service;

import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String username) {
        log.info("Saving new journal entry for user: {}", username);
        User byUserName = userService.findByUserName(username);

        journalEntry.setUser(byUserName);
        journalEntry.setDate(LocalDateTime.now());

        JournalEntry saved = journalEntryRepository.save(journalEntry);
        byUserName.getJournalEntries().add(saved);
        userService.saveEntry(byUserName);

        log.debug("Journal entry '{}' saved successfully", saved.getTitle());
    }

    public void saveEntry(JournalEntry journalEntry) {
        log.info("Updating existing journal entry: {}", journalEntry.getTitle());
        journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> getEntry() {
        log.info("Fetching all journal entries (admin/debug use)");
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id) {
        log.info("Finding journal entry by ID: {}", id);
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Attempting to delete journal entry ID {} for user {}", id, username);

        User user = userService.findByUserName(username);
        boolean removed = user.getJournalEntries().removeIf(entry -> entry.getId().equals(id));

        if (removed) {
            userService.saveEntry(user);
            journalEntryRepository.deleteById(id);
            log.debug("Deleted journal entry ID: {}", id);
            return true;
        }

        log.warn("Journal entry ID {} not found or doesn't belong to user {}", id, username);
        return false;
    }
}