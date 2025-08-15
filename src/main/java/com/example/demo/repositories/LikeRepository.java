package com.example.demo.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.Like;

@Repository
public interface LikeRepository extends ReactiveMongoRepository<Like, String> {
    
}
