package com.phep.casesvc.controller;

import com.phep.casesvc.dto.CaseDto;
import com.phep.casesvc.model.CaseEntity;
import com.phep.casesvc.model.CaseStatus;
import com.phep.casesvc.repository.CaseRepository;
import com.phep.casesvc.service.CaseService;
import com.phep.casesvc.web.CaseResponse;
import com.phep.casesvc.web.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cases")
public class CaseController {

    private final CaseService service;

     public CaseController(CaseService service){
         this.service = service;
     }

    @PostMapping
    public ResponseEntity<CaseResponse> create(@Valid @RequestBody CaseDto dto) {
        var e = service.create(dto.firstName(), dto.lastName(), dto.dob());
        var body = toResponse(e);
        return ResponseEntity.created(java.net.URI.create("/cases/" + e.getCaseId())).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(toResponse(service.get(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CaseResponse> updateStatus(@PathVariable String id,
                                                     @Valid @RequestBody UpdateStatusRequest req) {
        CaseStatus newStatus;
        try { newStatus = CaseStatus.valueOf(req.status().trim().toUpperCase()); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Unknown status"); }
        return ResponseEntity.ok(toResponse(service.updateStatus(id, newStatus)));
    }

    @GetMapping
    public Page<CaseResponse> list(@RequestParam(defaultValue="0") int page,
                                   @RequestParam(defaultValue="10") int size,
                                   @RequestParam(required=false) String status) {
        CaseStatus st = null;
        if (status != null && !status.isBlank()) {
            try { st = CaseStatus.valueOf(status.trim().toUpperCase()); } catch (IllegalArgumentException ignored) {}
        }
        return service.list(page, size, st).map(this::toResponse);
    }

    private CaseResponse toResponse(CaseEntity e) {
        return new CaseResponse(
                e.getCaseId(), e.getFirstName(), e.getLastName(),
                e.getDob(), e.getStatus().name(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}