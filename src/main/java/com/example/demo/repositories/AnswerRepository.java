package com.example.demo.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.Answer;

@Repository
public interface AnswerRepository extends ReactiveMongoRepository<Answer, String> {
    
}
