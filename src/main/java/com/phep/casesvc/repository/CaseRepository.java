package com.phep.casesvc.repository;

import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.model.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<CaseEntity, UUID> {

    Optional<CaseEntity> findFirstByPatientIdAndStatusOrderByOpenedAtDesc(UUID patientId, CaseStatus status);

    //Page<CaseEntity> findByStatus(CaseStatus status, Pageable pageable);
}
