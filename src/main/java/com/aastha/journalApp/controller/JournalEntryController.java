package com.aastha.journalApp.controller;

import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.service.JournalEntryService;
import com.aastha.journalApp.service.UserService;
import com.aastha.journalApp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "02. Journal Management", description = "Create, View, Update Journal Entries")
@Slf4j
@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @Autowired
    JwtUtil jwtUtil;

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

            if (myEntry.getPhotos() == null) {
                myEntry.setPhotos(new ArrayList<>());
            }

            if (myEntry.getDate() == null) {
                myEntry.setDate(LocalDateTime.now(ZoneOffset.UTC));
            }

            // Debug log to verify photos
            log.info("Creating entry with {} photos", myEntry.getPhotos().size());
            JournalEntry savedEntry = journalEntryService.saveEntry(myEntry, username);

            log.debug("Journal entry created: {}", savedEntry.getId());
            log.debug("Journal entry created: {}", myEntry.getTitle());
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating journal entry", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get Entry By ID", description = "Returns specific journal entry by ID if owned by user")
    @GetMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable String myId) {
        log.info("Fetching journal entry by ID: {}", myId);

        try {
            Optional<JournalEntry> journalEntryOpt = journalEntryService.findById(myId);
            if (journalEntryOpt.isEmpty()) {
                log.warn("No journal entry found with ID: {}", myId);
                return ResponseEntity.notFound().build();
            }

            JournalEntry journalEntry = journalEntryOpt.get();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) {
                log.warn("Authentication failed or username not found in context.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String username = auth.getName();
            User user = userService.findByUserName(username);
            if (user == null || !journalEntry.getUserId().equals(user.getId())) {
                log.warn("Unauthorized access by user '{}' for journal ID {}", username, myId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            log.info("Journal entry {} fetched successfully for user {}", myId, username);
            return ResponseEntity.ok(journalEntry);
        } catch (Exception e) {
            log.error("Error while fetching journal entry ID {}: {}", myId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Update Entry By ID", description = "Updates a journal entry by ID if owned by user")
    @PutMapping("/id/{id}")
    public ResponseEntity<Map<String, Object>> updateJournalEntryById(
            @PathVariable String id,
            @RequestBody JournalEntry newEntry) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Attempt to update journal entry ID: {}", id);

        JournalEntry oldEntry = journalEntryService.findById(id).orElse(null);
        if (oldEntry != null) {
            User user = userService.findByUserName(username);
            if (!oldEntry.getUserId().equals(user.getId())) {
                log.warn("User {} attempted unauthorized update to journal ID {}", username, id);
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            // Update fields
            if (newEntry.getTitle() != null && !newEntry.getTitle().isBlank()) {
                oldEntry.setTitle(newEntry.getTitle());
            }
            if (newEntry.getContent() != null && !newEntry.getContent().isBlank()) {
                oldEntry.setContent(newEntry.getContent());
            }
            if (newEntry.getPhotos() != null) {
                oldEntry.setPhotos(newEntry.getPhotos());
            }

            // Preserve original date if not provided
            if (newEntry.getDate() != null) {
                oldEntry.setDate(newEntry.getDate());
            }

            JournalEntry updatedEntry = journalEntryService.saveEntry(oldEntry);

            // Convert to frontend-compatible format
            Map<String, Object> response = new HashMap<>();
            response.put("_id", updatedEntry.getId());
            response.put("title", updatedEntry.getTitle());
            response.put("content", updatedEntry.getContent());
            response.put("photos", updatedEntry.getPhotos());
            response.put("date", formatDateForFrontend(updatedEntry.getDate()));
            response.put("createdAt", formatDateForFrontend(updatedEntry.getDate())); // Using same date for both
            response.put("sentiment", updatedEntry.getSentiment());
            response.put("score", updatedEntry.getScore());
            response.put("confidence", updatedEntry.getConfidence());
            response.put("language", updatedEntry.getLanguage());

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private String formatDateForFrontend(LocalDateTime date) {
        return date != null ?
                date.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT) :
                LocalDateTime.now().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT);
    }

    @Operation(summary = "Delete Entry By ID", description = "Deletes a journal entry by ID if owned by user")
    @DeleteMapping("/id/{myId}")
    public ResponseEntity<Void> deleteJournalEntryById(@PathVariable String myId) {
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

    @Operation(
            summary = "Get journal entry dates by username",
            description = "Fetches a list of all dates on which the specified user has created journal entries."
    )
    @GetMapping("/dates")
    public ResponseEntity<List<Map<String, String>>> getJournalDatesByUsername(@RequestParam String username) {
        log.info("Fetching journal entry dates for user: {}", username);
        try {
            List<LocalDateTime> dateList = journalEntryService.getJournalDatesByUsername(username);

            // Convert to ISO 8601 strings
            List<Map<String, String>> response = dateList.stream()
                    .map(date -> {
                        Map<String, String> dateMap = new HashMap<>();
                        dateMap.put("date", date.toString());
                        return dateMap;
                    })
                    .collect(Collectors.toList());

            log.info("Returning {} journal entry dates for user {}", response.size(), username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching journal dates for user {}: {}", username, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


    @Operation(
            summary = "Get journal entry for a specific date",
            description = "Fetches a journal entry for a given date and username."
    )
    @GetMapping("/dates/{date}")
    public ResponseEntity<JournalEntry> getJournalByDate(
            @PathVariable String date,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Extract username from token (you can use JWTService for this)
            String username = jwtUtil.extractUsername(token.substring(7)); // Remove "Bearer "

            log.info("Fetching journal entry for user {} on date {}", username, date);
            JournalEntry entry = journalEntryService.getJournalByUsernameAndDate(username, date);

            if (entry != null) {
                return ResponseEntity.ok(entry);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching journal entry: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
