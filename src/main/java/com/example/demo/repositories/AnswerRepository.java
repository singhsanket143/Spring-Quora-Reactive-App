package com.example.demo.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.Answer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AnswerRepository extends ReactiveMongoRepository<Answer, String> {
    
    Flux<Answer> findByQuestionId(String questionId);
    
    Mono<Long> countByQuestionId(String questionId);
    
    Flux<Answer> findByQuestionIdOrderByCreatedAtDesc(String questionId);
    
    Flux<Answer> findByQuestionIdOrderByCreatedAtAsc(String questionId);
}
