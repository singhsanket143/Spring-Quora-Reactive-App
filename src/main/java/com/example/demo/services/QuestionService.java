package com.example.demo.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.adapter.QuestionAdapter;
import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.Question;
import com.example.demo.repositories.QuestionRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService {

    private final QuestionRepository questionRepository;
    
    @Override
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO) {

        Question question = Question.builder()
            .title(questionRequestDTO.getTitle())
            .content(questionRequestDTO.getContent())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return questionRepository.save(question)
        .map(QuestionAdapter::toQuestionResponseDTO)
        .doOnSuccess(response -> System.out.println("Question created successfully: " + response))
        .doOnError(error -> System.out.println("Error creating question: " + error));
    }

    @Override
    public Mono<QuestionResponseDTO> getQuestionById(String id) {
        return questionRepository.findById(id)
            .map(QuestionAdapter::toQuestionResponseDTO)
            .doOnSuccess(response -> System.out.println("Question retrieved successfully: " + response))
            .doOnError(error -> System.out.println("Error retrieving question: " + error))
            .switchIfEmpty(Mono.error(new RuntimeException("Question not found with id: " + id)));
    }


}
