package com.phep.casesvc.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phep.casesvc.contract.LabEventDto;
import com.phep.casesvc.ingesterror.CaseIngestErrorService;
import com.phep.casesvc.service.LabIngestWorkflowService;
import com.phep.casesvc.workflow.IngestResult;
import com.phep.casesvc.workflow.LabIngestCommand;
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

    private final ObjectMapper objectMapper;
    private final LabIngestWorkflowService labIngestWorkflowService;
    private final CaseIngestErrorService caseIngestErrorService;

    public LabEventConsumer(ObjectMapper objectMapper, LabIngestWorkflowService labIngestWorkflowService, CaseIngestErrorService caseIngestErrorService) {
        this.objectMapper = objectMapper;
        this.labIngestWorkflowService = labIngestWorkflowService;
        this.caseIngestErrorService = caseIngestErrorService;
    }


    @KafkaListener(topics = "lab.events", groupId = "case-svc")
    public void onMessage(ConsumerRecord<String, String> record) {

        String cid = extractCid(record);

        MDC.put(CID_MDC_KEY, cid);
        try {
            log.info("LAB EVENT RECEIVED key={} partition={} offset={} value={}",
                    record.key(), record.partition(), record.offset(), record.value());

            LabEventDto event = objectMapper.readValue(record.value(),LabEventDto.class);

            log.info("LAB EVENT PARSED lastName={} testCode={} status={}",
                    event.patientLastName(), event.testCode(), event.resultStatus());


            LabIngestCommand cmd = from(event);
            IngestResult result = labIngestWorkflowService.ingestLab(cmd);

            log.info("LabIngested patientId={} caseId={} labId={} patientCreated={} caseCreated={}",
                    result.patientId(), result.caseId(), result.labResultId(),
                    result.patientCreated(), result.caseCreated());



        } catch (Exception ex) {

            // 1) Persist error (always) so we don't rely on logs
            try {
                caseIngestErrorService.recordFailure(
                        cid,
                        record.topic(),
                        record.partition(),
                        record.offset(),     // maps to kafka_offset column via service/entity
                        record.key(),
                        record.value(),
                        ex
                );
            } catch (Exception persistEx) {
                // If persisting fails, we still want the original exception to drive retry behavior
                log.error("Failed to persist case_ingest_error row. corrId={} topic={} partition={} offset={} persistError={}",
                        cid, record.topic(), record.partition(), record.offset(), persistEx.toString(), persistEx);
            }

            // 2) Log with context
            log.error("LAB EVENT FAILED. key={} topic={} partition={} offset={} corrId={} error={}",
                    record.key(), record.topic(), record.partition(), record.offset(), cid, ex.toString(), ex);

            // 3) Rethrow: keep retry semantics for now (PR-2.2 will add controlled retry + DLQ)
            throw new RuntimeException(ex);

        }finally {
            MDC.remove(CID_MDC_KEY);
        }
    }

    private String extractCid(ConsumerRecord<String, String> record) {
        String cid = null;
        var header = record.headers().lastHeader(CORRELATION_ID_HEADER);
        if (header != null) {
            cid = new String(header.value(), StandardCharsets.UTF_8);
        }
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }
        return cid;
    }

    private LabIngestCommand from(LabEventDto e){

        return new LabIngestCommand(
                e.caseId(),
                e.patientFirstName(),
                e.patientLastName(),
                e.dob(),
                e.testCode(),
                e.resultValue(),
                e.resultStatus(),
                e.labName(),
                null
        );

    }
}