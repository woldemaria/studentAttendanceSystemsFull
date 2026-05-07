package com.attendance.system.dao;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.AttendanceRecord;
import com.attendance.system.model.AttendanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Attendance operations.
 */
public class AttendanceDAO {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceDAO.class);
    
    private final DatabaseManager databaseManager;
    
    public AttendanceDAO() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    public AttendanceDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Inserts a new attendance record.
     * @param record the attendance record to insert
     * @return true if record was inserted successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean insertAttendanceRecord(AttendanceRecord record) throws DatabaseException {
        String sql = """
            INSERT INTO ATTENDANCE_RECORDS (student_id, course_id, attendance_date, class_time, status, marked_by, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Convert user_id to student_id
            int studentId = getStudentIdFromUserId(record.getStudentId());
            
            statement.setInt(1, studentId);
            statement.setInt(2, record.getCourseId());
            statement.setDate(3, Date.valueOf(record.getAttendanceDate()));
            statement.setTime(4, Time.valueOf(record.getClassTime()));
            statement.setString(5, record.getStatus().name());
            statement.setInt(6, record.getMarkedBy());
            statement.setString(7, record.getRemarks());
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setAttendanceId(generatedKeys.getInt(1));
                    }
                }
                connection.commit();
                logger.info("Attendance record inserted: " + record);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to insert attendance record", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Helper method to get student_id from user_id.
     * @param userId the user ID
     * @return the student ID from STUDENTS table
     * @throws DatabaseException if student not found or database error
     */
    private int getStudentIdFromUserId(int userId) throws DatabaseException {
        String sql = "SELECT student_id FROM STUDENTS WHERE user_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("student_id");
                } else {
                    throw new DatabaseException("Student not found for user_id: " + userId);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Failed to get student_id for user_id: " + userId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Updates an existing attendance record.
     * @param record the attendance record to update
     * @return true if record was updated successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean updateAttendanceRecord(AttendanceRecord record) throws DatabaseException {
        String sql = """
            UPDATE ATTENDANCE_RECORDS 
            SET status = ?, remarks = ?, marked_at = CURRENT_TIMESTAMP
            WHERE attendance_id = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, record.getStatus().name());
            statement.setString(2, record.getRemarks());
            statement.setInt(3, record.getAttendanceId());
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Attendance record updated: " + record.getAttendanceId());
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to update attendance record: " + record.getAttendanceId(), e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds attendance records by student ID.
     * @param studentId the student ID
     * @return list of attendance records
     * @throws DatabaseException if database error occurs
     */
    public List<AttendanceRecord> findByStudent(int studentId) throws DatabaseException {
        String sql = """
            SELECT * FROM ATTENDANCE_RECORDS 
            WHERE student_id = ? 
            ORDER BY attendance_date DESC, class_time DESC
            """;
        
        List<AttendanceRecord> records = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToAttendanceRecord(resultSet));
                }
            }
            
            return records;
            
        } catch (SQLException e) {
            logger.error("Failed to find attendance records by student: " + studentId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds attendance records by student and course.
     * @param studentId the student ID
     * @param courseId the course ID
     * @return list of attendance records
     * @throws DatabaseException if database error occurs
     */
    public List<AttendanceRecord> findByStudentAndCourse(int studentId, int courseId) throws DatabaseException {
        String sql = """
            SELECT * FROM ATTENDANCE_RECORDS 
            WHERE student_id = ? AND course_id = ?
            ORDER BY attendance_date DESC, class_time DESC
            """;
        
        List<AttendanceRecord> records = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToAttendanceRecord(resultSet));
                }
            }
            
            return records;
            
        } catch (SQLException e) {
            logger.error("Failed to find attendance records by student and course: " + studentId + ", " + courseId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds attendance records by course and date.
     * @param courseId the course ID
     * @param date the attendance date
     * @return list of attendance records
     * @throws DatabaseException if database error occurs
     */
    public List<AttendanceRecord> findByCourseAndDate(int courseId, LocalDate date) throws DatabaseException {
        String sql = """
            SELECT ar.*, u.first_name, u.last_name, s.student_number
            FROM ATTENDANCE_RECORDS ar
            JOIN STUDENTS s ON ar.student_id = s.student_id
            JOIN USERS u ON s.user_id = u.user_id
            WHERE ar.course_id = ? AND ar.attendance_date = ?
            ORDER BY u.last_name, u.first_name
            """;
        
        List<AttendanceRecord> records = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, courseId);
            statement.setDate(2, Date.valueOf(date));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToAttendanceRecord(resultSet));
                }
            }
            
            return records;
            
        } catch (SQLException e) {
            logger.error("Failed to find attendance records by course and date: " + courseId + ", " + date, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds attendance records by date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return list of attendance records
     * @throws DatabaseException if database error occurs
     */
    public List<AttendanceRecord> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = """
            SELECT * FROM ATTENDANCE_RECORDS 
            WHERE attendance_date BETWEEN ? AND ?
            ORDER BY attendance_date DESC, class_time DESC
            """;
        
        List<AttendanceRecord> records = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToAttendanceRecord(resultSet));
                }
            }
            
            return records;
            
        } catch (SQLException e) {
            logger.error("Failed to find attendance records by date range: " + startDate + " to " + endDate, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds attendance records with filtering options.
     * @param studentId the student ID (optional, 0 for all)
     * @param courseId the course ID (optional, 0 for all)
     * @param startDate the start date (optional)
     * @param endDate the end date (optional)
     * @param status the attendance status (optional)
     * @return list of filtered attendance records
     * @throws DatabaseException if database error occurs
     */
    public List<AttendanceRecord> findWithFilters(int studentId, int courseId, LocalDate startDate, 
                                                  LocalDate endDate, AttendanceStatus status) throws DatabaseException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ATTENDANCE_RECORDS WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        
        if (studentId > 0) {
            sqlBuilder.append(" AND student_id = ?");
            parameters.add(studentId);
        }
        
        if (courseId > 0) {
            sqlBuilder.append(" AND course_id = ?");
            parameters.add(courseId);
        }
        
        if (startDate != null) {
            sqlBuilder.append(" AND attendance_date >= ?");
            parameters.add(Date.valueOf(startDate));
        }
        
        if (endDate != null) {
            sqlBuilder.append(" AND attendance_date <= ?");
            parameters.add(Date.valueOf(endDate));
        }
        
        if (status != null) {
            sqlBuilder.append(" AND status = ?");
            parameters.add(status.name());
        }
        
        sqlBuilder.append(" ORDER BY attendance_date DESC, class_time DESC");
        
        String sql = sqlBuilder.toString();
        List<AttendanceRecord> records = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToAttendanceRecord(resultSet));
                }
            }
            
            return records;
            
        } catch (SQLException e) {
            logger.error("Failed to find attendance records with filters", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Calculates attendance statistics for a student and course.
     * @param studentId the student ID
     * @param courseId the course ID
     * @return attendance statistics map
     * @throws DatabaseException if database error occurs
     */
    public Map<String, Object> calculateAttendanceStatistics(int studentId, int courseId) throws DatabaseException {
        String sql = """
            SELECT 
                COUNT(*) as total_classes,
                SUM(CASE WHEN status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) as attended_classes,
                SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present_count,
                SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count,
                SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late_count,
                SUM(CASE WHEN status = 'EXCUSED' THEN 1 ELSE 0 END) as excused_count
            FROM ATTENDANCE_RECORDS 
            WHERE student_id = ? AND course_id = ?
            """;
        
        Map<String, Object> statistics = new HashMap<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int totalClasses = resultSet.getInt("total_classes");
                    int attendedClasses = resultSet.getInt("attended_classes");
                    
                    statistics.put("totalClasses", totalClasses);
                    statistics.put("attendedClasses", attendedClasses);
                    statistics.put("presentCount", resultSet.getInt("present_count"));
                    statistics.put("absentCount", resultSet.getInt("absent_count"));
                    statistics.put("lateCount", resultSet.getInt("late_count"));
                    statistics.put("excusedCount", resultSet.getInt("excused_count"));
                    
                    // Calculate attendance percentage
                    double attendancePercentage = totalClasses > 0 ? 
                        (double) attendedClasses / totalClasses * 100.0 : 0.0;
                    statistics.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);
                }
            }
            
            return statistics;
            
        } catch (SQLException e) {
            logger.error("Failed to calculate attendance statistics for student: " + studentId + ", course: " + courseId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Checks if an attendance record already exists for the given parameters.
     * @param studentId the student ID
     * @param courseId the course ID
     * @param date the attendance date
     * @param classTime the class time
     * @return true if record exists
     * @throws DatabaseException if database error occurs
     */
    public boolean attendanceRecordExists(int studentId, int courseId, LocalDate date, LocalTime classTime) throws DatabaseException {
        String sql = """
            SELECT COUNT(*) FROM ATTENDANCE_RECORDS 
            WHERE student_id = ? AND course_id = ? AND attendance_date = ? AND class_time = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
            statement.setDate(3, Date.valueOf(date));
            statement.setTime(4, Time.valueOf(classTime));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to check attendance record existence", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Gets students with low attendance (below threshold).
     * @param courseId the course ID (optional, 0 for all courses)
     * @param threshold the attendance percentage threshold
     * @return list of student IDs with low attendance
     * @throws DatabaseException if database error occurs
     */
    public List<Integer> getStudentsWithLowAttendance(int courseId, double threshold) throws DatabaseException {
        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT student_id,
                   COUNT(*) as total_classes,
                   SUM(CASE WHEN status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) as attended_classes,
                   (SUM(CASE WHEN status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) / COUNT(*)) * 100 as attendance_percentage
            FROM ATTENDANCE_RECORDS 
            WHERE 1=1
            """);
        
        List<Object> parameters = new ArrayList<>();
        
        if (courseId > 0) {
            sqlBuilder.append(" AND course_id = ?");
            parameters.add(courseId);
        }
        
        sqlBuilder.append("""
             GROUP BY student_id
             HAVING attendance_percentage < ?
             ORDER BY attendance_percentage ASC
            """);
        parameters.add(threshold);
        
        String sql = sqlBuilder.toString();
        List<Integer> studentIds = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    studentIds.add(resultSet.getInt("student_id"));
                }
            }
            
            return studentIds;
            
        } catch (SQLException e) {
            logger.error("Failed to get students with low attendance", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Deletes an attendance record.
     * @param attendanceId the attendance record ID
     * @return true if record was deleted successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean deleteAttendanceRecord(int attendanceId) throws DatabaseException {
        String sql = "DELETE FROM ATTENDANCE_RECORDS WHERE attendance_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, attendanceId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Attendance record deleted: " + attendanceId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to delete attendance record: " + attendanceId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    // Helper method to map ResultSet to AttendanceRecord
    private AttendanceRecord mapResultSetToAttendanceRecord(ResultSet rs) throws SQLException {
        AttendanceRecord record = new AttendanceRecord();
        
        record.setAttendanceId(rs.getInt("attendance_id"));
        record.setStudentId(rs.getInt("student_id"));
        record.setCourseId(rs.getInt("course_id"));
        
        Date attendanceDate = rs.getDate("attendance_date");
        if (attendanceDate != null) {
            record.setAttendanceDate(attendanceDate.toLocalDate());
        }
        
        Time classTime = rs.getTime("class_time");
        if (classTime != null) {
            record.setClassTime(classTime.toLocalTime());
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            record.setStatus(AttendanceStatus.valueOf(statusStr));
        }
        
        Timestamp markedAt = rs.getTimestamp("marked_at");
        if (markedAt != null) {
            record.setMarkedAt(markedAt.toLocalDateTime());
        }
        
        record.setMarkedBy(rs.getInt("marked_by"));
        record.setRemarks(rs.getString("remarks"));
        
        return record;
    }

    /**
     * Tests database connection.
     * @return true if connection is successful
     * @throws DatabaseException if connection test fails
     */
    public boolean testConnection() throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.executeQuery("SELECT 1");
            return true;
            
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            throw new DatabaseException("Connection test failed", e);
        }
    }
    
    /**
     * Gets the total number of attendance records in the system.
     * @return total attendance record count
     * @throws DatabaseException if database operation fails
     */
    public int getTotalRecordCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM ATTENDANCE_RECORDS";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to get total attendance record count", e);
            throw new DatabaseException("Failed to get total attendance record count", e);
        }
    }}
