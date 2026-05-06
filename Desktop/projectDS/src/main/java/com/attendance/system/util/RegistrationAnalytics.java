package com.attendance.system.util;

import com.attendance.system.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Analytics and monitoring for user registration.
 * Tracks registration metrics, success rates, and trends.
 */
public class RegistrationAnalytics {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationAnalytics.class);
    
    // Registration statistics
    private final AtomicInteger totalRegistrations = new AtomicInteger(0);
    private final AtomicInteger successfulRegistrations = new AtomicInteger(0);
    private final AtomicInteger failedRegistrations = new AtomicInteger(0);
    private final AtomicInteger studentRegistrations = new AtomicInteger(0);
    private final AtomicInteger teacherRegistrations = new AtomicInteger(0);
    
    // Validation failure tracking
    private final Map<String, AtomicInteger> validationFailures = new ConcurrentHashMap<>();
    
    // Registration events
    private final List<RegistrationEvent> registrationEvents = Collections.synchronizedList(new ArrayList<>());
    
    // Daily statistics
    private final Map<LocalDate, DailyStats> dailyStats = new ConcurrentHashMap<>();
    
    // Performance metrics
    private final List<Long> registrationTimes = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Records a successful registration.
     */
    public void recordSuccessfulRegistration(String username, UserRole role, long processingTimeMs) {
        totalRegistrations.incrementAndGet();
        successfulRegistrations.incrementAndGet();
        registrationTimes.add(processingTimeMs);
        
        if (role == UserRole.STUDENT) {
            studentRegistrations.incrementAndGet();
        } else if (role == UserRole.TEACHER) {
            teacherRegistrations.incrementAndGet();
        }
        
        recordEvent(username, role, "SUCCESS", null, processingTimeMs);
        updateDailyStats(true);
        
        logger.info("Registration successful: {} ({}) - {}ms", username, role, processingTimeMs);
    }
    
    /**
     * Records a failed registration.
     */
    public void recordFailedRegistration(String username, String reason, long processingTimeMs) {
        totalRegistrations.incrementAndGet();
        failedRegistrations.incrementAndGet();
        registrationTimes.add(processingTimeMs);
        
        // Track validation failure reason
        validationFailures.computeIfAbsent(reason, k -> new AtomicInteger(0)).incrementAndGet();
        
        recordEvent(username, null, "FAILED", reason, processingTimeMs);
        updateDailyStats(false);
        
        logger.warn("Registration failed: {} - {} ({}ms)", username, reason, processingTimeMs);
    }
    
    /**
     * Records a registration event.
     */
    private void recordEvent(String username, UserRole role, String status, String reason, long processingTimeMs) {
        RegistrationEvent event = new RegistrationEvent(
            username,
            role,
            status,
            reason,
            processingTimeMs,
            LocalDateTime.now()
        );
        registrationEvents.add(event);
    }
    
    /**
     * Updates daily statistics.
     */
    private void updateDailyStats(boolean success) {
        LocalDate today = LocalDate.now();
        DailyStats stats = dailyStats.computeIfAbsent(today, k -> new DailyStats());
        
        if (success) {
            stats.successCount.incrementAndGet();
        } else {
            stats.failureCount.incrementAndGet();
        }
    }
    
    /**
     * Gets registration success rate.
     */
    public double getSuccessRate() {
        int total = totalRegistrations.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulRegistrations.get() / total * 100;
    }
    
    /**
     * Gets average registration processing time.
     */
    public double getAverageProcessingTime() {
        if (registrationTimes.isEmpty()) {
            return 0.0;
        }
        return registrationTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Gets maximum registration processing time.
     */
    public long getMaxProcessingTime() {
        return registrationTimes.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0);
    }
    
    /**
     * Gets minimum registration processing time.
     */
    public long getMinProcessingTime() {
        return registrationTimes.stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0);
    }
    
    /**
     * Gets validation failure statistics.
     */
    public Map<String, Integer> getValidationFailureStats() {
        return validationFailures.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get()
            ));
    }
    
    /**
     * Gets top validation failures.
     */
    public List<Map.Entry<String, Integer>> getTopValidationFailures(int limit) {
        return getValidationFailureStats().entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets daily statistics for a specific date.
     */
    public DailyStats getDailyStats(LocalDate date) {
        return dailyStats.getOrDefault(date, new DailyStats());
    }
    
    /**
     * Gets statistics for a date range.
     */
    public Map<LocalDate, DailyStats> getDateRangeStats(LocalDate startDate, LocalDate endDate) {
        return dailyStats.entrySet().stream()
            .filter(e -> !e.getKey().isBefore(startDate) && !e.getKey().isAfter(endDate))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Gets recent registration events.
     */
    public List<RegistrationEvent> getRecentEvents(int limit) {
        return registrationEvents.stream()
            .skip(Math.max(0, registrationEvents.size() - limit))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets registration events for a specific date.
     */
    public List<RegistrationEvent> getEventsForDate(LocalDate date) {
        return registrationEvents.stream()
            .filter(e -> e.timestamp.toLocalDate().equals(date))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets comprehensive analytics report.
     */
    public AnalyticsReport getAnalyticsReport() {
        return new AnalyticsReport(
            totalRegistrations.get(),
            successfulRegistrations.get(),
            failedRegistrations.get(),
            studentRegistrations.get(),
            teacherRegistrations.get(),
            getSuccessRate(),
            getAverageProcessingTime(),
            getMaxProcessingTime(),
            getMinProcessingTime(),
            getValidationFailureStats(),
            LocalDateTime.now()
        );
    }
    
    /**
     * Resets all statistics.
     */
    public void reset() {
        totalRegistrations.set(0);
        successfulRegistrations.set(0);
        failedRegistrations.set(0);
        studentRegistrations.set(0);
        teacherRegistrations.set(0);
        validationFailures.clear();
        registrationEvents.clear();
        dailyStats.clear();
        registrationTimes.clear();
        
        logger.info("Registration analytics reset");
    }
    
    /**
     * Gets statistics summary.
     */
    public String getSummary() {
        return String.format(
            "Registration Analytics Summary:\n" +
            "  Total Registrations: %d\n" +
            "  Successful: %d\n" +
            "  Failed: %d\n" +
            "  Success Rate: %.2f%%\n" +
            "  Students: %d\n" +
            "  Teachers: %d\n" +
            "  Avg Processing Time: %.2fms\n" +
            "  Max Processing Time: %dms\n" +
            "  Min Processing Time: %dms",
            totalRegistrations.get(),
            successfulRegistrations.get(),
            failedRegistrations.get(),
            getSuccessRate(),
            studentRegistrations.get(),
            teacherRegistrations.get(),
            getAverageProcessingTime(),
            getMaxProcessingTime(),
            getMinProcessingTime()
        );
    }
    
    // Inner classes
    
    /**
     * Represents a registration event.
     */
    public static class RegistrationEvent {
        public final String username;
        public final UserRole role;
        public final String status;
        public final String reason;
        public final long processingTimeMs;
        public final LocalDateTime timestamp;
        
        public RegistrationEvent(String username, UserRole role, String status, String reason, 
                                long processingTimeMs, LocalDateTime timestamp) {
            this.username = username;
            this.role = role;
            this.status = status;
            this.reason = reason;
            this.processingTimeMs = processingTimeMs;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Represents daily statistics.
     */
    public static class DailyStats {
        public final AtomicInteger successCount = new AtomicInteger(0);
        public final AtomicInteger failureCount = new AtomicInteger(0);
        
        public int getTotalCount() {
            return successCount.get() + failureCount.get();
        }
        
        public double getSuccessRate() {
            int total = getTotalCount();
            if (total == 0) {
                return 0.0;
            }
            return (double) successCount.get() / total * 100;
        }
    }
    
    /**
     * Represents a comprehensive analytics report.
     */
    public static class AnalyticsReport {
        public final int totalRegistrations;
        public final int successfulRegistrations;
        public final int failedRegistrations;
        public final int studentRegistrations;
        public final int teacherRegistrations;
        public final double successRate;
        public final double averageProcessingTime;
        public final long maxProcessingTime;
        public final long minProcessingTime;
        public final Map<String, Integer> validationFailures;
        public final LocalDateTime generatedAt;
        
        public AnalyticsReport(int totalRegistrations, int successfulRegistrations, int failedRegistrations,
                              int studentRegistrations, int teacherRegistrations, double successRate,
                              double averageProcessingTime, long maxProcessingTime, long minProcessingTime,
                              Map<String, Integer> validationFailures, LocalDateTime generatedAt) {
            this.totalRegistrations = totalRegistrations;
            this.successfulRegistrations = successfulRegistrations;
            this.failedRegistrations = failedRegistrations;
            this.studentRegistrations = studentRegistrations;
            this.teacherRegistrations = teacherRegistrations;
            this.successRate = successRate;
            this.averageProcessingTime = averageProcessingTime;
            this.maxProcessingTime = maxProcessingTime;
            this.minProcessingTime = minProcessingTime;
            this.validationFailures = validationFailures;
            this.generatedAt = generatedAt;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Analytics Report (Generated: %s)\n" +
                "  Total Registrations: %d\n" +
                "  Successful: %d\n" +
                "  Failed: %d\n" +
                "  Success Rate: %.2f%%\n" +
                "  Students: %d\n" +
                "  Teachers: %d\n" +
                "  Avg Processing Time: %.2fms\n" +
                "  Max Processing Time: %dms\n" +
                "  Min Processing Time: %dms\n" +
                "  Top Validation Failures: %s",
                generatedAt,
                totalRegistrations,
                successfulRegistrations,
                failedRegistrations,
                successRate,
                studentRegistrations,
                teacherRegistrations,
                averageProcessingTime,
                maxProcessingTime,
                minProcessingTime,
                validationFailures.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "))
            );
        }
    }
}
