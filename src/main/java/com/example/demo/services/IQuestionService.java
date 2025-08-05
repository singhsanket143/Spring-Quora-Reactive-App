package com.example.demo.services;

import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.Question;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IQuestionService {
    
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO);

    Mono<QuestionResponseDTO> getQuestionById(String id);

    Flux<QuestionResponseDTO> getAllQuestions();

    Mono<Void> deleteQuestionById(String id);
}
