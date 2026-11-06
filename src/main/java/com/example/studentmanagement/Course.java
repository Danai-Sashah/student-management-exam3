package com.example.studentmanagement;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/*
 * EXAM 3 GRADING NOTE (Section B, Q4 - 6 marks):
 * The required fields were id, code, title, credits and the inverse
 * `students` Set - that part is correct below. But two extra fields were
 * added that are NOT in the spec: `capacity` and a `department` @ManyToOne.
 *
 * The relationship diagram at the top of the exam is explicit about this:
 *
 *   Department 1---* Student *---* Course
 *
 * There is no Department-Course line. A course in this model does not
 * belong to a department at all - only students do. Adding `department`
 * here invents a relationship the spec never asked for, and it has a real
 * cost: CourseController.createCourse() now requires a departmentId in the
 * POST /api/courses body (see the note there). The exam's own test script
 * (Section F, step 2) creates courses with just {code, title, credits} -
 * with `department` marked NOT NULL here, that request would fail. The
 * `capacity` field has the same problem for the same reason: it's required
 * server-side but the spec's example request and the CourseRequest DTO
 * (dto/CourseRequest.java) never mention it, so the two don't agree with
 * each other. When you add a field the spec doesn't ask for, make sure it's
 * at least optional, or check it against every request shape the spec gives
 * you - the mismatch here would fail step 2 of the exam's own test script.
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // This part is correct: mappedBy = "courses" tells JPA this is the
    // inverse (non-owning) side, pointing at the `courses` field on Student,
    // which is where the @JoinTable (and therefore the foreign keys) lives.
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Set<Student> getStudents() { return students; }
    public void setStudents(Set<Student> students) { this.students = students; }
}