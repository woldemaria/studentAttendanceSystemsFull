package com.attendance.system.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Student user class with attendance viewing permissions.
 */
public class Student extends User {
    private static final long serialVersionUID = 1L;
    
    private String studentNumber;
    private String program;
    private int yearLevel;
    private LocalDate enrollmentDate;
    private List<Course> enrolledCourses;
    
    // Student-specific permissions
    private static final List<String> STUDENT_PERMISSIONS = Arrays.asList(
        "ATTENDANCE_READ_OWN", "COURSE_READ_ENROLLED",
        "NOTIFICATION_RECEIVE", "PROFILE_UPDATE_OWN"
    );
    
    public Student() {
        super();
        setRole(UserRole.STUDENT);
        this.enrolledCourses = new ArrayList<>();
    }
    
    public Student(String username, String email, String firstName, String lastName,
                   String studentNumber, String program, int yearLevel) {
        super(username, email, firstName, lastName, UserRole.STUDENT);
        this.studentNumber = studentNumber;
        this.program = program;
        this.yearLevel = yearLevel;
        this.enrollmentDate = LocalDate.now();
        this.enrolledCourses = new ArrayList<>();
    }
    
    @Override
    public List<String> getPermissions() {
        return STUDENT_PERMISSIONS;
    }
    
    @Override
    public String getDisplayName() {
        return "Student: " + getFullName() + " (" + studentNumber + ")";
    }
    
    // Getters and Setters
    public String getStudentNumber() {
        return studentNumber;
    }
    
    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
    
    public String getProgram() {
        return program;
    }
    
    public void setProgram(String program) {
        this.program = program;
    }
    
    public int getYearLevel() {
        return yearLevel;
    }
    
    public void setYearLevel(int yearLevel) {
        if (yearLevel >= 1 && yearLevel <= 4) {
            this.yearLevel = yearLevel;
        } else {
            throw new IllegalArgumentException("Year level must be between 1 and 4");
        }
    }
    
    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }
    
    public void setEnrolledCourses(List<Course> enrolledCourses) {
        this.enrolledCourses = enrolledCourses != null ? enrolledCourses : new ArrayList<>();
    }
    
    /**
     * Enrolls the student in a course.
     * @param course the course to enroll in
     */
    public void enrollInCourse(Course course) {
        if (course != null && !enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
        }
    }
    
    /**
     * Drops the student from a course.
     * @param course the course to drop
     */
    public void dropCourse(Course course) {
        enrolledCourses.remove(course);
    }
    
    /**
     * Checks if the student is enrolled in a specific course.
     * @param course the course to check
     * @return true if enrolled in the course
     */
    public boolean isEnrolledIn(Course course) {
        return course != null && enrolledCourses.contains(course);
    }
    
    /**
     * Gets the number of courses the student is enrolled in.
     * @return number of enrolled courses
     */
    public int getEnrolledCourseCount() {
        return enrolledCourses.size();
    }
    
    /**
     * Checks if the student is eligible for exam based on attendance.
     * Typically requires 75% attendance.
     * @param attendancePercentage the attendance percentage for a course
     * @return true if eligible (>= 75% attendance)
     */
    public boolean isEligibleForExam(double attendancePercentage) {
        return attendancePercentage >= 75.0;
    }
    
    /**
     * Gets the academic year based on enrollment date and year level.
     * @return academic year string
     */
    public String getAcademicYear() {
        if (enrollmentDate == null) {
            return "Unknown";
        }
        int enrollmentYear = enrollmentDate.getYear();
        int currentYear = enrollmentYear + (yearLevel - 1);
        return currentYear + "-" + (currentYear + 1);
    }
}