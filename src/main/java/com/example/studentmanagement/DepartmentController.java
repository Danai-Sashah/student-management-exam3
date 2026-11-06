package com.example.studentmanagement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*
 * EXAM 3 GRADING NOTE (Section D, Q7):
 * - create() correctly returns 201 - good - but never checks whether a
 *   department with that name already exists (case-insensitively, per the
 *   spec). Right now a duplicate name reaches the database's unique
 *   constraint and comes back as an unhandled DataIntegrityViolationException
 *   -> Spring's default 500, not the documented 409. The fix is to check
 *   first and throw your own exception:
 *     if (departments.existsByNameIgnoreCase(dept.getName()))
 *         throw new DuplicateResourceException("Department " + dept.getName() + " already exists");
 *   (that query method would need adding to DepartmentRepository - it isn't
 *   there yet either.)
 * - GET /api/departments/{id}/students is missing entirely (3 marks in the
 *   spec's table). This is exactly what Department.students (currently
 *   missing - see the note in Department.java) would be used to serve:
 *   once that field exists, this method is just
 *   `departments.findById(id).orElseThrow(...).getStudents()` mapped to
 *   DTOs.
 * - delete() returns 404/204 correctly but never checks for existing
 *   students before deleting (spec wants 409 Conflict if the department
 *   still has students). Without that check, deleting a department that
 *   still has students hits the FK constraint on students.department_id
 *   and - again - surfaces as an unhandled 500 instead of a clean 409. This
 *   is literally the scenario your own theory answer to Q5 describes as the
 *   thing to avoid. The fix again depends on Department.students existing:
 *     if (!dept.getStudents().isEmpty())
 *         throw new DuplicateResourceException("Department " + id + " still has students");
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentRepository departments;
    public DepartmentController(DepartmentRepository departments) { this.departments = departments; }

    @GetMapping
    public List<Department> getAll() { return departments.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getOne(@PathVariable Long id) {
        return departments.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Department> create(@RequestBody Department dept) {
        return ResponseEntity.status(201).body(departments.save(dept));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!departments.existsById(id)) return ResponseEntity.notFound().build();
        departments.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}