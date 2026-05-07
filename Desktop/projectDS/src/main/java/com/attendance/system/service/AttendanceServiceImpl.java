package com.attendance.system.service;

import com.attendance.system.dao.AttendanceDAO;
import com.attendance.system.dao.CourseDAO;
import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.AuthenticationException;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.*;
import com.attendance.system.util.DateUtil;
import com.attendance.system.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Business logic implementation for attendance operations.
 * This class contains the core business rules and validation logic.
 */
public class AttendanceServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);
    
    private final UserDAO userDAO;
    private final AttendanceDAO attendanceDAO;
    private final CourseDAO courseDAO;
    private final AuthenticationService authService;
    
    public AttendanceServiceImpl() {
        this.userDAO = new UserDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.authService = new AuthenticationService(userDAO);
    }
    
    public AttendanceServiceImpl(UserDAO userDAO, AttendanceDAO attendanceDAO, 
                                CourseDAO courseDAO, AuthenticationService authService) {
        this.userDAO = userDAO;
        this.attendanceDAO = attendanceDAO;
        this.courseDAO = courseDAO;
        this.authService = authService;
    }
    
    /**
     * Validates and marks attendance for a student.
     * @param sessionToken the session token
     * @param record the attendance record
     * @return true if attendance was marked successfully
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if attendance data is invalid
     * @throws DatabaseException if database operation fails
     */
    public boolean markAttendance(String sessionToken, AttendanceRecord record) 
            throws AuthenticationException, ValidationException, DatabaseException {
        
        // Validate session and permissions
        User currentUser = authService.validateSession(sessionToken);
        if (!currentUser.hasPermission("ATTENDANCE_CREATE")) {
            throw AuthenticationException.insufficientPermissions();
        }
        
        // Validate attendance record
        validateAttendanceRecord(record);
        
        // Business rule validations
        validateAttendanceBusinessRules(record, currentUser);
        
        // Set marked by user
        record.setMarkedBy(currentUser.getUserId());
        
        // Insert attendance record
        boolean success = attendanceDAO.insertAttendanceRecord(record);
        
        if (success) {
            logger.info("Attendance marked by user {} for student {} in course {} on {}", 
                    currentUser.getUsername(), record.getStudentId(), record.getCourseId(), record.getAttendanceDate());
        }
        
        return success;
    }
    
    /**
     * Updates an existing attendance record.
     * @param sessionToken the session token
     * @param record the attendance record to update
     * @return true if attendance was updated successfully
     * @throws AuthenticationException if session is invalid or insufficient permissions
     * @throws ValidationException if attendance data is invalid
     * @throws DatabaseException if database operation fails
     */
    public boolean updateAttendance(String sessionToken, AttendanceRecord record) 
            throws AuthenticationException, ValidationException, DatabaseException {
        
        // Validate session and permissions
        User currentUser = authService.validateSession(sessionToken);
        if (!currentUser.hasPermission("ATTENDANCE_UPDATE")) {
            throw AuthenticationException.insufficientPermissions();
        }
        
        // Validate that record can be modified (within 24-hour window)
        if (!record.canBeModified()) {
            throw new ValidationException("attendance_modification", 
                    "Attendance records can only be modified within 24 hours of being marked");
        }
        
        // Validate attendance record
        validateAttendanceRecord(record);
        
        // Update attendance record
        boolean success = attendanceDAO.updateAttendanceRecord(record);
        
        if (success) {
            logger.info("Attendance updated by user {} for record {}", 
                    currentUser.getUsername(), record.getAttendanceId());
        }
        
        return success;
    }
    
    /**
     * Calculates attendance percentage for a student in a course.
     * @param studentId the student ID
     * @param courseId the course ID
     * @return attendance percentage (0-100)
     * @throws DatabaseException if database operation fails
     */
    public double calculateAttendancePercentage(int studentId, int courseId) throws DatabaseException {
        Map<String, Object> statistics = attendanceDAO.calculateAttendanceStatistics(studentId, courseId);
        
        Object percentageObj = statistics.get("attendancePercentage");
        if (percentageObj instanceof Number) {
            return ((Number) percentageObj).doubleValue();
        }
        
        return 0.0;
    }
    
    /**
     * Gets filtered attendance records based on criteria.
     * @param sessionToken the session token
     * @param studentId the student ID (0 for all)
     * @param courseId the course ID (0 for all)
     * @param startDate the start date (optional)
     * @param endDate the end date (optional)
     * @param status the attendance status (optional)
     * @return list of filtered attendance records
     * @throws AuthenticationException if session is invalid
     * @throws DatabaseException if database operation fails
     */
    public List<AttendanceRecord> getFilteredAttendanceRecords(String sessionToken, int studentId, 
                                                              int courseId, LocalDate startDate, 
                                                              LocalDate endDate, AttendanceStatus status) 
            throws AuthenticationException, ValidationException, DatabaseException {
        
        // Validate session
        User currentUser = authService.validateSession(sessionToken);
        
        // Apply role-based filtering
        if (currentUser.getRole() == UserRole.STUDENT) {
            // Students can only see their own records
            studentId = currentUser.getUserId();
        } else if (currentUser.getRole() == UserRole.TEACHER && courseId > 0) {
            // Teachers can only see records for their courses
            validateTeacherCourseAccess(currentUser.getUserId(), courseId);
        }
        
        return attendanceDAO.findWithFilters(studentId, courseId, startDate, endDate, status);
    }
    
    /**
     * Validates attendance business rules.
     * @param record the attendance record to validate
     * @param currentUser the user marking attendance
     * @throws ValidationException if business rules are violated
     * @throws DatabaseException if database operation fails
     */
    private void validateAttendanceBusinessRules(AttendanceRecord record, User currentUser) 
            throws ValidationException, DatabaseException {
        
        // Rule 1: No future dates
        if (DateUtil.isFutureDate(record.getAttendanceDate())) {
            throw ValidationException.futureDate("attendance_date");
        }
        
        // Rule 2: No duplicate entries for same student, course, date, and time
        if (attendanceDAO.attendanceRecordExists(record.getStudentId(), record.getCourseId(), 
                record.getAttendanceDate(), record.getClassTime())) {
            throw new ValidationException("duplicate_attendance", 
                    "Attendance record already exists for this student, course, date, and time");
        }
        
        // Rule 3: Teacher can only mark attendance for their courses
        if (currentUser.getRole() == UserRole.TEACHER) {
            validateTeacherCourseAccess(currentUser.getUserId(), record.getCourseId());
        }
        
        // Rule 4: Student must be enrolled in the course
        if (!courseDAO.isStudentEnrolled(record.getStudentId(), record.getCourseId())) {
            throw new ValidationException("student_not_enrolled", 
                    "Student is not enrolled in this course");
        }
        
        // Rule 5: Course must be active
        Course course = courseDAO.findById(record.getCourseId());
        if (course == null || !course.isActive()) {
            throw new ValidationException("course_inactive", 
                    "Cannot mark attendance for inactive course");
        }
    }
    
    /**
     * Validates an attendance record for basic data integrity.
     * @param record the attendance record to validate
     * @throws ValidationException if validation fails
     */
    private void validateAttendanceRecord(AttendanceRecord record) throws ValidationException {
        if (record == null) {
            throw new ValidationException("Attendance record is required");
        }
        
        if (record.getStudentId() <= 0) {
            throw ValidationException.required("student_id");
        }
        
        if (record.getCourseId() <= 0) {
            throw ValidationException.required("course_id");
        }
        
        if (record.getAttendanceDate() == null) {
            throw ValidationException.required("attendance_date");
        }
        
        if (record.getClassTime() == null) {
            throw ValidationException.required("class_time");
        }
        
        if (record.getStatus() == null) {
            throw ValidationException.required("status");
        }
        
        // Validate date is not too far in the past (e.g., more than 1 year)
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (record.getAttendanceDate().isBefore(oneYearAgo)) {
            throw new ValidationException("attendance_date", 
                    "Attendance date cannot be more than one year in the past");
        }
        
        // Validate class time is reasonable (between 6 AM and 10 PM)
        LocalTime minTime = LocalTime.of(6, 0);
        LocalTime maxTime = LocalTime.of(22, 0);
        if (record.getClassTime().isBefore(minTime) || record.getClassTime().isAfter(maxTime)) {
            throw new ValidationException("class_time", 
                    "Class time must be between 6:00 AM and 10:00 PM");
        }
        
        // Validate remarks length if provided
        if (record.getRemarks() != null && record.getRemarks().length() > 500) {
            throw ValidationException.tooLong("remarks", 500);
        }
    }
    
    /**
     * Validates that a teacher has access to a specific course.
     * @param teacherId the teacher ID
     * @param courseId the course ID
     * @throws ValidationException if teacher doesn't have access
     * @throws DatabaseException if database operation fails
     */
    private void validateTeacherCourseAccess(int teacherId, int courseId) 
            throws ValidationException, DatabaseException {
        
        Course course = courseDAO.findById(courseId);
        if (course == null) {
            throw new ValidationException("course_not_found", "Course not found");
        }
        
        if (course.getTeacherId() != teacherId) {
            throw new ValidationException("unauthorized_course_access", 
                    "Teacher is not assigned to this course");
        }
    }
    
    /**
     * Validates user data for creation or update.
     * @param user the user to validate
     * @throws ValidationException if validation fails
     */
    public void validateUser(User user) throws ValidationException {
        if (user == null) {
            throw new ValidationException("User is required");
        }
        
        SecurityUtil.validateUsername(user.getUsername());
        SecurityUtil.validateEmail(user.getEmail());
        SecurityUtil.validateRequired(user.getFirstName(), "first_name");
        SecurityUtil.validateRequired(user.getLastName(), "last_name");
        
        if (user.getRole() == null) {
            throw ValidationException.required("role");
        }
        
        // Role-specific validation
        if (user instanceof Student) {
            validateStudent((Student) user);
        } else if (user instanceof Teacher) {
            validateTeacher((Teacher) user);
        }
    }
    
    /**
     * Validates student-specific data.
     * @param student the student to validate
     * @throws ValidationException if validation fails
     */
    private void validateStudent(Student student) throws ValidationException {
        SecurityUtil.validateRequired(student.getStudentNumber(), "student_number");
        SecurityUtil.validateRequired(student.getProgram(), "program");
        
        if (student.getYearLevel() < 1 || student.getYearLevel() > 4) {
            throw ValidationException.invalidRange("year_level", 1, 4);
        }
        
        if (student.getEnrollmentDate() == null) {
            throw ValidationException.required("enrollment_date");
        }
        
        if (DateUtil.isFutureDate(student.getEnrollmentDate())) {
            throw ValidationException.futureDate("enrollment_date");
        }
    }
    
    /**
     * Validates teacher-specific data.
     * @param teacher the teacher to validate
     * @throws ValidationException if validation fails
     */
    private void validateTeacher(Teacher teacher) throws ValidationException {
        SecurityUtil.validateRequired(teacher.getEmployeeId(), "employee_id");
        SecurityUtil.validateRequired(teacher.getDepartment(), "department");
        
        // Specialization is optional but validate length if provided
        if (teacher.getSpecialization() != null && teacher.getSpecialization().length() > 100) {
            throw ValidationException.tooLong("specialization", 100);
        }
    }
    
    /**
     * Validates course data.
     * @param course the course to validate
     * @throws ValidationException if validation fails
     */
    public void validateCourse(Course course) throws ValidationException {
        if (course == null) {
            throw new ValidationException("Course is required");
        }
        
        SecurityUtil.validateRequired(course.getCourseCode(), "course_code");
        SecurityUtil.validateRequired(course.getCourseName(), "course_name");
        SecurityUtil.validateRequired(course.getSemester(), "semester");
        SecurityUtil.validateRequired(course.getAcademicYear(), "academic_year");
        
        if (course.getCredits() <= 0) {
            throw new ValidationException("credits", "Credits must be positive");
        }
        
        if (course.getTeacherId() <= 0) {
            throw ValidationException.required("teacher_id");
        }
        
        // Validate course code format (e.g., CS101, MATH201)
        if (!course.getCourseCode().matches("^[A-Z]{2,4}\\d{3,4}$")) {
            throw ValidationException.invalidFormat("course_code", "DEPT###");
        }
        
        // Validate academic year format (e.g., 2023-2024)
        if (!course.getAcademicYear().matches("^\\d{4}-\\d{4}$")) {
            throw ValidationException.invalidFormat("academic_year", "YYYY-YYYY");
        }
    }
    
    /**
     * Checks if a student's attendance is below the warning threshold.
     * @param studentId the student ID
     * @param courseId the course ID
     * @return true if attendance is below 75%
     * @throws DatabaseException if database operation fails
     */
    public boolean isAttendanceBelowThreshold(int studentId, int courseId) throws DatabaseException {
        double percentage = calculateAttendancePercentage(studentId, courseId);
        return percentage < 75.0;
    }
    
    /**
     * Gets students with low attendance for notification purposes.
     * @param courseId the course ID (0 for all courses)
     * @param threshold the attendance threshold percentage
     * @return list of student IDs with low attendance
     * @throws DatabaseException if database operation fails
     */
    public List<Integer> getStudentsWithLowAttendance(int courseId, double threshold) throws DatabaseException {
        return attendanceDAO.getStudentsWithLowAttendance(courseId, threshold);
    }

    /**
     * Generates an attendance report with specified filter criteria.
     * @param criteria the report filter criteria
     * @return a map containing report data
     * @throws DatabaseException if database operation fails
     */
    public Map<String, Object> generateAttendanceReport(AttendanceService.ReportCriteria criteria) throws DatabaseException {
        ReportService.ReportCriteria reportCriteria = new ReportService.ReportCriteria(
            criteria.getStartDate(),
            criteria.getEndDate()
        );
        
        if (criteria.getStudentId() > 0) {
            reportCriteria.setStudentId(criteria.getStudentId());
        }
        if (criteria.getCourseId() > 0) {
            reportCriteria.setCourseId(criteria.getCourseId());
        }
        if (criteria.getTeacherId() > 0) {
            reportCriteria.setTeacherId(criteria.getTeacherId());
        }
        
        ReportServiceImpl reportService = new ReportServiceImpl(attendanceDAO, courseDAO, userDAO);
        return reportService.generateAttendanceReport(reportCriteria);
    }

    /**
     * Generates system-wide attendance statistics.
     * @param startDate the start date
     * @param endDate the end date
     * @return system statistics map
     * @throws DatabaseException if database operation fails
     */
    public Map<String, Object> generateSystemStatistics(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        ReportServiceImpl reportService = new ReportServiceImpl(attendanceDAO, courseDAO, userDAO);
        return reportService.generateSystemStatistics(startDate, endDate);
    }

    /**
     * Generates a class-specific attendance report.
     * @param courseId the course ID
     * @param startDate the start date
     * @param endDate the end date
     * @return class report map
     * @throws DatabaseException if database operation fails
     */
    public Map<String, Object> generateClassReport(int courseId, LocalDate startDate, LocalDate endDate) throws DatabaseException {
        ReportServiceImpl reportService = new ReportServiceImpl(attendanceDAO, courseDAO, userDAO);
        return reportService.generateClassReport(courseId, startDate, endDate);
    }

    /**
     * Generates a student-specific attendance report.
     * @param studentId the student ID
     * @param startDate the start date
     * @param endDate the end date
     * @return student report map
     * @throws DatabaseException if database operation fails
     */
    public Map<String, Object> generateStudentReport(int studentId, LocalDate startDate, LocalDate endDate) throws DatabaseException {
        ReportServiceImpl reportService = new ReportServiceImpl(attendanceDAO, courseDAO, userDAO);
        return reportService.generateStudentReport(studentId, startDate, endDate);
    }

    /**
     * Exports a report to PDF format.
     * @param reportData the report data
     * @param fileName the output file name
     * @return PDF file as byte array
     * @throws DatabaseException if export operation fails
     */
    public byte[] exportReportToPDF(Map<String, Object> reportData, String fileName) throws DatabaseException {
        ReportServiceImpl reportService = new ReportServiceImpl(attendanceDAO, courseDAO, userDAO);
        return reportService.exportToPDF(reportData, fileName);
    }

    /**
     * Exports a report to Excel format.
     * @param reportData the report data
     * @param fileName the output file name
     * @return Excel file as byte array
     * @throws DatabaseException if export operation fails
     */
    public byte[] exportReportToExcel(Map<String, Object> reportData, String fileName) throws DatabaseException {
        ReportServiceImpl reportService = new ReportServiceImpl(attendanceDAO, courseDAO, userDAO);
        return reportService.exportToExcel(reportData, fileName);
    }
}