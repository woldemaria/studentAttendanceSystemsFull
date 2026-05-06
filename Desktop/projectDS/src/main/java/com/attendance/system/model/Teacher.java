package com.attendance.system.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Teacher user class with course and attendance management permissions.
 */
public class Teacher extends User {
    private static final long serialVersionUID = 1L;
    
    private String employeeId;
    private String department;
    private String specialization;
    private List<Course> assignedCourses;
    
    // Teacher-specific permissions
    private static final List<String> TEACHER_PERMISSIONS = Arrays.asList(
        "ATTENDANCE_CREATE", "ATTENDANCE_READ", "ATTENDANCE_UPDATE",
        "COURSE_READ", "STUDENT_READ",
        "REPORT_GENERATE_CLASS", "REPORT_EXPORT",
        "NOTIFICATION_RECEIVE"
    );
    
    public Teacher() {
        super();
        setRole(UserRole.TEACHER);
        this.assignedCourses = new ArrayList<>();
    }
    
    public Teacher(String username, String email, String firstName, String lastName, 
                   String employeeId, String department) {
        super(username, email, firstName, lastName, UserRole.TEACHER);
        this.employeeId = employeeId;
        this.department = department;
        this.assignedCourses = new ArrayList<>();
    }
    
    @Override
    public List<String> getPermissions() {
        return TEACHER_PERMISSIONS;
    }
    
    @Override
    public String getDisplayName() {
        return "Teacher: " + getFullName() + " (" + department + ")";
    }
    
    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public List<Course> getAssignedCourses() {
        return assignedCourses;
    }
    
    public void setAssignedCourses(List<Course> assignedCourses) {
        this.assignedCourses = assignedCourses != null ? assignedCourses : new ArrayList<>();
    }
    
    /**
     * Adds a course to the teacher's assigned courses.
     * @param course the course to assign
     */
    public void assignCourse(Course course) {
        if (course != null && !assignedCourses.contains(course)) {
            assignedCourses.add(course);
        }
    }
    
    /**
     * Removes a course from the teacher's assigned courses.
     * @param course the course to remove
     */
    public void removeCourse(Course course) {
        assignedCourses.remove(course);
    }
    
    /**
     * Checks if the teacher can mark attendance for a specific course.
     * @param course the course to check
     * @return true if the teacher is assigned to the course
     */
    public boolean canMarkAttendance(Course course) {
        return course != null && assignedCourses.contains(course);
    }
    
    /**
     * Gets the number of courses assigned to this teacher.
     * @return number of assigned courses
     */
    public int getCourseCount() {
        return assignedCourses.size();
    }
}