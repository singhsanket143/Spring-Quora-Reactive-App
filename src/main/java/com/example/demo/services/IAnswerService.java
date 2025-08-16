package com.example.demo.services;

import com.example.demo.dto.AnswerRequestDTO;
import com.example.demo.dto.AnswerResponseDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAnswerService {
    
    public Mono<AnswerResponseDTO> createAnswer(AnswerRequestDTO answerRequestDTO);

    public Mono<AnswerResponseDTO> getAnswerById(String id);

    public Mono<AnswerResponseDTO> updateAnswer(String id, AnswerRequestDTO answerRequestDTO);

    public Mono<Void> deleteAnswer(String id);

    public Flux<AnswerResponseDTO> getAllAnswers();

    public Flux<AnswerResponseDTO> getAnswersByQuestionId(String questionId);

    public Mono<Long> getAnswerCountByQuestionId(String questionId);

    public Flux<AnswerResponseDTO> getAnswersByQuestionIdOrderByCreatedAtDesc(String questionId);

    public Flux<AnswerResponseDTO> getAnswersByQuestionIdOrderByCreatedAtAsc(String questionId);
}
