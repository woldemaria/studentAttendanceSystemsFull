package com.attendance.system.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * AttendanceRecord entity representing a single attendance entry.
 */
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int attendanceId;
    private int studentId;
    private int courseId;
    private LocalDate attendanceDate;
    private LocalTime classTime;
    private AttendanceStatus status;
    private LocalDateTime markedAt;
    private int markedBy;
    private String remarks;
    
    // Transient fields for relationships
    private transient Student student;
    private transient Course course;
    private transient User markedByUser;
    
    public AttendanceRecord() {
        this.markedAt = LocalDateTime.now();
    }
    
    public AttendanceRecord(int studentId, int courseId, LocalDate attendanceDate, 
                           LocalTime classTime, AttendanceStatus status, int markedBy) {
        this();
        this.studentId = studentId;
        this.courseId = courseId;
        this.attendanceDate = attendanceDate;
        this.classTime = classTime;
        this.status = status;
        this.markedBy = markedBy;
    }
    
    // Getters and Setters
    public int getAttendanceId() {
        return attendanceId;
    }
    
    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
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
    
    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }
    
    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }
    
    public LocalTime getClassTime() {
        return classTime;
    }
    
    public void setClassTime(LocalTime classTime) {
        this.classTime = classTime;
    }
    
    public AttendanceStatus getStatus() {
        return status;
    }
    
    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getMarkedAt() {
        return markedAt;
    }
    
    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
    
    public int getMarkedBy() {
        return markedBy;
    }
    
    public void setMarkedBy(int markedBy) {
        this.markedBy = markedBy;
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
    
    public User getMarkedByUser() {
        return markedByUser;
    }
    
    public void setMarkedByUser(User markedByUser) {
        this.markedByUser = markedByUser;
        if (markedByUser != null) {
            this.markedBy = markedByUser.getUserId();
        }
    }
    
    /**
     * Validates if the attendance record is valid for the given date.
     * @param date the date to validate against
     * @return true if the attendance date matches the given date
     */
    public boolean isValidForDate(LocalDate date) {
        return attendanceDate != null && attendanceDate.equals(date);
    }
    
    /**
     * Checks if the attendance record can be modified.
     * Records can be modified within 24 hours of being marked.
     * @return true if can be modified
     */
    public boolean canBeModified() {
        if (markedAt == null) {
            return true; // New record
        }
        LocalDateTime cutoff = markedAt.plusHours(24);
        return LocalDateTime.now().isBefore(cutoff);
    }
    
    /**
     * Gets the display string for the attendance status.
     * @return status display name
     */
    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "Unknown";
    }
    
    /**
     * Checks if this attendance record counts as attended.
     * @return true if status is PRESENT or LATE
     */
    public boolean countsAsAttended() {
        return status != null && status.countsAsAttended();
    }
    
    /**
     * Validates that the attendance date is not in the future.
     * @return true if date is today or in the past
     */
    public boolean isValidDate() {
        return attendanceDate != null && !attendanceDate.isAfter(LocalDate.now());
    }
    
    /**
     * Gets a summary string for this attendance record.
     * @return formatted summary
     */
    public String getSummary() {
        return String.format("%s - %s (%s)", 
                attendanceDate, 
                getStatusDisplay(), 
                classTime != null ? classTime.toString() : "No time");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceRecord that = (AttendanceRecord) o;
        return attendanceId == that.attendanceId ||
               (studentId == that.studentId && 
                courseId == that.courseId && 
                Objects.equals(attendanceDate, that.attendanceDate) &&
                Objects.equals(classTime, that.classTime));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(attendanceId, studentId, courseId, attendanceDate, classTime);
    }
    
    @Override
    public String toString() {
        return String.format("AttendanceRecord{id=%d, studentId=%d, courseId=%d, date=%s, time=%s, status=%s, markedAt=%s}",
                attendanceId, studentId, courseId, attendanceDate, classTime, status, markedAt);
    }
}