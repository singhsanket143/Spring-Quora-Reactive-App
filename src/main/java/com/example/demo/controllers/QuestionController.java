package com.example.demo.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.services.IQuestionService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final IQuestionService questionService;
    
    @PostMapping()
    public Mono<QuestionResponseDTO> createQuestion(@RequestBody QuestionRequestDTO questionRequestDTO) {
        return questionService.createQuestion(questionRequestDTO)
        .doOnSuccess(response -> System.out.println("Question created successfully: " + response))
        .doOnError(error -> System.out.println("Error creating question: " + error));
    }
    
    @GetMapping("/{id}")
    public Mono<QuestionResponseDTO> getQuestionById(@PathVariable String id) {
        return questionService.getQuestionById(id)
            .doOnSuccess(response -> System.out.println("Question retrieved successfully: " + response))
            .doOnError(error -> System.out.println("Error retrieving question: " + error));
    }

    @GetMapping()
    public Flux<QuestionResponseDTO> getAllQuestions() {
        return questionService.getAllQuestions()
            .doOnNext(response -> System.out.println("Questions retrieved successfully: " + response))
            .doOnError(error -> System.out.println("Error retrieving questions: " + error));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteQuestionById(@PathVariable String id) {
        return questionService.deleteQuestionById(id)
            .doOnSuccess(aVoid -> System.out.println("Question deleted successfully with id: " + id))
            .doOnError(error -> System.out.println("Error deleting question: " + error));
    }

    @GetMapping("/search")
    public Flux<QuestionResponseDTO> searchQuestions(
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return questionService.searchQuestions(query, page, size)
                .doOnError(error -> System.out.println("Error retrieving search results: " + error))
                .doOnComplete(() -> System.out.println("Search results retrieved successfully."));
    }
    
    @GetMapping("/tag/{tag}")
    public Flux<QuestionResponseDTO> getQuestionsByTag(@PathVariable String tag,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        throw new UnsupportedOperationException("Not implemented");
    }

    
    
    
}
