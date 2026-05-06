package com.attendance.system.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations.
 */
public class DateUtil {
    
    // Common date and time formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    /**
     * Formats a LocalDate for display.
     * @param date the date to format
     * @return formatted date string
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : "";
    }
    
    /**
     * Formats a LocalDateTime for display.
     * @param dateTime the datetime to format
     * @return formatted datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMATTER) : "";
    }
    
    /**
     * Formats a LocalTime for display.
     * @param time the time to format
     * @return formatted time string
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }
    
    /**
     * Parses a date string in yyyy-MM-dd format.
     * @param dateString the date string to parse
     * @return LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parses a time string in HH:mm format.
     * @param timeString the time string to parse
     * @return LocalTime or null if parsing fails
     */
    public static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeString.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parses a datetime string in yyyy-MM-dd HH:mm:ss format.
     * @param dateTimeString the datetime string to parse
     * @return LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Checks if a date is in the future.
     * @param date the date to check
     * @return true if date is after today
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }
    
    /**
     * Checks if a date is today.
     * @param date the date to check
     * @return true if date is today
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }
    
    /**
     * Checks if a date is in the past.
     * @param date the date to check
     * @return true if date is before today
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }
    
    /**
     * Gets the number of days between two dates.
     * @param startDate the start date
     * @param endDate the end date
     * @return number of days between dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * Gets the number of hours between two datetimes.
     * @param startDateTime the start datetime
     * @param endDateTime the end datetime
     * @return number of hours between datetimes
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }
    
    /**
     * Gets the start of the current academic year.
     * Assumes academic year starts in September.
     * @return start date of current academic year
     */
    public static LocalDate getCurrentAcademicYearStart() {
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() >= 9) {
            // September or later - current year
            return LocalDate.of(now.getYear(), 9, 1);
        } else {
            // Before September - previous year
            return LocalDate.of(now.getYear() - 1, 9, 1);
        }
    }
    
    /**
     * Gets the end of the current academic year.
     * Assumes academic year ends in August.
     * @return end date of current academic year
     */
    public static LocalDate getCurrentAcademicYearEnd() {
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() >= 9) {
            // September or later - next year
            return LocalDate.of(now.getYear() + 1, 8, 31);
        } else {
            // Before September - current year
            return LocalDate.of(now.getYear(), 8, 31);
        }
    }
    
    /**
     * Gets the current semester based on the date.
     * @return "Fall", "Spring", or "Summer"
     */
    public static String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 9 || month <= 1) {
            return "Fall";
        } else if (month >= 2 && month <= 5) {
            return "Spring";
        } else {
            return "Summer";
        }
    }
    
    /**
     * Gets the current academic year string.
     * @return academic year in format "2023-2024"
     */
    public static String getCurrentAcademicYearString() {
        LocalDate start = getCurrentAcademicYearStart();
        LocalDate end = getCurrentAcademicYearEnd();
        return start.getYear() + "-" + end.getYear();
    }
    
    /**
     * Checks if a datetime is within the modification window (24 hours).
     * @param markedAt the datetime when record was marked
     * @return true if within 24 hours
     */
    public static boolean isWithinModificationWindow(LocalDateTime markedAt) {
        if (markedAt == null) {
            return true; // New record
        }
        LocalDateTime cutoff = markedAt.plusHours(24);
        return LocalDateTime.now().isBefore(cutoff);
    }
    
    /**
     * Gets a human-readable time ago string.
     * @param dateTime the datetime to compare
     * @return string like "2 hours ago", "3 days ago"
     */
    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        }
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 30) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }
        
        long months = ChronoUnit.DAYS.between(dateTime, now) / 30;
        if (months < 12) {
            return months + " month" + (months == 1 ? "" : "s") + " ago";
        }
        
        long years = ChronoUnit.DAYS.between(dateTime, now) / 365;
        return years + " year" + (years == 1 ? "" : "s") + " ago";
    }
    
    /**
     * Validates that a date range is valid (start <= end).
     * @param startDate the start date
     * @param endDate the end date
     * @return true if range is valid
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(endDate);
    }
}