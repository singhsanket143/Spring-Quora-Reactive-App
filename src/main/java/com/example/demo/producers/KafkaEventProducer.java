package com.example.demo.producers;

import com.example.demo.config.KafkaConfig;
import com.example.demo.events.ViewCountEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void publishViewCountEvent(ViewCountEvent viewCountEvent){
        kafkaTemplate.send(KafkaConfig.TOPIC_NAME,viewCountEvent.getTargetId(),viewCountEvent)
                .whenComplete((result,err)->{
                    if(err!=null)
                    {
                        System.out.println("Error Publishing view Count Event :"+err.getMessage());
                    }
                });
    }



}
