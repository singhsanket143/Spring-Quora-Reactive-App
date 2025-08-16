package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AnswerRequestDTO;
import com.example.demo.dto.AnswerResponseDTO;
import com.example.demo.models.Answer;
import com.example.demo.repositories.AnswerRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AnswerService implements IAnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Override
    public Mono<AnswerResponseDTO> createAnswer(AnswerRequestDTO answerRequestDTO) {
        Answer answer = Answer.builder()
                .content(answerRequestDTO.getContent())
                .questionId(answerRequestDTO.getQuestionId())
                .build();
        
        return answerRepository.save(answer)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Mono<AnswerResponseDTO> getAnswerById(String id) {
        return answerRepository.findById(id)
                .map(this::mapToResponseDTO)
                .switchIfEmpty(Mono.error(new RuntimeException("Answer not found with id: " + id)));
    }

    public Mono<AnswerResponseDTO> updateAnswer(String id, AnswerRequestDTO answerRequestDTO) {
        return answerRepository.findById(id)
                .flatMap(existingAnswer -> {
                    existingAnswer.setContent(answerRequestDTO.getContent());
                    return answerRepository.save(existingAnswer);
                })
                .map(this::mapToResponseDTO)
                .switchIfEmpty(Mono.error(new RuntimeException("Answer not found with id: " + id)));
    }

    public Mono<Void> deleteAnswer(String id) {
        return answerRepository.findById(id)
                .flatMap(answer -> answerRepository.deleteById(id))
                .switchIfEmpty(Mono.error(new RuntimeException("Answer not found with id: " + id)));
    }

    public Flux<AnswerResponseDTO> getAllAnswers() {
        return answerRepository.findAll()
                .map(this::mapToResponseDTO);
    }

    public Flux<AnswerResponseDTO> getAnswersByQuestionId(String questionId) {
        return answerRepository.findByQuestionId(questionId)
                .map(this::mapToResponseDTO);
    }

    public Mono<Long> getAnswerCountByQuestionId(String questionId) {
        return answerRepository.countByQuestionId(questionId);
    }

    public Flux<AnswerResponseDTO> getAnswersByQuestionIdOrderByCreatedAtDesc(String questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId)
                .map(this::mapToResponseDTO);
    }

    public Flux<AnswerResponseDTO> getAnswersByQuestionIdOrderByCreatedAtAsc(String questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId)
                .map(this::mapToResponseDTO);
    }

    private AnswerResponseDTO mapToResponseDTO(Answer answer) {
        return AnswerResponseDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .questionId(answer.getQuestionId())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .build();
    }
}
