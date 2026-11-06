package com.example.studentmanagement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
 * EXAM 3 GRADING NOTE (Section A, Q2): the three handlers below are well
 * built - correct status codes, correct {status, message} / field-error
 * shapes, matching the spec exactly. The marks lost in Section A aren't a
 * problem with this file; they're that the controllers never throw the
 * exceptions this class is listening for. ResourceNotFoundException and
 * DuplicateResourceException are each defined, handled perfectly here, and
 * then never `throw`n anywhere in the whole src tree (grep for
 * "throw new ResourceNotFoundException" / "throw new
 * DuplicateResourceException" - both come back empty). Controllers use
 * `new RuntimeException(...)` or `ResponseEntity.notFound().build()`
 * instead, which either skips this class entirely (plain RuntimeException
 * -> Spring's default 500) or returns 404 with an empty body instead of the
 * documented {"status":404,"message":"..."} shape. The fix isn't here, it's
 * swapping those `RuntimeException`/`notFound().build()` calls in the
 * controllers for `throw new ResourceNotFoundException("...")`.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DuplicateResourceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 409);
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}