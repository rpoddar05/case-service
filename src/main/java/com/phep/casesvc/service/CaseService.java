package com.phep.casesvc.service;

import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.model.CaseStatus;
import com.phep.casesvc.repository.CaseRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CaseService {
    private final CaseRepository repo;
    public CaseService(CaseRepository repo){ this.repo = repo; }

    @Transactional
    public CaseEntity create(String first, String last, String dob) {
        log.debug("CreateCase service start first={} last={} dob={}", first, last, dob);
        var e = new CaseEntity();
        e.setFirstName(first);
        e.setLastName(last);
        e.setDob(dob);
        var saved = repo.save(e);
        log.info("CasePersisted id={} status={}", saved.getCaseId(), saved.getStatus());
        return saved;
    }

    @Transactional(readOnly = true)
    public CaseEntity get(String id) {
        return repo.findById(id)
                .map(e -> {
                    log.debug("CaseEntity id={} status={}", e.getCaseId(), e.getStatus());
                return e;
                })
                .orElseThrow(() -> {
                    log.warn("CaseNotFound id={}", id);
                   return new java.util.NoSuchElementException("case not found");
                });
    }

    @Transactional
    public CaseEntity updateStatus(String id, CaseStatus newStatus) {
        var e = get(id);
        var old = e.getStatus();
        e.setStatus(newStatus);
        var saved = repo.save(e);
        log.info("CaseStatusUpdated id={} from={} to={}", id, old, saved.getStatus());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<CaseEntity> list(Integer page, Integer size, CaseStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        log.debug("ListCases service page={} size={} statusFilter={}", page, size, status);
        return (status == null) ? repo.findAll(pageable) : repo.findByStatus(status, pageable);
    }
}
