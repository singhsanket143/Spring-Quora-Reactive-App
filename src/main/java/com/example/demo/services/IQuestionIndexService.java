package com.example.demo.services;

import com.example.demo.models.Question;
import com.example.demo.models.QuestionElasticDocument;

public interface IQuestionIndexService {
    

    void createQuestionIndex(Question question) ;
}
