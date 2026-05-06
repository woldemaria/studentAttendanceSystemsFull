package com.attendance.system.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Course entity representing a course in the attendance system.
 */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int courseId;
    private String courseCode;
    private String courseName;
    private String description;
    private int credits;
    private int teacherId;
    private String semester;
    private String academicYear;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    // Transient fields for relationships
    private transient Teacher teacher;
    private transient List<Student> enrolledStudents;
    
    public Course() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.enrolledStudents = new ArrayList<>();
    }
    
    public Course(String courseCode, String courseName, int credits, 
                  String semester, String academicYear) {
        this();
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.semester = semester;
        this.academicYear = academicYear;
    }
    
    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getCredits() {
        return credits;
    }
    
    public void setCredits(int credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive");
        }
        this.credits = credits;
    }
    
    public int getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public String getAcademicYear() {
        return academicYear;
    }
    
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Teacher getTeacher() {
        return teacher;
    }
    
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
        if (teacher != null) {
            this.teacherId = teacher.getUserId();
        }
    }
    
    public List<Student> getEnrolledStudents() {
        return enrolledStudents != null ? enrolledStudents : new ArrayList<>();
    }
    
    public void setEnrolledStudents(List<Student> enrolledStudents) {
        this.enrolledStudents = enrolledStudents != null ? enrolledStudents : new ArrayList<>();
    }
    
    /**
     * Adds a student to the enrolled students list.
     * @param student the student to enroll
     */
    public void enrollStudent(Student student) {
        if (student != null && !getEnrolledStudents().contains(student)) {
            getEnrolledStudents().add(student);
        }
    }
    
    /**
     * Removes a student from the enrolled students list.
     * @param student the student to remove
     */
    public void removeStudent(Student student) {
        getEnrolledStudents().remove(student);
    }
    
    /**
     * Gets the number of enrolled students.
     * @return number of enrolled students
     */
    public int getEnrollmentCount() {
        return getEnrolledStudents().size();
    }
    
    /**
     * Gets the full course display name.
     * @return courseCode + " - " + courseName
     */
    public String getFullName() {
        return courseCode + " - " + courseName;
    }
    
    /**
     * Checks if the course is currently active and in session.
     * @return true if active
     */
    public boolean isCurrentlyActive() {
        return isActive;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseId == course.courseId && 
               Objects.equals(courseCode, course.courseCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(courseId, courseCode);
    }
    
    @Override
    public String toString() {
        return String.format("Course{id=%d, code='%s', name='%s', credits=%d, semester='%s', year='%s', active=%s}",
                courseId, courseCode, courseName, credits, semester, academicYear, isActive);
    }
}