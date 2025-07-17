package com.aastha.journalApp.repository;

import com.aastha.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId>, UserRepositoryCustom {
    User findByUsername(String username);

}

