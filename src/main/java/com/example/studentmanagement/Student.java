package com.example.studentmanagement;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/*
 * EXAM 3 GRADING NOTE (Section B, Q5 - 13 marks): this entity is the
 * strongest part of the submission.
 * - The old `course` String field is gone: correct (1 mark).
 * - @ManyToOne(optional = false) + @JoinColumn(name = "department_id") gives
 *   a NOT NULL foreign key column named exactly as asked: correct (5 marks).
 * - The @ManyToMany + @JoinTable below creates student_courses(student_id,
 *   course_id) with Student as the owning side (it's the side that declares
 *   @JoinTable, not mappedBy): correct (5 marks). Good use of Set rather
 *   than List too, per the exam's own hint.
 *
 * One thing worth knowing even though it's not wrong here: the Bean
 * Validation rules for age (16-100), required fields etc. belong on the
 * *request DTO* (dto/StudentRequest.java), not on this entity - and that's
 * exactly where they were put. That's the right instinct. The gap is that
 * StudentController never actually uses StudentRequest (see the note in
 * StudentController.java) - the validation annotations exist but nothing
 * triggers them, so they currently have zero effect at runtime.
 */
@Entity
@Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private Integer age;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany
    @JoinTable(name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();

    // getters and setters
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Set<Course> getCourses() { return courses; }
}