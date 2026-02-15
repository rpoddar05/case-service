package com.phep.casesvc.ingesterror;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaseIngestErrorService {

    private final CaseIngestErrorRepository repo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
            String correlationId,
            String topic,
            int partition,
            long kafkaOffset,
            String messageKey,
            String payload,
            Throwable ex
    ){
        CaseIngestErrorEntity e = new CaseIngestErrorEntity();
        e.setCorrelationId(correlationId);
        e.setTopic(topic);
        e.setPartition(partition);
        e.setOffset(kafkaOffset);
        e.setMessageKey(messageKey);

        e.setErrorType(ex.getClass().getSimpleName());
        e.setErrorMessage(safeMessage(ex));
        e.setPayload(payload);

        repo.save(e);

    }

    private String safeMessage(Throwable ex) {
        String msg = ex.getMessage();
        if (msg == null) msg = ex.toString();
        return msg.length() > 4000 ? msg.substring(0, 4000) : msg;
    }
}
