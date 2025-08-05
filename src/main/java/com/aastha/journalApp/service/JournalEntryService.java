package com.aastha.journalApp.service;

import com.aastha.journalApp.dto.SentimentResponse;
import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Slf4j
@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SentimentService sentimentService;

    @Transactional
    public JournalEntry saveEntry(JournalEntry journalEntry, String username) {
        log.info("Saving new journal entry for user: {}", username);
        User user = userService.findByUserName(username);

        if (journalEntry.getPhotos() == null) {
            journalEntry.setPhotos(new ArrayList<>());
        }

        log.info("Saving entry with {} photos", journalEntry.getPhotos().size());

        journalEntry.setUserId(user.getId());
        if (journalEntry.getDate() == null) {
            journalEntry.setDate(LocalDateTime.now(ZoneOffset.UTC)); // Use UTC for consistency
        }

        //sentiment analysis
        SentimentResponse sentiment = sentimentService.analyzeSentiment(journalEntry.getContent());
        if (sentiment != null) {
            journalEntry.setSentiment(sentiment.getSentiment());
            journalEntry.setScore(sentiment.getScore());
            journalEntry.setConfidence(sentiment.getConfidence());
            journalEntry.setLanguage(sentiment.getLanguage());
            log.debug("Sentiment analyzed: {}, confidence: {}%", sentiment.getSentiment(), sentiment.getConfidence());
        } else {
            log.warn("Sentiment analysis failed or returned null for user: {}", username);
        }

        log.debug("Saving entry with date: {}", journalEntry.getDate());
        log.debug("Journal entry '{}' saved successfully", journalEntry.getTitle());
        return journalEntryRepository.save(journalEntry);
    }

    public JournalEntry saveEntry(JournalEntry journalEntry) {
        log.info("Updating existing journal entry: {}", journalEntry.getTitle());

        SentimentResponse sentiment = sentimentService.analyzeSentiment(journalEntry.getContent());

        if (sentiment != null) {
            journalEntry.setSentiment(sentiment.getSentiment());
            journalEntry.setScore(sentiment.getScore());
            journalEntry.setConfidence(sentiment.getConfidence());
            journalEntry.setLanguage(sentiment.getLanguage());
            log.debug("Updated sentiment analyzed: {}, confidence: {}%",
                    sentiment.getSentiment(), sentiment.getConfidence());
        } else {
            log.warn("Sentiment analysis failed or returned null for updating entry: {}",
                    journalEntry.getId());
        }

        return journalEntryRepository.save(journalEntry);
    }

    public Optional<JournalEntry> findById(String id) {
        log.info("Finding journal entry by ID: {}", id);
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Attempting to delete journal entry ID {} for user {}", id, username);

        User user = userService.findByUserName(username);
        Optional<JournalEntry> removed = journalEntryRepository.findById(id);

        if (removed.isPresent() && removed.get().getUserId().equals(user.getId())) {
            journalEntryRepository.deleteById(id);
            log.debug("Deleted journal entry ID: {}", id);
            return true;
        }

        log.warn("Journal entry ID {} not found or doesn't belong to user {}", id, username);
        return false;
    }

    public List<JournalEntry> findAllByUserId(String id) {
        return journalEntryRepository.findByUserId(id);
    }

    public List<LocalDateTime> getJournalDatesByUsername(String username) {
        log.debug("Retrieving journal entry dates for user: {}", username);

        try {
            Optional<User> optionalUser = Optional.ofNullable(userService.findByUserName(username));

            if (!optionalUser.isPresent()) {
                log.warn("User not found: {}", username);
                return Collections.emptyList(); // or throw new RuntimeException(...)
            }

            User user = optionalUser.get();
            List<JournalEntry> journals = journalEntryRepository.findByUserId(user.getId());

            List<LocalDateTime> dates = journals.stream()
                    .map(JournalEntry::getDate)
                    .collect(Collectors.toList());

            log.info("Retrieved {} journal dates for user: {}", dates.size(), username);
            return dates;

        } catch (Exception e) {
            log.error("Error retrieving journal dates for user {}: {}", username, e.getMessage(), e);
            return Collections.emptyList(); // Or rethrow: throw new RuntimeException(...)
        }
    }

    public JournalEntry getJournalByUsernameAndDate(String username, String date) {
        log.info("Fetching journal entry for user '{}' on date '{}'", username, date);

        try {
            User user = userService.findByUserName(username);
            if (user == null) {
                log.warn("User '{}' not found", username);
                return null;
            }

            List<JournalEntry> entries = journalEntryRepository.findByUserId(user.getId());
            if (entries.isEmpty()) {
                log.info("No journal entries found for user '{}'", username);
                return null;
            }

            LocalDate targetDate = LocalDate.parse(date); // Format: yyyy-MM-dd

            for (JournalEntry entry : entries) {
                LocalDate entryDate = entry.getDate().toLocalDate(); // Ignore time
                if (entryDate.equals(targetDate)) {
                    log.info("Journal entry found for date '{}' and user '{}'", date, username);
                    return entry;
                }
            }

            log.info("No journal entry found for date '{}' and user '{}'", date, username);
            return null;

        } catch (Exception e) {
            log.error("Error while fetching journal entry for user '{}': {}", username, e.getMessage());
            return null;
        }
    }
}