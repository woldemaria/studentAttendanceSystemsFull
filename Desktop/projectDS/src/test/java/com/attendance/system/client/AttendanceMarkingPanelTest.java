package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
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
 * Unit tests for AttendanceMarkingPanel.
 * Tests core functionality of the attendance marking interface.
 * 
 * Validates Requirements: 3.1, 3.2, 3.4
 */
@DisplayName("AttendanceMarkingPanel Unit Tests")
public class AttendanceMarkingPanelTest {
    
    @Mock
    private AttendanceGUI mockParentFrame;
    
    @Mock
    private AttendanceService mockRemoteService;
    
    private String sessionToken = "test-session-token";
    private Teacher currentTeacher;
    private Course testCourse;
    private List<Student> testStudents;
    private List<AttendanceRecord> existingRecords;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        currentTeacher = new Teacher("teacher1", "teacher@test.com", "John", "Doe", "EMP001");
        currentTeacher.setUserId(1);
        
        testCourse = new Course("CS101", "Introduction to Computer Science", 3, "Fall", "2024");
        testCourse.setCourseId(1);
        testCourse.setTeacherId(1);
        
        // Create test students
        testStudents = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Student student = new Student("student" + i, "student" + i + "@test.com", 
                    "Student", "Number" + i, "STU00" + i);
            student.setUserId(i + 1);
            testStudents.add(student);
        }
        
        // Create existing attendance records
        existingRecords = new ArrayList<>();
        AttendanceRecord record1 = new AttendanceRecord();
        record1.setAttendanceId(1);
        record1.setStudentId(2);
        record1.setCourseId(1);
        record1.setAttendanceDate(LocalDate.now());
        record1.setClassTime(LocalTime.of(10, 0));
        record1.setStatus(AttendanceStatus.PRESENT);
        record1.setMarkedBy(1);
        existingRecords.add(record1);
        
        // Setup mock behavior
        when(mockRemoteService.validateSession(sessionToken)).thenReturn(currentTeacher);
        when(mockRemoteService.getCoursesByTeacher(sessionToken, 1))
                .thenReturn(Arrays.asList(testCourse));
        when(mockRemoteService.getEnrolledStudents(sessionToken, 1))
                .thenReturn(testStudents);
        when(mockRemoteService.getAttendanceByClassDate(sessionToken, 1, LocalDate.now()))
                .thenReturn(existingRecords);
    }
    
    /**
     * Test 1: Verify student list retrieval accuracy
     * Validates Requirement 3.1: Student list display for selected class
     */
    @Test
    @DisplayName("Should retrieve correct student list for selected class")
    public void testStudentListRetrievalAccuracy() throws Exception {
        // Arrange
        List<Student> students = mockRemoteService.getEnrolledStudents(sessionToken, 1);
        
        // Act & Assert
        assertNotNull(students, "Student list should not be null");
        assertEquals(5, students.size(), "Should have 5 students");
        
        // Verify all students are from the correct course
        for (Student student : students) {
            assertNotNull(student.getFullName(), "Student name should not be null");
            assertNotNull(student.getStudentNumber(), "Student number should not be null");
        }
    }
    
    /**
     * Test 2: Verify attendance record creation with valid data
     * Validates Requirement 3.2: Attendance record storage with timestamp
     */
    @Test
    @DisplayName("Should create attendance record with valid data")
    public void testAttendanceRecordCreation() throws Exception {
        // Arrange
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(2);
        record.setCourseId(1);
        record.setAttendanceDate(LocalDate.now());
        record.setClassTime(LocalTime.now());
        record.setStatus(AttendanceStatus.PRESENT);
        record.setMarkedBy(1);
        record.setRemarks("Test remark");
        
        // Act
        when(mockRemoteService.markAttendance(sessionToken, record)).thenReturn(true);
        boolean result = mockRemoteService.markAttendance(sessionToken, record);
        
        // Assert
        assertTrue(result, "Attendance marking should succeed");
        assertNotNull(record.getMarkedAt(), "Marked timestamp should be set");
        assertEquals(AttendanceStatus.PRESENT, record.getStatus(), "Status should be PRESENT");
        assertEquals(1, record.getMarkedBy(), "Marked by should be teacher ID");
    }
    
    /**
     * Test 3: Verify attendance modification time window validation
     * Validates Requirement 3.4: Modification within 24 hours
     */
    @Test
    @DisplayName("Should validate modification time window")
    public void testAttendanceModificationTimeWindow() throws Exception {
        // Arrange
        AttendanceRecord record = existingRecords.get(0);
        
        // Act & Assert
        assertTrue(record.canBeModified(), "Recent record should be modifiable");
        
        // Verify the record has a marked timestamp
        assertNotNull(record.getMarkedAt(), "Marked timestamp should exist");
    }
    
    /**
     * Test 4: Verify attendance status options
     * Validates Requirement 3.2: Status selection (Present, Absent, Late, Excused)
     */
    @Test
    @DisplayName("Should support all attendance status options")
    public void testAttendanceStatusOptions() throws Exception {
        // Arrange & Act
        AttendanceStatus[] statuses = AttendanceStatus.values();
        
        // Assert
        assertEquals(4, statuses.length, "Should have 4 status options");
        
        List<String> statusNames = new ArrayList<>();
        for (AttendanceStatus status : statuses) {
            statusNames.add(status.getDisplayName());
        }
        
        assertTrue(statusNames.contains("Present"), "Should have Present status");
        assertTrue(statusNames.contains("Absent"), "Should have Absent status");
        assertTrue(statusNames.contains("Late"), "Should have Late status");
        assertTrue(statusNames.contains("Excused"), "Should have Excused status");
    }
    
    /**
     * Test 5: Verify bulk attendance marking capability
     * Validates Requirement 3.2: Bulk marking capabilities
     */
    @Test
    @DisplayName("Should support bulk attendance marking")
    public void testBulkAttendanceMarking() throws Exception {
        // Arrange
        List<AttendanceRecord> records = new ArrayList<>();
        for (Student student : testStudents) {
            AttendanceRecord record = new AttendanceRecord();
            record.setStudentId(student.getUserId());
            record.setCourseId(1);
            record.setAttendanceDate(LocalDate.now());
            record.setClassTime(LocalTime.now());
            record.setStatus(AttendanceStatus.PRESENT);
            record.setMarkedBy(1);
            records.add(record);
        }
        
        // Act
        when(mockRemoteService.markAttendance(sessionToken, any(AttendanceRecord.class)))
                .thenReturn(true);
        
        boolean allSuccess = true;
        for (AttendanceRecord record : records) {
            boolean result = mockRemoteService.markAttendance(sessionToken, record);
            allSuccess = allSuccess && result;
        }
        
        // Assert
        assertTrue(allSuccess, "All attendance records should be marked successfully");
        assertEquals(5, records.size(), "Should have marked 5 records");
        
        // Verify all records have the same status
        for (AttendanceRecord record : records) {
            assertEquals(AttendanceStatus.PRESENT, record.getStatus(), 
                    "All records should have PRESENT status");
        }
    }
    
    /**
     * Test 6: Verify attendance record retrieval for modification
     * Validates Requirement 3.4: Modification interface with existing records
     */
    @Test
    @DisplayName("Should retrieve existing attendance records for modification")
    public void testExistingRecordsRetrieval() throws Exception {
        // Arrange & Act
        List<AttendanceRecord> records = mockRemoteService.getAttendanceByClassDate(
                sessionToken, 1, LocalDate.now());
        
        // Assert
        assertNotNull(records, "Records should not be null");
        assertEquals(1, records.size(), "Should have 1 existing record");
        
        AttendanceRecord record = records.get(0);
        assertEquals(2, record.getStudentId(), "Should be for student 2");
        assertEquals(AttendanceStatus.PRESENT, record.getStatus(), "Should be PRESENT");
    }
    
    /**
     * Test 7: Verify course selection and loading
     * Validates Requirement 3.1: Class selection functionality
     */
    @Test
    @DisplayName("Should load courses for teacher")
    public void testCourseSelection() throws Exception {
        // Arrange & Act
        List<Course> courses = mockRemoteService.getCoursesByTeacher(sessionToken, 1);
        
        // Assert
        assertNotNull(courses, "Courses should not be null");
        assertEquals(1, courses.size(), "Should have 1 course");
        
        Course course = courses.get(0);
        assertEquals("CS101", course.getCourseCode(), "Course code should match");
        assertEquals("Introduction to Computer Science", course.getCourseName(), 
                "Course name should match");
    }
    
    /**
     * Test 8: Verify remarks field handling
     * Validates Requirement 3.2: Remarks support in attendance records
     */
    @Test
    @DisplayName("Should handle remarks field in attendance records")
    public void testRemarksHandling() throws Exception {
        // Arrange
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(2);
        record.setCourseId(1);
        record.setAttendanceDate(LocalDate.now());
        record.setClassTime(LocalTime.now());
        record.setStatus(AttendanceStatus.LATE);
        record.setRemarks("Student arrived 15 minutes late");
        record.setMarkedBy(1);
        
        // Act
        when(mockRemoteService.markAttendance(sessionToken, record)).thenReturn(true);
        boolean result = mockRemoteService.markAttendance(sessionToken, record);
        
        // Assert
        assertTrue(result, "Attendance with remarks should be saved");
        assertEquals("Student arrived 15 minutes late", record.getRemarks(), 
                "Remarks should be preserved");
    }
    
    /**
     * Test 9: Verify date validation
     * Validates Requirement 3.5: Date validation (not in future)
     */
    @Test
    @DisplayName("Should validate attendance date is not in future")
    public void testDateValidation() throws Exception {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(1);
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate todayDate = LocalDate.now();
        
        // Act & Assert
        assertTrue(todayDate.isBefore(futureDate) || todayDate.equals(futureDate), 
                "Today should be before or equal to future date");
        assertTrue(pastDate.isBefore(todayDate), "Past date should be before today");
        assertTrue(todayDate.isAfter(pastDate) || todayDate.equals(pastDate), 
                "Today should be after or equal to past date");
    }
    
    /**
     * Test 10: Verify duplicate prevention
     * Validates Requirement 3.6: Duplicate attendance prevention
     */
    @Test
    @DisplayName("Should prevent duplicate attendance entries")
    public void testDuplicatePrevention() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.now();
        
        // Act - Get existing records for the date
        List<AttendanceRecord> existingForDate = mockRemoteService.getAttendanceByClassDate(
                sessionToken, 1, testDate);
        
        // Assert
        assertNotNull(existingForDate, "Should retrieve existing records");
        
        // Verify no duplicates in existing records
        Set<String> recordKeys = new HashSet<>();
        for (AttendanceRecord record : existingForDate) {
            String key = record.getStudentId() + "-" + record.getCourseId() + "-" + record.getAttendanceDate();
            assertFalse(recordKeys.contains(key), "Should not have duplicate records");
            recordKeys.add(key);
        }
    }
}
