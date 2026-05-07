package com.attendance.system.service;

import com.attendance.system.dao.AttendanceDAO;
import com.attendance.system.dao.CourseDAO;
import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportServiceImpl.
 * Tests report generation, filtering, and export functionality.
 */
@DisplayName("ReportServiceImpl Tests")
public class ReportServiceImplTest {
    
    private ReportServiceImpl reportService;
    
    @Mock
    private AttendanceDAO attendanceDAO;
    
    @Mock
    private CourseDAO courseDAO;
    
    @Mock
    private UserDAO userDAO;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reportService = new ReportServiceImpl(attendanceDAO, courseDAO, userDAO);
    }
    
    @Test
    @DisplayName("Should generate attendance report with date range filter")
    public void testGenerateAttendanceReport_WithDateRange() throws DatabaseException {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(10);
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        
        when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate, null)).thenReturn(records);
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        assertNotNull(report);
        assertTrue(report.containsKey("records"));
        assertTrue(report.containsKey("statistics"));
        assertTrue(report.containsKey("summary"));
        
        @SuppressWarnings("unchecked")
        List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
        assertEquals(10, reportRecords.size());
    }
    
    @Test
    @DisplayName("Should generate attendance report with student filter")
    public void testGenerateAttendanceReport_WithStudentFilter() throws DatabaseException {
        // Arrange
        int studentId = 1;
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(5);
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        criteria.setStudentId(studentId);
        
        when(attendanceDAO.findWithFilters(studentId, -1, startDate, endDate, null)).thenReturn(records);
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
        assertEquals(5, reportRecords.size());
    }
    
    @Test
    @DisplayName("Should generate attendance report with course filter")
    public void testGenerateAttendanceReport_WithCourseFilter() throws DatabaseException {
        // Arrange
        int courseId = 1;
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(8);
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        criteria.setCourseId(courseId);
        
        when(attendanceDAO.findWithFilters(-1, courseId, startDate, endDate, null)).thenReturn(records);
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
        assertEquals(8, reportRecords.size());
    }
    
    @Test
    @DisplayName("Should generate attendance report with status filter")
    public void testGenerateAttendanceReport_WithStatusFilter() throws DatabaseException {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> allRecords = createSampleAttendanceRecords(10);
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        criteria.setAttendanceStatus("PRESENT");
        
        when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate, null)).thenReturn(allRecords);
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
        assertTrue(reportRecords.size() <= 10);
    }
    
    @Test
    @DisplayName("Should calculate correct statistics in report")
    public void testGenerateAttendanceReport_Statistics() throws DatabaseException {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AttendanceRecord record = new AttendanceRecord();
            record.setStudentId(1);
            record.setCourseId(1);
            record.setAttendanceDate(LocalDate.now());
            record.setClassTime(LocalTime.of(9, 0));
            record.setStatus(i < 7 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT);
            records.add(record);
        }
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        
        when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate, null)).thenReturn(records);
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) report.get("statistics");
        assertEquals(10, statistics.get("totalRecords"));
        assertEquals(7L, statistics.get("presentCount"));
        assertEquals(3L, statistics.get("absentCount"));
        assertEquals(70.0, statistics.get("attendancePercentage"));
    }
    
    @Test
    @DisplayName("Should generate system statistics")
    public void testGenerateSystemStatistics() throws DatabaseException {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(100);
        
        when(attendanceDAO.findByDateRange(startDate, endDate)).thenReturn(records);
        when(attendanceDAO.getStudentsWithLowAttendance(-1, 75.0)).thenReturn(Arrays.asList(1, 2, 3));
        
        // Act
        Map<String, Object> statistics = reportService.generateSystemStatistics(startDate, endDate);
        
        // Assert
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalRecords"));
        assertTrue(statistics.containsKey("averageAttendance"));
        assertTrue(statistics.containsKey("attendanceByStatus"));
        assertTrue(statistics.containsKey("atRiskStudents"));
        assertTrue(statistics.containsKey("trends"));
        assertTrue(statistics.containsKey("topStudents"));
        
        assertEquals(100, statistics.get("totalRecords"));
        assertEquals(3, statistics.get("atRiskStudents"));
    }
    
    @Test
    @DisplayName("Should generate class report")
    public void testGenerateClassReport() throws DatabaseException {
        // Arrange
        int courseId = 1;
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName("Mathematics 101");
        
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Student student = new Student();
            student.setUserId(i);
            student.setFirstName("Student " + i);
            students.add(student);
        }
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(20);
        
        when(courseDAO.findById(courseId)).thenReturn(course);
        when(courseDAO.getEnrolledStudents(courseId)).thenReturn(students);
        when(attendanceDAO.findWithFilters(anyInt(), eq(courseId), eq(startDate), eq(endDate), any()))
            .thenReturn(records);
        
        // Act
        Map<String, Object> report = reportService.generateClassReport(courseId, startDate, endDate);
        
        // Assert
        assertNotNull(report);
        assertTrue(report.containsKey("courseInfo"));
        assertTrue(report.containsKey("records"));
        assertTrue(report.containsKey("statistics"));
        assertTrue(report.containsKey("enrolledStudents"));
        
        assertEquals(course, report.get("courseInfo"));
        assertEquals(5, ((List<?>) report.get("enrolledStudents")).size());
    }
    
    @Test
    @DisplayName("Should generate student report")
    public void testGenerateStudentReport() throws DatabaseException {
        // Arrange
        int studentId = 1;
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        Student student = new Student();
        student.setUserId(studentId);
        student.setFirstName("John");
        student.setLastName("Doe");
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(15);
        
        when(userDAO.findById(studentId)).thenReturn(student);
        when(attendanceDAO.findWithFilters(studentId, -1, startDate, endDate, null)).thenReturn(records);
        
        // Act
        Map<String, Object> report = reportService.generateStudentReport(studentId, startDate, endDate);
        
        // Assert
        assertNotNull(report);
        assertTrue(report.containsKey("studentInfo"));
        assertTrue(report.containsKey("records"));
        assertTrue(report.containsKey("statistics"));
        
        assertEquals(student, report.get("studentInfo"));
        @SuppressWarnings("unchecked")
        List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
        assertEquals(15, reportRecords.size());
    }
    
    @Test
    @DisplayName("Should export report to PDF")
    public void testExportToPDF() throws DatabaseException {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        String fileName = "attendance_report";
        
        // Act
        byte[] pdfBytes = reportService.exportToPDF(reportData, fileName);
        
        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF files start with %PDF
        assertTrue(new String(pdfBytes, 0, Math.min(4, pdfBytes.length)).contains("%PDF") ||
                   pdfBytes[0] == '%');
    }
    
    @Test
    @DisplayName("Should export report to Excel")
    public void testExportToExcel() throws DatabaseException {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        String fileName = "attendance_report";
        
        // Act
        byte[] excelBytes = reportService.exportToExcel(reportData, fileName);
        
        // Assert
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }
    
    @Test
    @DisplayName("Should handle empty records in report generation")
    public void testGenerateAttendanceReport_EmptyRecords() throws DatabaseException {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        
        when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate, null)).thenReturn(new ArrayList<>());
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
        assertEquals(0, reportRecords.size());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) report.get("statistics");
        assertEquals(0, statistics.get("totalRecords"));
        assertEquals(0.0, statistics.get("attendancePercentage"));
    }
    
    @Test
    @DisplayName("Should include generated date in report")
    public void testGenerateAttendanceReport_IncludesGeneratedDate() throws DatabaseException {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        
        when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate, null)).thenReturn(new ArrayList<>());
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        assertTrue(report.containsKey("generatedDate"));
        assertEquals(LocalDate.now(), report.get("generatedDate"));
    }
    
    @Test
    @DisplayName("Should export PDF with correct structure")
    public void testExportToPDF_Structure() throws DatabaseException {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        
        // Act
        byte[] pdfBytes = reportService.exportToPDF(reportData, "test_report");
        
        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Verify it's a valid PDF by checking for PDF markers
        String pdfContent = new String(pdfBytes);
        assertTrue(pdfContent.contains("%PDF") || pdfBytes[0] == '%');
    }
    
    @Test
    @DisplayName("Should export Excel with correct structure")
    public void testExportToExcel_Structure() throws DatabaseException {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        
        // Act
        byte[] excelBytes = reportService.exportToExcel(reportData, "test_report");
        
        // Assert
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
        // Excel files (XLSX) start with PK (ZIP format)
        assertEquals('P', (char) excelBytes[0]);
        assertEquals('K', (char) excelBytes[1]);
    }
    
    @Test
    @DisplayName("Should handle multiple filters in report criteria")
    public void testGenerateAttendanceReport_MultipleFilters() throws DatabaseException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(5);
        
        ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
        criteria.setStudentId(studentId);
        criteria.setCourseId(courseId);
        criteria.setAttendanceStatus("PRESENT");
        
        when(attendanceDAO.findWithFilters(studentId, courseId, startDate, endDate, null)).thenReturn(records);
        
        // Act
        Map<String, Object> report = reportService.generateAttendanceReport(criteria);
        
        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
        assertTrue(reportRecords.size() <= 5);
    }
    
    // Helper methods
    
    private List<AttendanceRecord> createSampleAttendanceRecords(int count) {
        List<AttendanceRecord> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(i + 1);
            record.setStudentId((i % 5) + 1);
            record.setCourseId((i % 3) + 1);
            record.setAttendanceDate(LocalDate.now().minusDays(i));
            record.setClassTime(LocalTime.of(9, 0));
            record.setStatus(i % 2 == 0 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT);
            record.setRemarks("Test remark " + i);
            records.add(record);
        }
        return records;
    }
    
    private Map<String, Object> createSampleReportData() {
        Map<String, Object> reportData = new HashMap<>();
        
        List<AttendanceRecord> records = createSampleAttendanceRecords(10);
        reportData.put("records", records);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", 10);
        statistics.put("presentCount", 7);
        statistics.put("absentCount", 3);
        statistics.put("lateCount", 0);
        statistics.put("excusedCount", 0);
        statistics.put("attendancePercentage", 70.0);
        reportData.put("statistics", statistics);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("Total Records", 10);
        summary.put("Present", 7);
        summary.put("Absent", 3);
        summary.put("Attendance Percentage", "70.0%");
        reportData.put("summary", summary);
        
        return reportData;
    }
}
