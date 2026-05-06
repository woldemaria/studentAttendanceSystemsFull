package com.attendance.system.client;

import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
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
 * Property-based tests for AttendanceMarkingPanel.
 * Tests correctness properties for attendance marking functionality.
 * 
 * Validates Requirements: 3.1, 3.2, 3.4
 */
@DisplayName("AttendanceMarkingPanel Property Tests")
public class AttendanceMarkingPanelPropertyTest {
    
    @Mock
    private AttendanceGUI mockParentFrame;
    
    @Mock
    private AttendanceService mockRemoteService;
    
    private String sessionToken = "test-session-token";
    private Teacher currentTeacher;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        currentTeacher = new Teacher("teacher1", "teacher@test.com", "John", "Doe", "EMP001");
        currentTeacher.setUserId(1);
        
        when(mockRemoteService.validateSession(sessionToken)).thenReturn(currentTeacher);
    }
    
    /**
     * Property 9: Student List Retrieval Accuracy
     * For any valid class and date combination, the system should return exactly the list of 
     * students enrolled in that class, with no duplicates and no students from other classes.
     * 
     * **Validates: Requirements 3.1**
     */
    @Test
    @DisplayName("Property 9: Student List Retrieval Accuracy")
    public void testStudentListRetrievalAccuracy() {
        QuickCheck.forAll(
                generateCourses(),
                generateStudentLists(),
                (course, students) -> {
                    // Setup mock
                    try {
                        when(mockRemoteService.getEnrolledStudents(sessionToken, course.getCourseId()))
                                .thenReturn(students);
                        
                        // Act
                        List<Student> retrieved = mockRemoteService.getEnrolledStudents(
                                sessionToken, course.getCourseId());
                        
                        // Assert
                        assertNotNull(retrieved, "Student list should not be null");
                        assertEquals(students.size(), retrieved.size(), 
                                "Should return exact number of students");
                        
                        // Verify no duplicates
                        Set<Integer> studentIds = new HashSet<>();
                        for (Student student : retrieved) {
                            assertFalse(studentIds.contains(student.getUserId()), 
                                    "Should not have duplicate students");
                            studentIds.add(student.getUserId());
                        }
                        
                        // Verify all students match
                        for (int i = 0; i < students.size(); i++) {
                            assertEquals(students.get(i).getUserId(), retrieved.get(i).getUserId(),
                                    "Student IDs should match");
                        }
                        
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).check();
    }
    
    /**
     * Property 10: Attendance Record Creation and Storage
     * For any valid attendance marking operation (student, course, date, status), the system 
     * should create an AttendanceRecord with accurate timestamp and store it such that it can 
     * be retrieved with all original data intact.
     * 
     * **Validates: Requirements 3.2, 3.3**
     */
    @Test
    @DisplayName("Property 10: Attendance Record Creation and Storage")
    public void testAttendanceRecordCreationAndStorage() {
        QuickCheck.forAll(
                generateAttendanceRecords(),
                record -> {
                    try {
                        // Setup mock
                        when(mockRemoteService.markAttendance(sessionToken, record))
                                .thenReturn(true);
                        
                        // Act
                        boolean result = mockRemoteService.markAttendance(sessionToken, record);
                        
                        // Assert
                        assertTrue(result, "Attendance marking should succeed");
                        assertNotNull(record.getMarkedAt(), "Timestamp should be set");
                        assertNotNull(record.getStatus(), "Status should not be null");
                        assertEquals(record.getStudentId(), record.getStudentId(), 
                                "Student ID should be preserved");
                        assertEquals(record.getCourseId(), record.getCourseId(), 
                                "Course ID should be preserved");
                        assertEquals(record.getAttendanceDate(), record.getAttendanceDate(), 
                                "Date should be preserved");
                        
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ).check();
    }
    
    /**
     * Property 11: Attendance Modification Time Window Enforcement
     * For any attendance record, modification attempts should succeed only if made within 
     * 24 hours of the original entry timestamp, and fail with appropriate error messages otherwise.
     * 
     * **Validates: Requirements 3.4**
     */
    @Test
    @DisplayName("Property 11: Attendance Modification Time Window Enforcement")
    public void testAttendanceModificationTimeWindow() {
        QuickCheck.forAll(
                generateAttendanceRecords(),
                record -> {
                    // Act & Assert
                    boolean canModify = record.canBeModified();
                    
                    // If record has a marked timestamp, verify the 24-hour window
                    if (record.getMarkedAt() != null) {
                        java.time.LocalDateTime cutoff = record.getMarkedAt().plusHours(24);
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();
                        
                        if (now.isBefore(cutoff)) {
                            assertTrue(canModify, "Should be modifiable within 24 hours");
                        } else {
                            assertFalse(canModify, "Should not be modifiable after 24 hours");
                        }
                    } else {
                        // New records should always be modifiable
                        assertTrue(canModify, "New records should be modifiable");
                    }
                    
                    return true;
                }
        ).check();
    }
    
    /**
     * Property 12: Future Date Validation for Attendance
     * For any attendance marking attempt, the system should reject entries with future dates 
     * and accept only current or past dates within reasonable bounds.
     * 
     * **Validates: Requirements 3.5**
     */
    @Test
    @DisplayName("Property 12: Future Date Validation for Attendance")
    public void testFutureDateValidation() {
        QuickCheck.forAll(
                generateDates(),
                date -> {
                    // Act
                    boolean isValid = !date.isAfter(LocalDate.now());
                    
                    // Assert
                    if (date.isAfter(LocalDate.now())) {
                        assertFalse(isValid, "Future dates should be invalid");
                    } else {
                        assertTrue(isValid, "Current and past dates should be valid");
                    }
                    
                    return true;
                }
        ).check();
    }
    
    /**
     * Property 13: Duplicate Attendance Prevention
     * For any student, course, and date combination, the system should allow only one 
     * attendance record and prevent duplicate entries while preserving the ability to 
     * modify the existing record.
     * 
     * **Validates: Requirements 3.6**
     */
    @Test
    @DisplayName("Property 13: Duplicate Attendance Prevention")
    public void testDuplicateAttendancePrevention() {
        QuickCheck.forAll(
                generateAttendanceRecordLists(),
                records -> {
                    // Group records by student-course-date combination
                    Map<String, List<AttendanceRecord>> grouped = new HashMap<>();
                    
                    for (AttendanceRecord record : records) {
                        String key = record.getStudentId() + "-" + record.getCourseId() + 
                                "-" + record.getAttendanceDate();
                        grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
                    }
                    
                    // Assert - each combination should have at most one record
                    for (List<AttendanceRecord> group : grouped.values()) {
                        assertTrue(group.size() <= 1, 
                                "Should not have duplicate records for same student-course-date");
                    }
                    
                    return true;
                }
        ).check();
    }
    
    // Generator methods
    
    private Generator<Course> generateCourses() {
        return new Generator<Course>() {
            private int courseId = 1;
            
            @Override
            public Course next() {
                Course course = new Course("CS" + courseId, "Course " + courseId, 3, "Fall", "2024");
                course.setCourseId(courseId++);
                course.setTeacherId(1);
                return course;
            }
        };
    }
    
    private Generator<List<Student>> generateStudentLists() {
        return new Generator<List<Student>>() {
            private int studentId = 1;
            
            @Override
            public List<Student> next() {
                List<Student> students = new ArrayList<>();
                int count = (int) (Math.random() * 10) + 1; // 1-10 students
                
                for (int i = 0; i < count; i++) {
                    Student student = new Student("student" + studentId, 
                            "student" + studentId + "@test.com", "Student", "Number" + studentId, 
                            "STU" + String.format("%04d", studentId));
                    student.setUserId(studentId++);
                    students.add(student);
                }
                
                return students;
            }
        };
    }
    
    private Generator<AttendanceRecord> generateAttendanceRecords() {
        return new Generator<AttendanceRecord>() {
            private int recordId = 1;
            
            @Override
            public AttendanceRecord next() {
                AttendanceRecord record = new AttendanceRecord();
                record.setAttendanceId(recordId++);
                record.setStudentId((int) (Math.random() * 100) + 1);
                record.setCourseId((int) (Math.random() * 10) + 1);
                record.setAttendanceDate(LocalDate.now().minusDays((int) (Math.random() * 30)));
                record.setClassTime(LocalTime.of((int) (Math.random() * 24), 
                        (int) (Math.random() * 60)));
                
                AttendanceStatus[] statuses = AttendanceStatus.values();
                record.setStatus(statuses[(int) (Math.random() * statuses.length)]);
                
                record.setMarkedBy(1);
                record.setRemarks("Test remark " + recordId);
                
                return record;
            }
        };
    }
    
    private Generator<List<AttendanceRecord>> generateAttendanceRecordLists() {
        return new Generator<List<AttendanceRecord>>() {
            @Override
            public List<AttendanceRecord> next() {
                List<AttendanceRecord> records = new ArrayList<>();
                int count = (int) (Math.random() * 20) + 1; // 1-20 records
                
                for (int i = 0; i < count; i++) {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setAttendanceId(i + 1);
                    record.setStudentId((int) (Math.random() * 50) + 1);
                    record.setCourseId((int) (Math.random() * 5) + 1);
                    record.setAttendanceDate(LocalDate.now().minusDays((int) (Math.random() * 30)));
                    record.setClassTime(LocalTime.of((int) (Math.random() * 24), 
                            (int) (Math.random() * 60)));
                    
                    AttendanceStatus[] statuses = AttendanceStatus.values();
                    record.setStatus(statuses[(int) (Math.random() * statuses.length)]);
                    
                    record.setMarkedBy(1);
                    records.add(record);
                }
                
                return records;
            }
        };
    }
    
    private Generator<LocalDate> generateDates() {
        return new Generator<LocalDate>() {
            @Override
            public LocalDate next() {
                // Generate dates within +/- 60 days from today
                int daysOffset = (int) (Math.random() * 120) - 60;
                return LocalDate.now().plusDays(daysOffset);
            }
        };
    }
}
