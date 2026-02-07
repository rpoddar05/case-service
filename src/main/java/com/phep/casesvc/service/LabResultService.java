package com.phep.casesvc.service;

import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.model.LabResultEntity;
import com.phep.casesvc.model.PatientEntity;
import com.phep.casesvc.repository.LabResultRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class LabResultService {

    private final LabResultRepository labResultRepository;

    @Transactional
    public LabResultEntity saveLabResult(
            PatientEntity patient,
            CaseEntity caze,
            String testCode,
            String resultValue,
            String resultStatus,
            String labName,
            OffsetDateTime receivedAt
    ){
        LabResultEntity lab = new LabResultEntity();
        lab.setPatient(patient);
        lab.setCaseEntity(caze); //linking case ids
        lab.setLabName(labName);
        lab.setTestCode(testCode);
        lab.setResultValue(resultValue);
        lab.setResultStatus(resultStatus);
        lab.setReceivedAt(receivedAt != null ? receivedAt : OffsetDateTime.now());
        return labResultRepository.save(lab);

    }
}