package com.phep.casesvc.ingest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LabEventConsumer {

    @KafkaListener(topics = "lab.events", groupId = "case-svc")
    public void onMessage(@Payload String rawJson) {
        log.info("LAB EVENT RECEIVED: {}", rawJson);
    }
}

