package com.phep.casesvc.repository;

import com.phep.casesvc.model.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

    Optional<PatientEntity> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDob(String firstName, String lastName, LocalDate dob);

}