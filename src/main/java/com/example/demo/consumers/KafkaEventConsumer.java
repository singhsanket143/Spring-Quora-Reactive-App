package com.example.demo.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.demo.config.KafkaConfig;
import com.example.demo.events.ViewCountEvent;
import com.example.demo.repositories.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    
    private final QuestionRepository questionRepository;

    @KafkaListener(
        topics = KafkaConfig.TOPIC_NAME,
        groupId = "view-count-consumer",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleViewCountEvent(ViewCountEvent viewCountEvent) {
        questionRepository.findById(viewCountEvent.getTargetId())
        .flatMap(question -> {
            System.out.println("Incrementing view count for question: " + question.getId());
            Integer views = question.getViews();
            question.setViews(views == null ? 0 : views + 1);
            return questionRepository.save(question);
        })
        .subscribe(updatedQuestion -> {
            System.out.println("View count incremented for question: " + updatedQuestion.getId());
        }, error -> {
            System.out.println("Error incrementing view count for question: " + error.getMessage());
        });
    }
    
}

// interfcae ViewCountIncrStrategy 

// QuestionViewCountIncrementStrategy

// AnswerViewCountIncrementStrategy

// ViewCountStrategyFactory