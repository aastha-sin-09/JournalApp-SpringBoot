package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.JournalEntryService;
import com.aastha.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAllJournalEntriesOfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Fetching journal entries for user: {}", username);

        User byUserName = userService.findByUserName(username);

        if (byUserName == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 if user doesn't exist
        }

        List<JournalEntry> allEntries = byUserName.getJournalEntries();
        if (allEntries != null && !allEntries.isEmpty()) {
            log.debug("Found {} journal entries", allEntries.size());
            return new ResponseEntity<>(allEntries, HttpStatus.OK);
        }

        log.info("No journal entries found for user: {}", username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 if user exists but has no entries
    }



    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myEntry) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Creating journal entry for user: {}", username);
            journalEntryService.saveEntry(myEntry, username);
            log.debug("Journal entry created: {}", myEntry.getTitle());
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating journal entry", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("id/{myId}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable ObjectId myId){
        log.info("Fetching journal entry by ID: {}", myId);
        Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
        if (journalEntry.isPresent()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            if (!journalEntry.get().getUser().getUsername().equals(username)) {
                log.warn("User {} attempted unauthorized access to journal ID {}", username, myId);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("id/{myId}")
    public ResponseEntity<Void> deleteJournalEntryById(@PathVariable ObjectId myId) {
        log.info("Attempt to delete journal entry ID: {}", myId);
        boolean deleted = journalEntryService.deleteById(myId);

        if (deleted) {
            log.debug("Journal entry {} deleted", myId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            log.warn("Journal entry {} not found for deletion", myId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping("/id/{id}")
    public ResponseEntity<JournalEntry> updateJournalEntryById(@PathVariable ObjectId id, @RequestBody JournalEntry newEntry){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Attempt to update journal entry ID: {}", id);

        JournalEntry old = journalEntryService.findById(id).orElse(null);
        if (old != null) {
            if (old.getUser() == null || !old.getUser().getUsername().equals(username)) {
                log.warn("User {} attempted unauthorized access to journal ID {}", username, id);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            if (newEntry.getTitle() != null && !newEntry.getTitle().isBlank()) {
                old.setTitle(newEntry.getTitle());
            }
            if (newEntry.getContent() != null && !newEntry.getContent().isBlank()) {
                old.setContent(newEntry.getContent());
            }

            journalEntryService.saveEntry(old);
            log.debug("Updated journal entry: {}", old.getTitle());
            return new ResponseEntity<>(old, HttpStatus.OK);
        }
        log.warn("Journal entry with ID {} not found", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
