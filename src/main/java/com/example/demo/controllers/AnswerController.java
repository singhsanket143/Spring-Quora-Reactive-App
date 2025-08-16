package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AnswerRequestDTO;
import com.example.demo.dto.AnswerResponseDTO;
import com.example.demo.services.AnswerService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @PostMapping
    public Mono<ResponseEntity<AnswerResponseDTO>> createAnswer(@Valid @RequestBody AnswerRequestDTO answerRequestDTO) {
        return answerService.createAnswer(answerRequestDTO)
                .map(answer -> ResponseEntity.status(HttpStatus.CREATED).body(answer))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AnswerResponseDTO>> getAnswerById(@PathVariable String id) {
        return answerService.getAnswerById(id)
                .map(answer -> ResponseEntity.ok(answer))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<AnswerResponseDTO>> updateAnswer(@PathVariable String id, 
                                                               @Valid @RequestBody AnswerRequestDTO answerRequestDTO) {
        return answerService.updateAnswer(id, answerRequestDTO)
                .map(answer -> ResponseEntity.ok(answer))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAnswer(@PathVariable String id) {
        return answerService.deleteAnswer(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    public Flux<AnswerResponseDTO> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    @GetMapping("/question/{questionId}")
    public Flux<AnswerResponseDTO> getAnswersByQuestionId(@PathVariable String questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

    @GetMapping("/question/{questionId}/count")
    public Mono<ResponseEntity<Long>> getAnswerCountByQuestionId(@PathVariable String questionId) {
        return answerService.getAnswerCountByQuestionId(questionId)
                .map(count -> ResponseEntity.ok(count))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @GetMapping("/question/{questionId}/latest")
    public Flux<AnswerResponseDTO> getAnswersByQuestionIdLatestFirst(@PathVariable String questionId) {
        return answerService.getAnswersByQuestionIdOrderByCreatedAtDesc(questionId);
    }

    @GetMapping("/question/{questionId}/oldest")
    public Flux<AnswerResponseDTO> getAnswersByQuestionIdOldestFirst(@PathVariable String questionId) {
        return answerService.getAnswersByQuestionIdOrderByCreatedAtAsc(questionId);
    }
}
