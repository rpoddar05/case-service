package com.phep.casesvc.repository;

import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.model.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<CaseEntity, String> {

    Page<CaseEntity> findByStatus(CaseStatus status, Pageable pageable);
}
