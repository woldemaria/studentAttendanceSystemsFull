package com.attendance.system.util;

import com.attendance.system.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RegistrationAnalytics.
 * Tests analytics tracking and reporting functionality.
 */
@DisplayName("Registration Analytics Tests")
public class RegistrationAnalyticsTest {
    
    private RegistrationAnalytics analytics;
    
    @BeforeEach
    public void setUp() {
        analytics = new RegistrationAnalytics();
    }
    
    // Basic Recording Tests
    
    @Test
    @DisplayName("Analytics: record successful student registration")
    public void testRecordSuccessfulStudentRegistration() {
        analytics.recordSuccessfulRegistration("student1", UserRole.STUDENT, 150);
        
        RegistrationAnalytics.AnalyticsReport report = analytics.getAnalyticsReport();
        assertEquals(1, report.totalRegistrations);
        assertEquals(1, report.successfulRegistrations);
        assertEquals(0, report.failedRegistrations);
        assertEquals(1, report.studentRegistrations);
        assertEquals(0, report.teacherRegistrations);
    }
    
    @Test
    @DisplayName("Analytics: record successful teacher registration")
    public void testRecordSuccessfulTeacherRegistration() {
        analytics.recordSuccessfulRegistration("teacher1", UserRole.TEACHER, 200);
        
        RegistrationAnalytics.AnalyticsReport report = analytics.getAnalyticsReport();
        assertEquals(1, report.totalRegistrations);
        assertEquals(1, report.successfulRegistrations);
        assertEquals(0, report.failedRegistrations);
        assertEquals(0, report.studentRegistrations);
        assertEquals(1, report.teacherRegistrations);
    }
    
    @Test
    @DisplayName("Analytics: record failed registration")
    public void testRecordFailedRegistration() {
        analytics.recordFailedRegistration("user1", "Invalid email", 100);
        
        RegistrationAnalytics.AnalyticsReport report = analytics.getAnalyticsReport();
        assertEquals(1, report.totalRegistrations);
        assertEquals(0, report.successfulRegistrations);
        assertEquals(1, report.failedRegistrations);
    }
    
    // Success Rate Tests
    
    @Test
    @DisplayName("Analytics: calculate success rate with all successful")
    public void testSuccessRateAllSuccessful() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user3", UserRole.STUDENT, 100);
        
        assertEquals(100.0, analytics.getSuccessRate(), 0.01);
    }
    
    @Test
    @DisplayName("Analytics: calculate success rate with all failed")
    public void testSuccessRateAllFailed() {
        analytics.recordFailedRegistration("user1", "Invalid email", 100);
        analytics.recordFailedRegistration("user2", "Invalid email", 100);
        
        assertEquals(0.0, analytics.getSuccessRate(), 0.01);
    }
    
    @Test
    @DisplayName("Analytics: calculate success rate with mixed results")
    public void testSuccessRateMixed() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 100);
        analytics.recordFailedRegistration("user3", "Invalid email", 100);
        
        assertEquals(66.67, analytics.getSuccessRate(), 0.01);
    }
    
    @Test
    @DisplayName("Analytics: success rate with no registrations")
    public void testSuccessRateNoRegistrations() {
        assertEquals(0.0, analytics.getSuccessRate(), 0.01);
    }
    
    // Processing Time Tests
    
    @Test
    @DisplayName("Analytics: calculate average processing time")
    public void testAverageProcessingTime() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 200);
        analytics.recordSuccessfulRegistration("user3", UserRole.STUDENT, 300);
        
        assertEquals(200.0, analytics.getAverageProcessingTime(), 0.01);
    }
    
    @Test
    @DisplayName("Analytics: get maximum processing time")
    public void testMaxProcessingTime() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 500);
        analytics.recordSuccessfulRegistration("user3", UserRole.STUDENT, 300);
        
        assertEquals(500, analytics.getMaxProcessingTime());
    }
    
    @Test
    @DisplayName("Analytics: get minimum processing time")
    public void testMinProcessingTime() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 500);
        analytics.recordSuccessfulRegistration("user3", UserRole.STUDENT, 300);
        
        assertEquals(100, analytics.getMinProcessingTime());
    }
    
    // Validation Failure Tests
    
    @Test
    @DisplayName("Analytics: track validation failures")
    public void testTrackValidationFailures() {
        analytics.recordFailedRegistration("user1", "Invalid email", 100);
        analytics.recordFailedRegistration("user2", "Invalid email", 100);
        analytics.recordFailedRegistration("user3", "Username too short", 100);
        
        Map<String, Integer> failures = analytics.getValidationFailureStats();
        assertEquals(2, failures.get("Invalid email"));
        assertEquals(1, failures.get("Username too short"));
    }
    
    @Test
    @DisplayName("Analytics: get top validation failures")
    public void testGetTopValidationFailures() {
        analytics.recordFailedRegistration("user1", "Invalid email", 100);
        analytics.recordFailedRegistration("user2", "Invalid email", 100);
        analytics.recordFailedRegistration("user3", "Invalid email", 100);
        analytics.recordFailedRegistration("user4", "Username too short", 100);
        analytics.recordFailedRegistration("user5", "Username too short", 100);
        analytics.recordFailedRegistration("user6", "Password too weak", 100);
        
        List<Map.Entry<String, Integer>> topFailures = analytics.getTopValidationFailures(2);
        assertEquals(2, topFailures.size());
        assertEquals("Invalid email", topFailures.get(0).getKey());
        assertEquals(3, topFailures.get(0).getValue());
        assertEquals("Username too short", topFailures.get(1).getKey());
        assertEquals(2, topFailures.get(1).getValue());
    }
    
    // Event Recording Tests
    
    @Test
    @DisplayName("Analytics: record registration events")
    public void testRecordRegistrationEvents() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordFailedRegistration("user2", "Invalid email", 100);
        analytics.recordSuccessfulRegistration("user3", UserRole.TEACHER, 150);
        
        List<RegistrationAnalytics.RegistrationEvent> events = analytics.getRecentEvents(10);
        assertEquals(3, events.size());
    }
    
    @Test
    @DisplayName("Analytics: get recent events with limit")
    public void testGetRecentEventsWithLimit() {
        for (int i = 0; i < 20; i++) {
            analytics.recordSuccessfulRegistration("user" + i, UserRole.STUDENT, 100);
        }
        
        List<RegistrationAnalytics.RegistrationEvent> events = analytics.getRecentEvents(5);
        assertEquals(5, events.size());
    }
    
    @Test
    @DisplayName("Analytics: get events for specific date")
    public void testGetEventsForDate() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 100);
        
        List<RegistrationAnalytics.RegistrationEvent> events = analytics.getEventsForDate(LocalDate.now());
        assertEquals(2, events.size());
    }
    
    // Daily Statistics Tests
    
    @Test
    @DisplayName("Analytics: get daily statistics")
    public void testGetDailyStatistics() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 100);
        analytics.recordFailedRegistration("user3", "Invalid email", 100);
        
        RegistrationAnalytics.DailyStats stats = analytics.getDailyStats(LocalDate.now());
        assertEquals(2, stats.successCount.get());
        assertEquals(1, stats.failureCount.get());
        assertEquals(3, stats.getTotalCount());
    }
    
    @Test
    @DisplayName("Analytics: calculate daily success rate")
    public void testDailySuccessRate() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.STUDENT, 100);
        analytics.recordFailedRegistration("user3", "Invalid email", 100);
        
        RegistrationAnalytics.DailyStats stats = analytics.getDailyStats(LocalDate.now());
        assertEquals(66.67, stats.getSuccessRate(), 0.01);
    }
    
    // Report Generation Tests
    
    @Test
    @DisplayName("Analytics: generate comprehensive report")
    public void testGenerateComprehensiveReport() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.TEACHER, 200);
        analytics.recordFailedRegistration("user3", "Invalid email", 150);
        
        RegistrationAnalytics.AnalyticsReport report = analytics.getAnalyticsReport();
        
        assertEquals(3, report.totalRegistrations);
        assertEquals(2, report.successfulRegistrations);
        assertEquals(1, report.failedRegistrations);
        assertEquals(1, report.studentRegistrations);
        assertEquals(1, report.teacherRegistrations);
        assertEquals(66.67, report.successRate, 0.01);
        assertEquals(150.0, report.averageProcessingTime, 0.01);
        assertEquals(200, report.maxProcessingTime);
        assertEquals(100, report.minProcessingTime);
    }
    
    // Reset Tests
    
    @Test
    @DisplayName("Analytics: reset all statistics")
    public void testResetStatistics() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordFailedRegistration("user2", "Invalid email", 100);
        
        analytics.reset();
        
        RegistrationAnalytics.AnalyticsReport report = analytics.getAnalyticsReport();
        assertEquals(0, report.totalRegistrations);
        assertEquals(0, report.successfulRegistrations);
        assertEquals(0, report.failedRegistrations);
    }
    
    // Summary Tests
    
    @Test
    @DisplayName("Analytics: generate summary string")
    public void testGenerateSummary() {
        analytics.recordSuccessfulRegistration("user1", UserRole.STUDENT, 100);
        analytics.recordSuccessfulRegistration("user2", UserRole.TEACHER, 200);
        
        String summary = analytics.getSummary();
        
        assertTrue(summary.contains("Total Registrations: 2"));
        assertTrue(summary.contains("Successful: 2"));
        assertTrue(summary.contains("Failed: 0"));
        assertTrue(summary.contains("Students: 1"));
        assertTrue(summary.contains("Teachers: 1"));
    }
}
