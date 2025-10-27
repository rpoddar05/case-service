package com.phep.casesvc.service;

import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.model.CaseStatus;
import com.phep.casesvc.repository.CaseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaseService {
    private final CaseRepository repo;
    public CaseService(CaseRepository repo){ this.repo = repo; }

    @Transactional
    public CaseEntity create(String first, String last, String dob) {
        var e = new CaseEntity();
        e.setFirstName(first);
        e.setLastName(last);
        e.setDob(dob);
        // default OPEN via enum + @PrePersist safeguard if added
        return repo.save(e);
    }

    @Transactional(readOnly = true)
    public CaseEntity get(String id) {
        return repo.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("case not found"));
    }

    @Transactional
    public CaseEntity updateStatus(String id, CaseStatus newStatus) {
        var e = get(id);
        e.setStatus(newStatus);
        return repo.save(e);
    }

    @Transactional(readOnly = true)
    public Page<CaseEntity> list(Integer page, Integer size, CaseStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return (status == null) ? repo.findAll(pageable) : repo.findByStatus(status, pageable);
    }
}
