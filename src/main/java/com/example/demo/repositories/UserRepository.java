package com.example.demo.repositories;

import com.example.demo.models.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    // You can later add: findByUsername, findByEmail, etc.
}
