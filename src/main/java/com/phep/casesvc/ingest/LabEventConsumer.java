package com.phep.casesvc.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phep.casesvc.contract.LabEventDto;
import com.phep.casesvc.service.LabIngestWorkflowService;
import com.phep.casesvc.workflow.IngestResult;
import com.phep.casesvc.workflow.LabIngestCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class LabEventConsumer {

    private static final String CID_MDC_KEY = "cid";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final ObjectMapper objectMapper;
    private final LabIngestWorkflowService labIngestWorkflowService;

    public LabEventConsumer(ObjectMapper objectMapper, LabIngestWorkflowService labIngestWorkflowService) {
        this.objectMapper = objectMapper;
        this.labIngestWorkflowService = labIngestWorkflowService;
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
            // For now, just log. Next step we’ll decide retry vs DLQ vs store in DB
            log.error("LAB EVENT FAILED to parse/process. key={} partition={} offset={} error={}",
                    record.key(), record.partition(), record.offset(), ex.toString(), ex);


            // Throwing causes retry (depending on error handler config).
            // Swallowing commits offset and you “lose” the event.
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
//String caseId,
//String patientFirstName,
//String patientLastName,
//LocalDate dob,
//String testCode,
//String resultValue,
//String resultStatus,  // keep String for now
//String labName