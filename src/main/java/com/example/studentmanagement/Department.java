package com.example.studentmanagement;

import jakarta.persistence.*;

/*
 * EXAM 3 GRADING NOTE (Section B, Q3 - 6 marks):
 * The spec's Department entity needed FOUR things: id, name, building, and a
 * @OneToMany students field (the inverse side, mapped by Student.department).
 * Only id and name are here. Two whole fields are missing:
 *
 *   private String building;   // optional, plain column - no annotation needed
 *
 *   @OneToMany(mappedBy = "department")
 *   private List<Student> students = new ArrayList<>();
 *
 * Why this matters beyond "it's in the spec": without the inverse `students`
 * list, Department has no way to answer "who belongs to me" from Java code.
 * That's exactly what GET /api/departments/{id}/students needs (that endpoint
 * is missing from DepartmentController - see the note there), and it's also
 * what you'd use to check "does this department still have students?" before
 * allowing a DELETE (also missing - see DepartmentController's delete note).
 * mappedBy="department" tells JPA "don't create a second foreign key for this
 * side, just read the department_id column that Student already owns" - it's
 * what keeps Department from trying to own its own join column.
 */
@Entity
@Table(name = "departments")
public class Department {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}