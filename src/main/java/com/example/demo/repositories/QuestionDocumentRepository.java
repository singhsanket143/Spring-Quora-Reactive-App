package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.demo.models.QuestionElasticDocument;

public interface QuestionDocumentRepository extends ElasticsearchRepository<QuestionElasticDocument, String> {
    
    List<QuestionElasticDocument> findByTitleContainingOrContentContaining(String title, String content);
}
