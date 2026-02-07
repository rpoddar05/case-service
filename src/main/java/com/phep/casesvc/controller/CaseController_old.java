package com.phep.casesvc.controller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@RestController
//@RequestMapping("/cases")
public class CaseController_old {

/*    private final CaseService_old service;

     public CaseController_old(CaseService_old service){
         this.service = service;
     }

    @PostMapping
    public ResponseEntity<CaseResponse> create(@Valid @RequestBody CaseDto dto) {

        log.info("CreateCase requested");
        log.debug("CreateCase payload firstName={} lastName={} dob={}", dto.firstName(), dto.lastName(), dto.dob());

        var e = service.create(dto.firstName(), dto.lastName(), dto.dob());
        var body = toResponse(e);

        log.info("CaseCreated  status={}", e.getStatus());

        return ResponseEntity.created(URI.create("/cases/" + e.getCaseId())).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseResponse> get(@PathVariable String id) {
        log.info("GetCase requested id={}", id);
        var entity = service.get(id);
        log.debug("GetCase hit id={} status={}", entity.getCaseId(), entity.getStatus());
        return ResponseEntity.ok(toResponse(entity));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CaseResponse> updateStatus(@PathVariable String id,
                                                     @Valid @RequestBody UpdateStatusRequest req) {
        log.info("UpdateStatus requested id={}", id);

        final CaseStatus newStatus;

        try { newStatus = CaseStatus.valueOf(req.status().trim().toUpperCase()); }
        catch (IllegalArgumentException e) {
            log.warn("UpdateStatus invalid status value id={} raw='{}'", id, req.status());
            throw new IllegalArgumentException("Unknown status");
        }
            var updated = service.updateStatus(id, newStatus);
            log.info("CaseStatusChanged id={} to={}", id, updated.getStatus());
            return ResponseEntity.ok(toResponse(updated));
    }

    @GetMapping
    public Page<CaseResponse> list(@RequestParam(defaultValue="0") int page,
                                   @RequestParam(defaultValue="10") int size,
                                   @RequestParam(required=false) String status) {

        log.info("ListCases requested page={} size={} statusFilter={}", page, size, status);

        CaseStatus st = null;
        if (status != null && !status.isBlank()) {
                try { st = CaseStatus.valueOf(status.trim().toUpperCase());
                }
                catch (IllegalArgumentException ignored) {
                    log.warn("ListCases ignored invalid status filter value='{}'", status);
                }
        }
        var result = service.list(page, size, st).map(this::toResponse);
        log.debug("ListCases result page={} size={} totalElements={} totalPages={}",
                page, size, result.getTotalElements(), result.getTotalPages());
        return result;
    }

    private CaseResponse toResponse(CaseEntity e) {
        return new CaseResponse(
                e.getCaseId(), e.getFirstName(), e.getLastName(),
                e.getDob(), e.getStatus().name(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }*/
}