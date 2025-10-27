package com.phep.casesvc.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    private Map<String, Object> body(String code, String msg) {
        return Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "error", code,
                "message", msg
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "msg", fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "error", "validation_error",
                "details", details
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(body("invalid_argument", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body("not_found", ex.getMessage() == null ? "resource not found" : ex.getMessage()));
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String,Object>> handleGeneric(Exception ex) {
//        // avoid leaking internals; log ex in real life
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(body("server_error", "Something went wrong"));
//    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> handleResponseStatus(org.springframework.web.server.ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error","http_error","message", String.valueOf(ex.getReason())));
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNoHandler(org.springframework.web.servlet.NoHandlerFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error","not_found","path", ex.getRequestURL()));
    }
}
