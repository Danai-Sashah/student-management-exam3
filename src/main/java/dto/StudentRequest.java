package com.example.studentmanagement.dto;

/*
 * EXAM 3 GRADING NOTE (Section C, Q6 - 10 marks):
 * Two separate issues with this DTO package, worth knowing about even
 * though neither one broke the build:
 *
 * 1. Location: this file lives at src/main/java/dto/StudentRequest.java but
 *    declares `package com.example.studentmanagement.dto;`. Maven's
 *    compiler plugin happens to still find and compile it correctly because
 *    it just gathers every .java file under src/main/java and hands them to
 *    javac together - but most IDEs (and some stricter build setups) expect
 *    the folder path to mirror the package name exactly, i.e. this file
 *    should sit at
 *    src/main/java/com/example/studentmanagement/dto/StudentRequest.java.
 *    Worth fixing even though it "works": it's the kind of mismatch that
 *    causes confusing "class not found" errors the moment someone opens the
 *    project in a different IDE or adds a module boundary.
 * 2. Bigger issue: this class - and age/email/required-field validation
 *    rules on it - is never actually used. StudentController.create()
 *    takes a raw `Student` entity as its @RequestBody instead of
 *    StudentRequest, so none of the @NotBlank/@Email/@Min/@Max annotations
 *    below ever get evaluated. The validation this class defines is
 *    correct; it's just disconnected from the one place (the controller)
 *    that would make Spring actually run it.
 */
import jakarta.validation.constraints.*;
public class StudentRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @Min(0) @Max(120) private Integer age;
    @NotNull private Long departmentId;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }
    public Long getDepartmentId() { return departmentId; }
}