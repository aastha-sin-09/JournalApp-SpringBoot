package com.aastha.journalApp.repository;

import com.aastha.journalApp.entity.JournalEntry;
import com.aastha.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserRepositoryImpl implements UserRepositoryCustom{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<User> findUsersWithNoJournalInLastNDays(int days) {
        LocalDateTime cutOff = LocalDateTime.now().minusDays(days);
        Date cutOffDate = (Date) Date.from(cutOff.atZone(ZoneId.systemDefault()).toInstant());

        Query activeUsersQuery = new Query();
        activeUsersQuery.addCriteria(Criteria.where("createdAt").gte(cutOffDate));
        List<JournalEntry> recentEntries = mongoTemplate.find(activeUsersQuery, JournalEntry.class);

        Set<ObjectId> activeUsers = new HashSet<>();
        for(JournalEntry entry : recentEntries){
            activeUsers.add(entry.getUserId());
        }

        Query inactiveUserQuery = new Query();
        if(!activeUsers.isEmpty()){
            inactiveUserQuery.addCriteria(Criteria.where("_id").nin(activeUsers));
        }
        List<User> inactiveUsers = mongoTemplate.find(inactiveUserQuery, User.class);

        return inactiveUsers;
    }
}
