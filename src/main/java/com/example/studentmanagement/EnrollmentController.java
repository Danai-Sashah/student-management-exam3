package com.example.studentmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * EXAM 3 GRADING NOTE (Section D, Q7 - enrol/unenrol/list-student-courses,
 * 7 marks total): the logic here is close - exists checks, a 409 on
 * duplicate enrolment, deleting the join row rather than the course - but
 * it's built around a URL shape and a persistence model the spec didn't ask
 * for, so none of the three endpoints below actually match what's graded:
 *
 * - The spec wants POST/DELETE /api/students/{id}/courses/{courseId} and
 *   GET /api/students/{id}/courses. This file instead exposes
 *   POST /api/enrollments (studentId/courseId in the JSON body) and
 *   DELETE /api/enrollments?studentId=&courseId= (query params) - a
 *   student-centric nested resource became its own top-level resource.
 * - Those two endpoints are also built on the separate `Enrollment` entity
 *   (Enrollment.java), going through EnrollmentRepository, instead of using
 *   Student.courses (the @ManyToMany the rest of the app - and the
 *   student_courses join table the spec names - is actually built around).
 *   The result is two different, disconnected ways of recording "student X
 *   is taking course Y" in the same codebase: enrolling via this endpoint
 *   writes a row to `enrollments`, but Student.getCourses() (used by
 *   StudentResponse, and by anything checking Student.courses) reads from
 *   `student_courses` and would never see it. A student enrolled through
 *   this endpoint wouldn't show up in their own `courses` list.
 * - Both POST and DELETE here return the raw `Enrollment` entity / 204 with
 *   no body. The spec wants enrol to return 200 with the *student's* JSON
 *   (the same StudentResponse shape used everywhere else), so the caller
 *   can see the updated courses list without a second request.
 * - `new RuntimeException("Student not found")` / `"Course not found"` -
 *   same issue as in CourseController: GlobalExceptionHandler never sees
 *   these because it isn't listening for plain RuntimeException, so a
 *   missing student or course here becomes an unhandled 500 instead of the
 *   documented 404.
 *
 * What this would look like built the way the spec asks: add to
 * StudentController (not a separate controller) something like
 *   @PostMapping("/{id}/courses/{courseId}")
 *   ResponseEntity<StudentResponse> enrol(@PathVariable Long id, @PathVariable Long courseId) {
 *       Student student = studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(...));
 *       Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException(...));
 *       if (student.getCourses().contains(course)) throw new DuplicateResourceException(...);
 *       student.getCourses().add(course);
 *       return ResponseEntity.ok(new StudentResponse(studentRepository.save(student)));
 *   }
 * with the DELETE and GET variants following the same pattern against
 * student.getCourses() directly - no Enrollment entity needed for the
 * graded scope (it's genuinely useful for the *bonus* challenge, which asks
 * for enrolledOn/grade columns, but this version of it doesn't store either
 * of those, so it isn't earning the bonus either - it's just an extra,
 * disconnected table).
 */
@RestController
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/api/enrollments")
    public ResponseEntity<?> create(@RequestBody Map<String, Long> body) {
        Long studentId = body.get("studentId");
        Long courseId = body.get("courseId");

        if (studentId == null || courseId == null) {
            return ResponseEntity.badRequest().body("studentId and courseId are required");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already enrolled");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        enrollmentRepository.save(enrollment);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/api/courses/{courseId}/students")
    public ResponseEntity<?> getStudentsByCourse(@PathVariable Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.notFound().build();
        }
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Student> students = enrollments.stream()
                .map(Enrollment::getStudent)
                .toList();
        return ResponseEntity.ok(students);
    }

    @DeleteMapping("/api/enrollments")
    public ResponseEntity<?> delete(@RequestParam Long studentId, @RequestParam Long courseId) {
        var enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (enrollmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        enrollmentRepository.delete(enrollmentOpt.get());
        return ResponseEntity.noContent().build();
    }
}