package com.phep.casesvc.ingest;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class LabEventConsumer {

    private static final String CID_MDC_KEY = "cid";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @KafkaListener(topics = "lab.events", groupId = "case-svc")
    public void onMessage(ConsumerRecord<String, String> record) {

        String cid = null;
        var header = record.headers().lastHeader(CORRELATION_ID_HEADER);
        if (header != null) {
            cid = new String(header.value(), StandardCharsets.UTF_8);
        }
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString(); // fallback (should be rare)
        }

        MDC.put(CID_MDC_KEY, cid);
        try {
            log.info("LAB EVENT RECEIVED key={} partition={} offset={} value={}",
                    record.key(), record.partition(), record.offset(), record.value());
        } finally {
            MDC.remove(CID_MDC_KEY);
        }
    }
}
