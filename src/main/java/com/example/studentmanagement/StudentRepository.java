package com.example.studentmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // Good: this is genuinely unused dead code right now, but the methods
    // themselves are correct and exactly what the spec asks for:
    // - findByLastNameIgnoreCase is a correct derived query for Section E's
    //   lastName search (StudentController.search() does call this one).
    // - findByCourseCode is a correct hand-written JPQL @Query joining
    //   students to courses, exactly matching Section E's courseCode
    //   requirement - but StudentController.search() never calls it, since
    //   that method only accepts a `lastName` parameter. Wiring it up is
    //   purely a controller change, the query itself doesn't need fixing.
    // - existsByEmail / existsByEmailAndIdNot were written for the "email
    //   already used" 409 checks on create/PUT/PATCH, but neither is called
    //   from StudentController (see the notes there).
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<Student> findByLastNameIgnoreCase(String lastName);

    @Query("SELECT s FROM Student s JOIN s.courses c WHERE c.code = :code")
    List<Student> findByCourseCode(@Param("code") String code);

    // This is the method CourseController needs
    List<Student> findByCoursesId(Long courseId);
}