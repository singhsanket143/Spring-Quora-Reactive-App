package com.example.demo.services;

import java.util.List;

import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.Question;
import com.example.demo.models.QuestionElasticDocument;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IQuestionService {
    
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO);

    public Flux<QuestionResponseDTO> searchQuestions(String searchTerm, int offset, int page);

    public Flux<QuestionResponseDTO> getAllQuestions(String cursor, int size);

    public Mono<QuestionResponseDTO> getQuestionById(String id);

    public List<QuestionElasticDocument> searchQuestionsByElasticsearch(String query);
}
