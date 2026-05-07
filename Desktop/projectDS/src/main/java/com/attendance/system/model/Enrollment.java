package com.attendance.system.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Enrollment entity representing a student's enrollment in a course.
 */
public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int enrollmentId;
    private int studentId;
    private int courseId;
    private EnrollmentStatus status;
    private LocalDate enrollmentDate;
    private LocalDate dropDate;
    private String grade;
    private String remarks;
    
    // Transient fields for relationships
    private transient Student student;
    private transient Course course;
    
    public Enrollment() {
        this.status = EnrollmentStatus.ENROLLED;
        this.enrollmentDate = LocalDate.now();
    }
    
    public Enrollment(int studentId, int courseId) {
        this();
        this.studentId = studentId;
        this.courseId = courseId;
    }
    
    // Getters and Setters
    public int getEnrollmentId() {
        return enrollmentId;
    }
    
    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public int getCourseId() {
        return courseId;
    }
    
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }
    
    public EnrollmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
        if (status == EnrollmentStatus.DROPPED && dropDate == null) {
            this.dropDate = LocalDate.now();
        } else if (status != EnrollmentStatus.DROPPED) {
            this.dropDate = null;
        }
    }
    
    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public LocalDate getDropDate() {
        return dropDate;
    }
    
    public void setDropDate(LocalDate dropDate) {
        this.dropDate = dropDate;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
        if (student != null) {
            this.studentId = student.getUserId();
        }
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
        if (course != null) {
            this.courseId = course.getCourseId();
        }
    }
    
    /**
     * Checks if the enrollment is active.
     * @return true if status is ENROLLED
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ENROLLED;
    }
    
    /**
     * Drops the enrollment.
     */
    public void drop() {
        setStatus(EnrollmentStatus.DROPPED);
    }
    
    /**
     * Completes the enrollment (sets status to COMPLETED).
     */
    public void complete() {
        setStatus(EnrollmentStatus.COMPLETED);
    }
    
    /**
     * Gets the duration of enrollment in days.
     * @return number of days enrolled (or was enrolled if dropped)
     */
    public long getEnrollmentDurationDays() {
        if (enrollmentDate == null) {
            return 0;
        }
        
        LocalDate endDate = dropDate != null ? dropDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(enrollmentDate, endDate);
    }
    
    /**
     * Gets a display string for the enrollment status.
     * @return status display string
     */
    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "Unknown";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return enrollmentId == that.enrollmentId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }
    
    @Override
    public String toString() {
        return String.format("Enrollment{id=%d, studentId=%d, courseId=%d, status=%s, enrollmentDate=%s}",
                enrollmentId, studentId, courseId, status, enrollmentDate);
    }
}