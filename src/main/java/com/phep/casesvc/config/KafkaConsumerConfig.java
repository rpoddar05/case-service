package com.phep.casesvc.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.phep.casesvc.ingesterror.CaseIngestErrorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CaseIngestErrorService caseIngestErrorService;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        // 1) Where to publish when we give up (DLQ topic per original topic)
        DeadLetterPublishingRecoverer dlqRecoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate, (record, ex) ->
                        new TopicPartition(record.topic() + ".DLQ", record.partition())
                );

        // 2) Final recoverer: called only after retries exhausted OR immediately for non-retryable exceptions
        ConsumerRecordRecoverer finalRecoverer = (record,ex) -> {
            String key = record.key() == null ? null : record.key().toString();
            String payload = record.value() == null ? null : record.value().toString();
            String correlationId = extractCorrelationId(record);

            // Persist failure metadata once
            try {
                caseIngestErrorService.recordFailure(
                        correlationId,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        key,
                        payload,
                        ex
                );
            } catch (Exception persistEx) {
                // Don't block DLQ publish if DB write fails
                log.error("Failed to persist case_ingest_error. corrId={} topic={} partition={} offset={} persistError={}",
                        correlationId, record.topic(), record.partition(), record.offset(),
                        persistEx.toString(), persistEx);
            }

            // Publish to DLQ
            try {
                dlqRecoverer.accept(record, ex);
            } catch (Exception dlqEx) {
                log.error("Failed to publish to DLQ. corrId={} topic={} partition={} offset={} dlqError={}",
                        correlationId, record.topic(), record.partition(), record.offset(),
                        dlqEx.toString(), dlqEx);
            }

            log.error("Record sent to DLQ. corrId={} topic={} partition={} offset={} key={} errorType={} error={}",
                    correlationId, record.topic(), record.partition(), record.offset(), key,
                    ex.getClass().getSimpleName(), ex.toString(), ex);
        };

        // 3) Retry policy: 2 retries (so total 3 attempts including first try), 1s between retries
        FixedBackOff backOff = new FixedBackOff(1000L, 2L);

        DefaultErrorHandler handler = new DefaultErrorHandler(finalRecoverer, backOff);

        // Optional: log each retry attempt (great for demo)
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            String correlationId = extractCorrelationId(record);
            log.warn("Retrying record. attempt={} corrId={} topic={} partition={} offset={} key={} errorType={} error={}",
                    deliveryAttempt,
                    correlationId,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    ex.getClass().getSimpleName(),
                    ex.toString());
        });

        // 4) Exceptions that should NOT be retried (poison-pill -> go straight to finalRecoverer -> DB + DLQ)
        handler.addNotRetryableExceptions(
                JsonProcessingException.class,
                IllegalArgumentException.class
        );

        return handler;
    }

    private String extractCorrelationId(ConsumerRecord<?, ?> record) {
        var header = record.headers().lastHeader(CORRELATION_ID_HEADER);
        if (header != null && header.value() != null) {
            String cid = new String(header.value(), StandardCharsets.UTF_8);
            if (!cid.isBlank()) return cid;
        }
        // fallback so we always have something traceable
        return UUID.randomUUID().toString();
    }
}
