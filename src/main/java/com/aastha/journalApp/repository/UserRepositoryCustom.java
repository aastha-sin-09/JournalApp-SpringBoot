package com.aastha.journalApp.repository;

import com.aastha.journalApp.entity.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> findUsersWithNoJournalInLastNDays(int days);
}
