package com.aastha.journalApp.service;

import com.aastha.journalApp.dto.SentimentResponse;
import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import com.aastha.journalApp.repository.JournalEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public void saveEntry(JournalEntry journalEntry, String username) {
        log.info("Saving new journal entry for user: {}", username);
        User user = userService.findByUserName(username);

        journalEntry.setUserId(user.getId());
        journalEntry.setDate(LocalDateTime.now());

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

        JournalEntry saved = journalEntryRepository.save(journalEntry);
        log.debug("Journal entry '{}' saved successfully", saved.getTitle());
    }

    public void saveEntry(JournalEntry journalEntry) {
        log.info("Updating existing journal entry: {}", journalEntry.getTitle());
        journalEntryRepository.save(journalEntry);
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
        Optional<JournalEntry> removed = journalEntryRepository.findById(id);

        if (removed.isPresent() && removed.get().getUserId().equals(user.getId())) {
            journalEntryRepository.deleteById(id);
            log.debug("Deleted journal entry ID: {}", id);
            return true;
        }

        log.warn("Journal entry ID {} not found or doesn't belong to user {}", id, username);
        return false;
    }

    public List<JournalEntry> findAllByUserId(ObjectId id) {
        return journalEntryRepository.findByUserId(id);
    }
}