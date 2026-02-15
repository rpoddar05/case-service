package com.phep.casesvc.repository;

import com.phep.casesvc.model.LabResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LabResultRepository extends JpaRepository<LabResultEntity, UUID> {

    Optional<LabResultEntity> findByEventId(String eventId);
}
