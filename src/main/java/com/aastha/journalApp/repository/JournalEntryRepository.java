package com.aastha.journalApp.repository;

import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface JournalEntryRepository extends MongoRepository<JournalEntry, String> {
    List<JournalEntry> findByUserId(String userId);

}
