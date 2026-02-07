package com.phep.casesvc.service;

import com.phep.casesvc.workflow.CaseResolution;
import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.model.CaseStatus;
import com.phep.casesvc.model.PatientEntity;
import com.phep.casesvc.repository.CaseRepository;
import com.phep.casesvc.workflow.LabIngestCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class CaseWorkflowService {

    private CaseRepository caseRepository;

    @Transactional
    public CaseResolution resolveOrCreateCase(PatientEntity patient) {

       CaseEntity caseEntity;
       boolean caseCreated;

        Optional<CaseEntity> caseEntityOptional = caseRepository.findFirstByPatientIdAndStatusOrderByOpenedAtDesc(patient.getId(), CaseStatus.OPEN);

        if(caseEntityOptional.isPresent()){
            caseEntity = caseEntityOptional.get();
            caseCreated = false;
        }else {
            CaseEntity created = new CaseEntity();
            created.setPatient(patient);
            created.setStatus(CaseStatus.OPEN);
            created.setOpenedAt(OffsetDateTime.now());

            caseEntity = caseRepository.save(created);
            caseCreated = true;
        }

        return new CaseResolution(caseEntity,caseCreated);
    }

    @Transactional(readOnly = true)
    public Optional<CaseEntity> findActiveCaseByHint(LabIngestCommand cmd) {

        String raw = cmd.caseId();
        if (raw == null || raw.isBlank()) return Optional.empty();

        final UUID caseId;
        try {
            caseId = UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid caseId format '{}'. Ignoring hint.", raw);
            return Optional.empty();
        }

        return caseRepository.findById(caseId)
                .filter(c -> c.getStatus().isActive())
                .filter(c -> patientMatches(c.getPatient(), cmd))
                .map(c -> {
                    log.info("caseId hint accepted caseId={} patientId={}", c.getId(), c.getPatient().getId());
                    return c;
                });
    }

    private boolean patientMatches(PatientEntity p, LabIngestCommand cmd) {
        if (p == null) return false;
        return p.getDob().equals(cmd.dob())
                && p.getFirstName().equalsIgnoreCase(cmd.firstName())
                && p.getLastName().equalsIgnoreCase(cmd.lastName());
    }


}
