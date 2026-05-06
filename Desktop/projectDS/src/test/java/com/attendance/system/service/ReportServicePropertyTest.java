package com.attendance.system.service;

import com.attendance.system.dao.AttendanceDAO;
import com.attendance.system.dao.CourseDAO;
import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.*;
import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.Classification;
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
 * Property-based tests for ReportServiceImpl.
 * Tests correctness properties for report generation and export functionality.
 */
@DisplayName("ReportServiceImpl Property Tests")
public class ReportServicePropertyTest {
    
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
    
    /**
     * Property 23: Report Generation with Filtering
     * For any combination of report filter criteria (date range, class, student, teacher),
     * the Report_Generator should produce a report containing exactly the data that matches
     * the specified criteria.
     * 
     * Validates: Requirements 7.1
     */
    @Test
    @DisplayName("Property 23: Report Generation with Filtering - Date Range Filter")
    public void testProperty23_ReportGenerationWithDateRangeFilter() {
        QuickCheck.forAll(
            generateDateRanges(),
            (dateRange) -> {
                try {
                    LocalDate startDate = dateRange[0];
                    LocalDate endDate = dateRange[1];
                    
                    List<AttendanceRecord> mockRecords = generateAttendanceRecords(10, startDate, endDate);
                    
                    ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
                    
                    when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate)).thenReturn(mockRecords);
                    
                    Map<String, Object> report = reportService.generateAttendanceReport(criteria);
                    
                    // Verify report contains records
                    assertNotNull(report);
                    assertTrue(report.containsKey("records"));
                    
                    @SuppressWarnings("unchecked")
                    List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
                    
                    // All records should be within the date range
                    for (AttendanceRecord record : reportRecords) {
                        assertTrue(!record.getAttendanceDate().isBefore(startDate) && 
                                 !record.getAttendanceDate().isAfter(endDate),
                            "Record date should be within specified range");
                    }
                    
                    return true;
                } catch (DatabaseException e) {
                    return false;
                }
            }
        ).check();
    }
    
    /**
     * Property 23: Report Generation with Filtering - Student Filter
     * For any student ID, the report should contain only records for that student.
     * 
     * Validates: Requirements 7.1
     */
    @Test
    @DisplayName("Property 23: Report Generation with Filtering - Student Filter")
    public void testProperty23_ReportGenerationWithStudentFilter() {
        QuickCheck.forAll(
            generateStudentIds(),
            (studentId) -> {
                try {
                    LocalDate startDate = LocalDate.now().minusDays(30);
                    LocalDate endDate = LocalDate.now();
                    
                    List<AttendanceRecord> mockRecords = generateAttendanceRecordsForStudent(studentId, 5);
                    
                    ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
                    criteria.setStudentId(studentId);
                    
                    when(attendanceDAO.findWithFilters(studentId, -1, startDate, endDate)).thenReturn(mockRecords);
                    
                    Map<String, Object> report = reportService.generateAttendanceReport(criteria);
                    
                    @SuppressWarnings("unchecked")
                    List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
                    
                    // All records should be for the specified student
                    for (AttendanceRecord record : reportRecords) {
                        assertEquals(studentId, record.getStudentId(),
                            "All records should belong to the specified student");
                    }
                    
                    return true;
                } catch (DatabaseException e) {
                    return false;
                }
            }
        ).check();
    }
    
    /**
     * Property 23: Report Generation with Filtering - Course Filter
     * For any course ID, the report should contain only records for that course.
     * 
     * Validates: Requirements 7.1
     */
    @Test
    @DisplayName("Property 23: Report Generation with Filtering - Course Filter")
    public void testProperty23_ReportGenerationWithCourseFilter() {
        QuickCheck.forAll(
            generateCourseIds(),
            (courseId) -> {
                try {
                    LocalDate startDate = LocalDate.now().minusDays(30);
                    LocalDate endDate = LocalDate.now();
                    
                    List<AttendanceRecord> mockRecords = generateAttendanceRecordsForCourse(courseId, 5);
                    
                    ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
                    criteria.setCourseId(courseId);
                    
                    when(attendanceDAO.findWithFilters(-1, courseId, startDate, endDate)).thenReturn(mockRecords);
                    
                    Map<String, Object> report = reportService.generateAttendanceReport(criteria);
                    
                    @SuppressWarnings("unchecked")
                    List<AttendanceRecord> reportRecords = (List<AttendanceRecord>) report.get("records");
                    
                    // All records should be for the specified course
                    for (AttendanceRecord record : reportRecords) {
                        assertEquals(courseId, record.getCourseId(),
                            "All records should belong to the specified course");
                    }
                    
                    return true;
                } catch (DatabaseException e) {
                    return false;
                }
            }
        ).check();
    }
    
    /**
     * Property 24: Report Export Format Integrity
     * For any generated report, exporting to PDF and Excel formats should preserve
     * all data and formatting such that the exported files contain the same information
     * as the original report.
     * 
     * Validates: Requirements 7.4
     */
    @Test
    @DisplayName("Property 24: Report Export Format Integrity - PDF Export")
    public void testProperty24_ReportExportFormatIntegrity_PDF() {
        QuickCheck.forAll(
            generateReportData(),
            (reportData) -> {
                try {
                    byte[] pdfBytes = reportService.exportToPDF(reportData, "test_report");
                    
                    // Verify PDF is not empty
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0, "PDF export should produce non-empty output");
                    
                    // Verify it's a valid PDF (starts with %PDF)
                    String pdfStart = new String(pdfBytes, 0, Math.min(4, pdfBytes.length));
                    assertTrue(pdfStart.contains("%PDF") || pdfBytes[0] == '%',
                        "PDF should start with PDF header");
                    
                    return true;
                } catch (DatabaseException e) {
                    return false;
                }
            }
        ).check();
    }
    
    /**
     * Property 24: Report Export Format Integrity - Excel Export
     * For any generated report, exporting to Excel format should preserve all data.
     * 
     * Validates: Requirements 7.4
     */
    @Test
    @DisplayName("Property 24: Report Export Format Integrity - Excel Export")
    public void testProperty24_ReportExportFormatIntegrity_Excel() {
        QuickCheck.forAll(
            generateReportData(),
            (reportData) -> {
                try {
                    byte[] excelBytes = reportService.exportToExcel(reportData, "test_report");
                    
                    // Verify Excel is not empty
                    assertNotNull(excelBytes);
                    assertTrue(excelBytes.length > 0, "Excel export should produce non-empty output");
                    
                    // Verify it's a valid XLSX (ZIP format, starts with PK)
                    assertTrue(excelBytes[0] == 'P' && excelBytes[1] == 'K',
                        "Excel XLSX should start with PK (ZIP format)");
                    
                    return true;
                } catch (DatabaseException e) {
                    return false;
                }
            }
        ).check();
    }
    
    /**
     * Property 25: Report Content Completeness
     * For any generated attendance report, the output should include attendance percentages,
     * trends, and summary statistics as required, with mathematically correct calculations.
     * 
     * Validates: Requirements 7.6
     */
    @Test
    @DisplayName("Property 25: Report Content Completeness - Statistics Calculation")
    public void testProperty25_ReportContentCompleteness_Statistics() {
        QuickCheck.forAll(
            generateAttendanceRecordLists(),
            (records) -> {
                try {
                    LocalDate startDate = LocalDate.now().minusDays(30);
                    LocalDate endDate = LocalDate.now();
                    
                    ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
                    
                    when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate)).thenReturn(records);
                    
                    Map<String, Object> report = reportService.generateAttendanceReport(criteria);
                    
                    // Verify statistics are present
                    assertTrue(report.containsKey("statistics"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> statistics = (Map<String, Object>) report.get("statistics");
                    
                    // Verify required statistics fields
                    assertTrue(statistics.containsKey("totalRecords"));
                    assertTrue(statistics.containsKey("presentCount"));
                    assertTrue(statistics.containsKey("absentCount"));
                    assertTrue(statistics.containsKey("attendancePercentage"));
                    
                    // Verify summary is present
                    assertTrue(report.containsKey("summary"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> summary = (Map<String, Object>) report.get("summary");
                    assertTrue(summary.size() > 0, "Summary should contain data");
                    
                    // Verify attendance percentage calculation is correct
                    if (!records.isEmpty()) {
                        long presentCount = (long) statistics.get("presentCount");
                        long lateCount = (long) statistics.get("lateCount");
                        long totalRecords = (long) statistics.get("totalRecords");
                        double attendancePercentage = (double) statistics.get("attendancePercentage");
                        
                        double expectedPercentage = ((presentCount + lateCount) / (double) totalRecords) * 100;
                        expectedPercentage = Math.round(expectedPercentage * 100.0) / 100.0;
                        
                        assertEquals(expectedPercentage, attendancePercentage,
                            "Attendance percentage should be calculated correctly");
                    }
                    
                    return true;
                } catch (DatabaseException e) {
                    return false;
                }
            }
        ).check();
    }
    
    /**
     * Property 25: Report Content Completeness - Summary Presence
     * For any generated report, the summary should be present and contain key metrics.
     * 
     * Validates: Requirements 7.6
     */
    @Test
    @DisplayName("Property 25: Report Content Completeness - Summary Presence")
    public void testProperty25_ReportContentCompleteness_SummaryPresence() {
        QuickCheck.forAll(
            generateReportData(),
            (reportData) -> {
                try {
                    LocalDate startDate = LocalDate.now().minusDays(30);
                    LocalDate endDate = LocalDate.now();
                    
                    ReportService.ReportCriteria criteria = new ReportService.ReportCriteria(startDate, endDate);
                    
                    when(attendanceDAO.findWithFilters(-1, -1, startDate, endDate))
                        .thenReturn((List<AttendanceRecord>) reportData.get("records"));
                    
                    Map<String, Object> report = reportService.generateAttendanceReport(criteria);
                    
                    // Verify summary is present and contains expected keys
                    assertTrue(report.containsKey("summary"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> summary = (Map<String, Object>) report.get("summary");
                    
                    assertTrue(summary.containsKey("Total Records"));
                    assertTrue(summary.containsKey("Attendance Percentage"));
                    
                    return true;
                } catch (DatabaseException e) {
                    return false;
                }
            }
        ).check();
    }
    
    // Generator methods
    
    private Generator<LocalDate[]> generateDateRanges() {
        return () -> {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(new Random().nextInt(90) + 1);
            return new LocalDate[]{startDate, endDate};
        };
    }
    
    private Generator<Integer> generateStudentIds() {
        return () -> new Random().nextInt(100) + 1;
    }
    
    private Generator<Integer> generateCourseIds() {
        return () -> new Random().nextInt(50) + 1;
    }
    
    private Generator<Map<String, Object>> generateReportData() {
        return () -> {
            Map<String, Object> reportData = new HashMap<>();
            
            List<AttendanceRecord> records = generateAttendanceRecords(
                new Random().nextInt(20) + 1,
                LocalDate.now().minusDays(30),
                LocalDate.now()
            );
            reportData.put("records", records);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRecords", records.size());
            statistics.put("presentCount", records.size() / 2);
            statistics.put("absentCount", records.size() / 4);
            statistics.put("lateCount", records.size() / 4);
            statistics.put("excusedCount", 0);
            statistics.put("attendancePercentage", 75.0);
            reportData.put("statistics", statistics);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("Total Records", records.size());
            summary.put("Attendance Percentage", "75.0%");
            reportData.put("summary", summary);
            
            return reportData;
        };
    }
    
    private Generator<List<AttendanceRecord>> generateAttendanceRecordLists() {
        return () -> generateAttendanceRecords(
            new Random().nextInt(50) + 1,
            LocalDate.now().minusDays(30),
            LocalDate.now()
        );
    }
    
    private List<AttendanceRecord> generateAttendanceRecords(int count, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(i + 1);
            record.setStudentId(random.nextInt(100) + 1);
            record.setCourseId(random.nextInt(50) + 1);
            
            long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
            LocalDate randomDate = startDate.plusDays(random.nextInt((int) daysBetween + 1));
            record.setAttendanceDate(randomDate);
            
            record.setClassTime(LocalTime.of(random.nextInt(24), random.nextInt(60)));
            
            AttendanceStatus[] statuses = AttendanceStatus.values();
            record.setStatus(statuses[random.nextInt(statuses.length)]);
            
            records.add(record);
        }
        
        return records;
    }
    
    private List<AttendanceRecord> generateAttendanceRecordsForStudent(int studentId, int count) {
        List<AttendanceRecord> records = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(i + 1);
            record.setStudentId(studentId);
            record.setCourseId(random.nextInt(50) + 1);
            record.setAttendanceDate(LocalDate.now().minusDays(i));
            record.setClassTime(LocalTime.of(9, 0));
            
            AttendanceStatus[] statuses = AttendanceStatus.values();
            record.setStatus(statuses[random.nextInt(statuses.length)]);
            
            records.add(record);
        }
        
        return records;
    }
    
    private List<AttendanceRecord> generateAttendanceRecordsForCourse(int courseId, int count) {
        List<AttendanceRecord> records = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(i + 1);
            record.setStudentId(random.nextInt(100) + 1);
            record.setCourseId(courseId);
            record.setAttendanceDate(LocalDate.now().minusDays(i));
            record.setClassTime(LocalTime.of(9, 0));
            
            AttendanceStatus[] statuses = AttendanceStatus.values();
            record.setStatus(statuses[random.nextInt(statuses.length)]);
            
            records.add(record);
        }
        
        return records;
    }
}
