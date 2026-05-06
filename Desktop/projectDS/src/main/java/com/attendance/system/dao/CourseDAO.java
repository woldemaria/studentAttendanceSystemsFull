package com.attendance.system.dao;

import com.attendance.system.exception.DatabaseException;
import com.attendance.system.model.Course;
import com.attendance.system.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course operations.
 */
public class CourseDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);
    
    private final DatabaseManager databaseManager;
    
    public CourseDAO() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    public CourseDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Creates a new course.
     * @param course the course to create
     * @return true if course was created successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean createCourse(Course course) throws DatabaseException {
        String sql = """
            INSERT INTO COURSES (course_code, course_name, description, credits, teacher_id, semester, academic_year, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, course.getCourseCode());
            statement.setString(2, course.getCourseName());
            statement.setString(3, course.getDescription());
            statement.setInt(4, course.getCredits());
            statement.setInt(5, course.getTeacherId());
            statement.setString(6, course.getSemester());
            statement.setString(7, course.getAcademicYear());
            statement.setBoolean(8, course.isActive());
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        course.setCourseId(generatedKeys.getInt(1));
                    }
                }
                connection.commit();
                logger.info("Course created: " + course);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to create course", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Updates an existing course.
     * @param course the course to update
     * @return true if course was updated successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean updateCourse(Course course) throws DatabaseException {
        String sql = """
            UPDATE COURSES 
            SET course_code = ?, course_name = ?, description = ?, credits = ?, 
                teacher_id = ?, semester = ?, academic_year = ?, is_active = ?
            WHERE course_id = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, course.getCourseCode());
            statement.setString(2, course.getCourseName());
            statement.setString(3, course.getDescription());
            statement.setInt(4, course.getCredits());
            statement.setInt(5, course.getTeacherId());
            statement.setString(6, course.getSemester());
            statement.setString(7, course.getAcademicYear());
            statement.setBoolean(8, course.isActive());
            statement.setInt(9, course.getCourseId());
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Course updated: " + course.getCourseId());
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to update course: " + course.getCourseId(), e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds a course by ID.
     * @param courseId the course ID
     * @return Course object or null if not found
     * @throws DatabaseException if database error occurs
     */
    public Course findById(int courseId) throws DatabaseException {
        String sql = """
            SELECT c.*, u.first_name, u.last_name
            FROM COURSES c
            LEFT JOIN TEACHERS t ON c.teacher_id = t.teacher_id
            LEFT JOIN USERS u ON t.user_id = u.user_id
            WHERE c.course_id = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, courseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCourse(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find course by ID: " + courseId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds a course by course code.
     * @param courseCode the course code
     * @return Course object or null if not found
     * @throws DatabaseException if database error occurs
     */
    public Course findByCourseCode(String courseCode) throws DatabaseException {
        String sql = """
            SELECT c.*, u.first_name, u.last_name
            FROM COURSES c
            LEFT JOIN TEACHERS t ON c.teacher_id = t.teacher_id
            LEFT JOIN USERS u ON t.user_id = u.user_id
            WHERE c.course_code = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, courseCode);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCourse(resultSet);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Failed to find course by code: " + courseCode, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds courses by teacher ID.
     * @param teacherId the teacher ID
     * @return list of courses taught by the teacher
     * @throws DatabaseException if database error occurs
     */
    public List<Course> findByTeacherId(int teacherId) throws DatabaseException {
        String sql = """
            SELECT c.*, u.first_name, u.last_name
            FROM COURSES c
            LEFT JOIN TEACHERS t ON c.teacher_id = t.teacher_id
            LEFT JOIN USERS u ON t.user_id = u.user_id
            WHERE c.teacher_id = ? AND c.is_active = TRUE
            ORDER BY c.course_code
            """;
        
        List<Course> courses = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, teacherId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courses.add(mapResultSetToCourse(resultSet));
                }
            }
            
            return courses;
            
        } catch (SQLException e) {
            logger.error("Failed to find courses by teacher ID: " + teacherId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Finds courses by student ID (enrolled courses).
     * @param studentId the student ID
     * @return list of courses the student is enrolled in
     * @throws DatabaseException if database error occurs
     */
    public List<Course> findByStudentId(int studentId) throws DatabaseException {
        String sql = """
            SELECT c.*, u.first_name, u.last_name
            FROM COURSES c
            JOIN ENROLLMENTS e ON c.course_id = e.course_id
            LEFT JOIN TEACHERS t ON c.teacher_id = t.teacher_id
            LEFT JOIN USERS u ON t.user_id = u.user_id
            WHERE e.student_id = ? AND e.status = 'ENROLLED' AND c.is_active = TRUE
            ORDER BY c.course_code
            """;
        
        List<Course> courses = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courses.add(mapResultSetToCourse(resultSet));
                }
            }
            
            return courses;
            
        } catch (SQLException e) {
            logger.error("Failed to find courses by student ID: " + studentId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Gets all active courses.
     * @return list of all active courses
     * @throws DatabaseException if database error occurs
     */
    public List<Course> findAllActive() throws DatabaseException {
        String sql = """
            SELECT c.*, u.first_name, u.last_name
            FROM COURSES c
            LEFT JOIN TEACHERS t ON c.teacher_id = t.teacher_id
            LEFT JOIN USERS u ON t.user_id = u.user_id
            WHERE c.is_active = TRUE
            ORDER BY c.course_code
            """;
        
        List<Course> courses = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                courses.add(mapResultSetToCourse(resultSet));
            }
            
            return courses;
            
        } catch (SQLException e) {
            logger.error("Failed to find all active courses", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Gets all courses.
     * @return list of all courses
     * @throws DatabaseException if database error occurs
     */
    public List<Course> findAll() throws DatabaseException {
        String sql = """
            SELECT c.*, u.first_name, u.last_name
            FROM COURSES c
            LEFT JOIN TEACHERS t ON c.teacher_id = t.teacher_id
            LEFT JOIN USERS u ON t.user_id = u.user_id
            ORDER BY c.is_active DESC, c.course_code
            """;
        
        List<Course> courses = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                courses.add(mapResultSetToCourse(resultSet));
            }
            
            return courses;
            
        } catch (SQLException e) {
            logger.error("Failed to find all courses", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Enrolls a student in a course.
     * @param studentId the student ID
     * @param courseId the course ID
     * @return true if enrollment was successful
     * @throws DatabaseException if database error occurs
     */
    public boolean enrollStudent(int studentId, int courseId) throws DatabaseException {
        String sql = """
            INSERT INTO ENROLLMENTS (student_id, course_id, enrollment_date, status)
            VALUES (?, ?, CURRENT_DATE, 'ENROLLED')
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Student enrolled: studentId=" + studentId + ", courseId=" + courseId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to enroll student: studentId=" + studentId + ", courseId=" + courseId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Drops a student from a course.
     * @param studentId the student ID
     * @param courseId the course ID
     * @return true if drop was successful
     * @throws DatabaseException if database error occurs
     */
    public boolean dropStudent(int studentId, int courseId) throws DatabaseException {
        String sql = """
            UPDATE ENROLLMENTS 
            SET status = 'DROPPED'
            WHERE student_id = ? AND course_id = ?
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Student dropped: studentId=" + studentId + ", courseId=" + courseId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to drop student: studentId=" + studentId + ", courseId=" + courseId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Gets enrolled students for a course.
     * @param courseId the course ID
     * @return list of enrolled students
     * @throws DatabaseException if database error occurs
     */
    public List<Student> getEnrolledStudents(int courseId) throws DatabaseException {
        String sql = """
            SELECT u.*, s.student_number, s.program, s.year_level, s.enrollment_date
            FROM ENROLLMENTS e
            JOIN STUDENTS s ON e.student_id = s.student_id
            JOIN USERS u ON s.user_id = u.user_id
            WHERE e.course_id = ? AND e.status = 'ENROLLED' AND u.is_active = TRUE
            ORDER BY u.last_name, u.first_name
            """;
        
        List<Student> students = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, courseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(mapResultSetToStudent(resultSet));
                }
            }
            
            return students;
            
        } catch (SQLException e) {
            logger.error("Failed to get enrolled students for course: " + courseId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Checks if a student is enrolled in a course.
     * @param studentId the student ID
     * @param courseId the course ID
     * @return true if student is enrolled
     * @throws DatabaseException if database error occurs
     */
    public boolean isStudentEnrolled(int studentId, int courseId) throws DatabaseException {
        String sql = """
            SELECT COUNT(*) FROM ENROLLMENTS 
            WHERE student_id = ? AND course_id = ? AND status = 'ENROLLED'
            """;
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, studentId);
            statement.setInt(2, courseId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to check student enrollment", e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    /**
     * Deletes a course.
     * @param courseId the course ID
     * @return true if course was deleted successfully
     * @throws DatabaseException if database error occurs
     */
    public boolean deleteCourse(int courseId) throws DatabaseException {
        String sql = "DELETE FROM COURSES WHERE course_id = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, courseId);
            
            int rowsAffected = statement.executeUpdate();
            connection.commit();
            
            if (rowsAffected > 0) {
                logger.info("Course deleted: " + courseId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Failed to delete course: " + courseId, e);
            throw DatabaseException.queryFailed(sql, e);
        }
    }
    
    // Helper methods
    
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setDescription(rs.getString("description"));
        course.setCredits(rs.getInt("credits"));
        course.setTeacherId(rs.getInt("teacher_id"));
        course.setSemester(rs.getString("semester"));
        course.setAcademicYear(rs.getString("academic_year"));
        course.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            course.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return course;
    }
    
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        
        student.setUserId(rs.getInt("user_id"));
        student.setUsername(rs.getString("username"));
        student.setEmail(rs.getString("email"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setActive(rs.getBoolean("is_active"));
        
        student.setStudentNumber(rs.getString("student_number"));
        student.setProgram(rs.getString("program"));
        student.setYearLevel(rs.getInt("year_level"));
        
        Date enrollmentDate = rs.getDate("enrollment_date");
        if (enrollmentDate != null) {
            student.setEnrollmentDate(enrollmentDate.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            student.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            student.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return student;
    }
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
     * Gets the total number of courses in the system.
     * @return total course count
     * @throws DatabaseException if database operation fails
     */
    public int getTotalCourseCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM COURSES";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to get total course count", e);
            throw new DatabaseException("Failed to get total course count", e);
        }
    }
    
    /**
     * Gets the number of active courses in the system.
     * @return active course count
     * @throws DatabaseException if database operation fails
     */
    public int getActiveCourseCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM COURSES WHERE is_active = true";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            logger.error("Failed to get active course count", e);
            throw new DatabaseException("Failed to get active course count", e);
        }
    }