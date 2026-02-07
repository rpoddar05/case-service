package com.phep.casesvc.ingesterror;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CaseIngestErrorRepository extends JpaRepository<CaseIngestErrorEntity, UUID> {
}
