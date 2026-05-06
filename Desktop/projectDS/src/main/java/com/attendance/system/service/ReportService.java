package com.attendance.system.service;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.AttendanceRecord;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for generating attendance reports with filtering capabilities.
 * Provides methods for creating attendance reports with various filter criteria,
 * system-wide statistics, and class-specific reports.
 */
public interface ReportService extends Serializable {

    /**
     * Generates an attendance report with specified filter criteria.
     * 
     * @param criteria the report filter criteria
     * @return a map containing report data with keys: "records", "statistics", "summary"
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> generateAttendanceReport(ReportCriteria criteria) throws DatabaseException;

    /**
     * Generates system-wide attendance statistics and trends for administrators.
     * 
     * @param startDate the start date for the report period
     * @param endDate the end date for the report period
     * @return a map containing system statistics with keys: "totalRecords", "averageAttendance",
     *         "attendanceByStatus", "trends", "topStudents", "atRiskStudents"
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> generateSystemStatistics(LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Generates class-specific attendance report for a teacher.
     * 
     * @param courseId the course ID
     * @param startDate the start date for the report period
     * @param endDate the end date for the report period
     * @return a map containing class report data with keys: "courseInfo", "records", "statistics", "summary"
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> generateClassReport(int courseId, LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Generates a student-specific attendance report.
     * 
     * @param studentId the student ID
     * @param startDate the start date for the report period
     * @param endDate the end date for the report period
     * @return a map containing student report data with keys: "studentInfo", "records", "statistics", "summary"
     * @throws DatabaseException if database operation fails
     */
    Map<String, Object> generateStudentReport(int studentId, LocalDate startDate, LocalDate endDate) throws DatabaseException;

    /**
     * Exports a report to PDF format.
     * 
     * @param reportData the report data to export
     * @param fileName the output file name (without extension)
     * @return the PDF file as byte array
     * @throws DatabaseException if export operation fails
     */
    byte[] exportToPDF(Map<String, Object> reportData, String fileName) throws DatabaseException;

    /**
     * Exports a report to Excel format.
     * 
     * @param reportData the report data to export
     * @param fileName the output file name (without extension)
     * @return the Excel file as byte array
     * @throws DatabaseException if export operation fails
     */
    byte[] exportToExcel(Map<String, Object> reportData, String fileName) throws DatabaseException;

    /**
     * Represents filter criteria for attendance reports.
     */
    class ReportCriteria implements Serializable {
        private static final long serialVersionUID = 1L;

        private LocalDate startDate;
        private LocalDate endDate;
        private Integer studentId;
        private Integer courseId;
        private Integer teacherId;
        private String attendanceStatus;

        public ReportCriteria() {
        }

        public ReportCriteria(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters and Setters
        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Integer getStudentId() {
            return studentId;
        }

        public void setStudentId(Integer studentId) {
            this.studentId = studentId;
        }

        public Integer getCourseId() {
            return courseId;
        }

        public void setCourseId(Integer courseId) {
            this.courseId = courseId;
        }

        public Integer getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Integer teacherId) {
            this.teacherId = teacherId;
        }

        public String getAttendanceStatus() {
            return attendanceStatus;
        }

        public void setAttendanceStatus(String attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
        }
    }
}
