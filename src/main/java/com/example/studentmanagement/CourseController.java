package com.example.studentmanagement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * EXAM 3 GRADING NOTE (Section D, Q7 - POST/GET /api/courses, 3 marks total):
 * - createCourse() reads the body as a raw Map instead of using the
 *   CourseRequest DTO that already exists (dto/CourseRequest.java, with the
 *   @NotBlank/@NotNull validation already on it). Using Map here means
 *   @Valid can't apply at all, so Bean Validation never runs for courses
 *   either, same root cause as in StudentController.
 * - Because Course.department and Course.capacity are required here but
 *   aren't in the spec's Course entity (see the note in Course.java), this
 *   method now *requires* departmentId and capacity in the POST body. The
 *   spec's example only has {code, title, credits}. Run the exam's own
 *   Section F test script (step 2: "Create courses CS101 Intro to
 *   Programming (15), DB201 Databases (15), BUS110 Accounting Basics (10)")
 *   against this endpoint and it returns 400 Bad Request for every one of
 *   those three requests, because none of them include capacity or
 *   departmentId.
 * - `code` uniqueness is never checked before save() (CourseRepository
 *   already has existsByCode(String) - it's just never called). A duplicate
 *   code hits the DB unique constraint and surfaces as an unhandled 500,
 *   not the documented 409. The fix mirrors the department one:
 *     if (courseRepository.existsByCode(code))
 *         throw new DuplicateResourceException("Course " + code + " already exists");
 * - new RuntimeException("Department not found...") on a missing
 *   departmentId does NOT become a 404. GlobalExceptionHandler only listens
 *   for ResourceNotFoundException, MethodArgumentNotValidException and
 *   DuplicateResourceException - a plain RuntimeException isn't any of
 *   those, so Spring's default error handling takes over and this becomes a
 *   generic 500. Throwing `new ResourceNotFoundException(...)` instead would
 *   make the handler you already wrote actually fire.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public CourseController(CourseRepository courseRepository, DepartmentRepository departmentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    // POST /api/courses
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        String title = (String) body.get("title");
        Integer credits = body.get("credits") != null ? ((Number) body.get("credits")).intValue() : null;
        Integer capacity = body.get("capacity") != null ? ((Number) body.get("capacity")).intValue() : null;
        Long departmentId = body.get("departmentId") != null ? ((Number) body.get("departmentId")).longValue() : null;

        if (code == null || title == null || credits == null || capacity == null || departmentId == null) {
            return ResponseEntity.badRequest().build();
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));

        Course course = new Course();
        course.setCode(code);
        course.setTitle(title);
        course.setCredits(credits);
        course.setCapacity(capacity);
        course.setDepartment(department);

        Course saved = courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/courses
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // GET /api/courses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/courses/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return courseRepository.findById(id).map(course -> {
            if (body.get("code") != null) course.setCode((String) body.get("code"));
            if (body.get("title") != null) course.setTitle((String) body.get("title"));
            if (body.get("credits") != null) course.setCredits(((Number) body.get("credits")).intValue());
            if (body.get("capacity") != null) course.setCapacity(((Number) body.get("capacity")).intValue());
            if (body.get("departmentId") != null) {
                Long departmentId = ((Number) body.get("departmentId")).longValue();
                Department department = departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                course.setDepartment(department);
            }
            Course updated = courseRepository.save(course);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/courses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (!courseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}