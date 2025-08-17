package com.example.demo.services;

import org.springframework.stereotype.Service;

import com.example.demo.models.Question;
import com.example.demo.models.QuestionElasticDocument;
import com.example.demo.repositories.QuestionDocumentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionIndexService implements IQuestionIndexService {
    
    private final QuestionDocumentRepository questionDocumentRepository;

    @Override
    public void createQuestionIndex(Question question) {
        QuestionElasticDocument document = QuestionElasticDocument.builder()
            .id(question.getId())
            .title(question.getTitle())
            .content(question.getContent())
            .build();

        questionDocumentRepository.save(document);
    }
}
