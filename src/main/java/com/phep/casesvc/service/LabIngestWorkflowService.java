package com.phep.casesvc.service;

import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.workflow.CaseResolution;
import com.phep.casesvc.workflow.IngestResult;
import com.phep.casesvc.model.LabResultEntity;
import com.phep.casesvc.model.PatientEntity;
import com.phep.casesvc.repository.PatientRepository;
import com.phep.casesvc.workflow.LabIngestCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LabIngestWorkflowService {

    private final PatientRepository patientRepository;
    private final CaseWorkflowService caseWorkflowService;
    private final LabResultService labResultService;

    @Transactional
    public IngestResult ingestLab(LabIngestCommand cmd){

        //check if case id is already exist in case of retry/ reprocess
        // 0) Fast-path: caseId hint (no patient/case creation)
        Optional<CaseEntity> hintedCaseOpt = caseWorkflowService.findActiveCaseByHint(cmd);
        if (hintedCaseOpt.isPresent()) {
            CaseEntity caze = hintedCaseOpt.get();
            PatientEntity patient = caze.getPatient();

            LabResultEntity lab = labResultService.saveLabResult(
                    cmd.eventId(),
                    patient,
                    caze,
                    cmd.testCode(),
                    cmd.resultValue(),
                    cmd.resultStatus(),
                    cmd.labName(),
                    cmd.receivedAt()
            );

            return new IngestResult(patient.getId(), caze.getId(), lab.getId(), false, false);
        }

        // 1) patient match/create
       Optional<PatientEntity>  patientOpt = patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDob(cmd.firstName(), cmd.lastName(), cmd.dob());

       PatientEntity patient;
       boolean patientCreated;

       if(patientOpt.isPresent()){
           patient = patientOpt.get();
           patientCreated = false;

       } else{
           patient= new PatientEntity();
           patient.setFirstName(cmd.firstName());
           patient.setLastName(cmd.lastName());
           patient.setDob(cmd.dob());
           patient = patientRepository.save(patient);
           patientCreated = true;
       }

        // 2) resolve/create OPEN case
        CaseResolution caseResolution = caseWorkflowService.resolveOrCreateCase(patient);

        // 3) persist lab row
        LabResultEntity labResult = labResultService.saveLabResult(cmd.eventId(), patient, caseResolution.caseEntity(),
                                                                    cmd.testCode(), cmd.resultValue(),
                                                                    cmd.resultStatus(), cmd.labName(), cmd.receivedAt());


        return new IngestResult(patient.getId(),
                    caseResolution.caseEntity().getId(),
                    labResult.getId(),
                    patientCreated,
                    caseResolution.caseCreated());

    }


}
