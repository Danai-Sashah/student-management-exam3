package com.example.studentmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * EXAM 3 GRADING NOTE - this controller is where most of the marks in
 * Sections C (DTOs) and D (Relationship Endpoints) were lost. Summary of
 * what's missing, endpoint by endpoint, with the "why":
 *
 * 1. create() accepts and returns the raw `Student` entity, not
 *    dto/StudentRequest and dto/StudentResponse, even though both exist in
 *    the project already and StudentResponse even builds the exact
 *    {id, firstName, ..., department:{id,name}, courses:[...]} shape the
 *    spec asks for. Two concrete consequences:
 *      - @Valid was never added to the @RequestBody parameter, and because
 *        the parameter type is Student (not StudentRequest), the validation
 *        annotations in StudentRequest can't run anyway. A POST with a
 *        blank firstName or age=200 is accepted as-is: Section A's 400
 *        requirement silently does nothing.
 *      - The client would have to send a *nested* department object to set
 *        a student's department ({"department": {"id": 1}, ...}), not the
 *        {"departmentId": 1} shape the spec's example request shows. The
 *        exam's own test script sends departmentId, so student creation as
 *        currently written doesn't match the documented contract.
 * 2. create() returns ResponseEntity.ok(...), i.e. HTTP 200. The spec asks
 *    for 201 Created on successful creation (use
 *    ResponseEntity.status(HttpStatus.CREATED).body(...), the way
 *    DepartmentController.create() correctly does).
 * 3. There's no check for "department not found" (should throw
 *    ResourceNotFoundException -> 404) or "email already used" (should
 *    throw DuplicateResourceException -> 409) before saving. Right now a
 *    bad departmentId or a duplicate email both fall straight through to a
 *    raw database exception, which Spring turns into an unhandled 500 -
 *    GlobalExceptionHandler never gets a chance to produce the documented
 *    404/409 JSON body, because nothing throws the exceptions it listens
 *    for.
 * 4. getAll() returns Page<Student> - entities again, with the same
 *    DTO/shape problem as above but worse: Student.courses is a
 *    Set<Course>, and Course.students (in Course.java) is the inverse side
 *    of that same relationship. Serializing a Student who has any courses
 *    would serialize department, then courses, then for each course its
 *    `students` set (which includes this student again) with *their*
 *    courses, and so on - this is the literal "infinite JSON" scenario
 *    described in your own theory answer (Q2), just not fixed here. The
 *    safe pattern: load the entity in a @Transactional service method,
 *    immediately map it to a DTO, and only ever return the DTO.
 * 5. There's no PUT /api/students/{id}. The spec explicitly says the Exam 2
 *    PUT/PATCH/DELETE endpoints "must keep working" - PUT (full replace,
 *    requiring departmentId) is missing entirely, only PATCH exists.
 * 6. search() only reads a `lastName` parameter. Section E asks for the
 *    *same* endpoint to also accept `?courseCode=DB201` and run the JPQL
 *    @Query you already wrote (StudentRepository.findByCourseCode) - that
 *    method exists and is correct, it's just never called from here. A
 *    quick fix would branch on which parameter was supplied:
 *      if (courseCode != null) return studentRepository.findByCourseCode(courseCode);
 * 7. There is no POST/DELETE/GET under /api/students/{id}/courses/{courseId}
 *    or /api/students/{id}/courses at all in this file. Enrol/unenrol/list-
 *    courses-for-a-student were instead implemented in
 *    EnrollmentController.java under a completely different URL shape
 *    (/api/enrollments) - see the notes there for why that doesn't satisfy
 *    the spec's endpoint table even though the underlying logic is close.
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @PostMapping
    public ResponseEntity<Student> create(@RequestBody Student student) {
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @GetMapping
    public ResponseEntity<Page<Student>> getAll(@PageableDefault(size = 2) Pageable pageable) {
        return ResponseEntity.ok(studentRepository.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Student>> search(@RequestParam String lastName) {
        return ResponseEntity.ok(studentRepository.findByLastNameIgnoreCase(lastName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@PathVariable Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Student> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        if (body.containsKey("departmentId")) {
            Long departmentId = body.get("departmentId");
            Department department = departmentRepository.findById(departmentId).orElse(null);
            if (department == null) {
                return ResponseEntity.notFound().build();
            }
            student.setDepartment(department);
        }
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!studentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        studentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
