package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.JournalEntryService;
import com.aastha.journalApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "02. Journal Management", description = "Create, View, Update Journal Entries")
@Slf4j
@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get All Journal Entries", description = "Returns all journal entries for the logged-in user")
    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAllJournalEntriesOfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Fetching journal entries for user: {}", username);
        User user = userService.findByUserName(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }

        List<JournalEntry> allEntries = journalEntryService.findAllByUserId(user.getId());
        if (allEntries != null && !allEntries.isEmpty()) {
            log.debug("Found {} journal entries", allEntries.size());
            return new ResponseEntity<>(allEntries, HttpStatus.OK);
        }

        log.info("No journal entries found for user: {}", username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
    }

    @Operation(summary = "Create Journal Entry", description = "Creates a new journal entry for the logged-in user")
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

    @Operation(summary = "Get Entry By ID", description = "Returns specific journal entry by ID if owned by user")
    @GetMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable ObjectId myId) {
        log.info("Fetching journal entry by ID: {}", myId);
        Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
        if (journalEntry.isPresent()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUserName(username);

            if (!journalEntry.get().getUserId().equals(user.getId())) {
                log.warn("User {} attempted unauthorized access to journal ID {}", username, myId);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Update Entry By ID", description = "Updates a journal entry by ID if owned by user")
    @PutMapping("/id/{id}")
    public ResponseEntity<JournalEntry> updateJournalEntryById(@PathVariable ObjectId id, @RequestBody JournalEntry newEntry) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Attempt to update journal entry ID: {}", id);

        JournalEntry old = journalEntryService.findById(id).orElse(null);
        if (old != null) {
            User user = userService.findByUserName(username);
            if (!old.getUserId().equals(user.getId())) {
                log.warn("User {} attempted unauthorized update to journal ID {}", username, id);
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

    @Operation(summary = "Delete Entry By ID", description = "Deletes a journal entry by ID if owned by user")
    @DeleteMapping("/id/{myId}")
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
}
