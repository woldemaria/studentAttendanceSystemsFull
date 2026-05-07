package com.attendance.system.server;

import com.attendance.system.dao.AttendanceDAO;
import com.attendance.system.dao.CourseDAO;
import com.attendance.system.dao.NotificationDAO;
import com.attendance.system.dao.UserDAO;
import com.attendance.system.exception.AuthenticationException;
import com.attendance.system.exception.DatabaseException;
import com.attendance.system.exception.RemoteServiceException;
import com.attendance.system.exception.ValidationException;
import com.attendance.system.model.*;
import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AttendanceServiceImpl;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.util.ConfigManager;
import com.attendance.system.util.MaintenanceManager;
import com.attendance.system.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RMI server implementation for the Student Attendance System.
 * Provides thread-safe remote access to attendance management functionality.
 */
public class AttendanceServer extends UnicastRemoteObject implements AttendanceService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceServer.class);
    private static final long serialVersionUID = 1L;
    
    // Core services
    private final AuthenticationService authService;
    private final AttendanceServiceImpl attendanceServiceImpl;
    private final UserDAO userDAO;
    private final AttendanceDAO attendanceDAO;
    private final CourseDAO courseDAO;
    private final NotificationDAO notificationDAO;
    
    // Server monitoring and statistics
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final LocalDateTime serverStartTime;
    private final Map<String, ClientConnection> clientConnections = new ConcurrentHashMap<>();
    
    // Configuration
    private final boolean encryptionEnabled;
    private final boolean allowDuplicateUsername;
    private final int maxConcurrentUsers;

    
    /**
     * Creates a new AttendanceServer instance.
     * @throws RemoteException if RMI initialization fails
     */
    public AttendanceServer() throws RemoteException {
        super();
        
        this.serverStartTime = LocalDateTime.now();
        this.userDAO = new UserDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.notificationDAO = new NotificationDAO();
        this.authService = new AuthenticationService(userDAO);
        this.attendanceServiceImpl = new AttendanceServiceImpl(userDAO, attendanceDAO, courseDAO, authService);
        
        // Load configuration
        this.encryptionEnabled = ConfigManager.getBoolean("server.encryption.enabled", true);
        this.allowDuplicateUsername = ConfigManager.getBoolean("server.registration.allowDuplicateUsername", false);
        this.maxConcurrentUsers = ConfigManager.getInt("server.max.concurrent.users", 100);

        
        logger.info("AttendanceServer initialized successfully");
        logger.info("Encryption enabled: {}", encryptionEnabled);
        logger.info("Max concurrent users: {}", maxConcurrentUsers);
    }
    
    /**
     * Constructor for dependency injection (testing).
     */
    public AttendanceServer(AuthenticationService authService, AttendanceServiceImpl attendanceServiceImpl,
                           UserDAO userDAO, AttendanceDAO attendanceDAO, CourseDAO courseDAO) throws RemoteException {
        super();
        
        this.serverStartTime = LocalDateTime.now();
        this.authService = authService;
        this.attendanceServiceImpl = attendanceServiceImpl;
        this.userDAO = userDAO;
        this.attendanceDAO = attendanceDAO;
        this.courseDAO = courseDAO;
        this.notificationDAO = new NotificationDAO();
        
        this.encryptionEnabled = ConfigManager.getBoolean("server.encryption.enabled", true);
        this.maxConcurrentUsers = ConfigManager.getInt("server.max.concurrent.users", 100);
        this.allowDuplicateUsername = ConfigManager.getBoolean("server.registration.allowDuplicateUsername", false);
        
        logger.info("AttendanceServer initialized with injected dependencies");
    }
    
    // Authentication methods
    
    @Override
    public AuthenticationService.AuthenticatedUser authenticateUser(String username, String password) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("authenticateUser", () -> {
            logger.debug("Authentication request for user: {}", username);
            
            // Check server capacity
            checkServerCapacity();
            
            // Decrypt credentials if encryption is enabled
            String decryptedUsername = encryptionEnabled ? SecurityUtil.decrypt(username) : username;
            String decryptedPassword = encryptionEnabled ? SecurityUtil.decrypt(password) : password;
            
            // Authenticate user
            AuthenticationService.AuthenticatedUser authenticatedUser = 
                authService.authenticateUser(decryptedUsername, decryptedPassword);
            
            // Track client connection
            String sessionToken = authenticatedUser.getSessionToken();
            ClientConnection connection = new ClientConnection(authenticatedUser.getUser(), sessionToken);
            clientConnections.put(sessionToken, connection);
            activeConnections.incrementAndGet();
            
            logger.info("User authenticated successfully: {} (Active connections: {})", 
                    decryptedUsername, activeConnections.get());
            
            return authenticatedUser;
        });
    }
    
    @Override
    public User validateSession(String sessionToken) throws RemoteException, AuthenticationException {
        return executeWithErrorHandling("validateSession", () -> {
            User user = authService.validateSession(sessionToken);
            
            // Update client connection activity
            ClientConnection connection = clientConnections.get(sessionToken);
            if (connection != null) {
                connection.updateLastActivity();
            }
            
            return user;
        });
    }
    
    @Override
    public void logout(String sessionToken) throws RemoteException {
        executeWithErrorHandling("logout", () -> {
            authService.logout(sessionToken);
            
            // Remove client connection tracking
            ClientConnection connection = clientConnections.remove(sessionToken);
            if (connection != null) {
                activeConnections.decrementAndGet();
                logger.info("User logged out: {} (Active connections: {})", 
                        connection.getUser().getUsername(), activeConnections.get());
            }
            
            return null;
        });
    }
    
    @Override
    public void changePassword(String sessionToken, String currentPassword, String newPassword) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        executeWithErrorHandling("changePassword", () -> {
            // Decrypt passwords if encryption is enabled
            String decryptedCurrentPassword = encryptionEnabled ? SecurityUtil.decrypt(currentPassword) : currentPassword;
            String decryptedNewPassword = encryptionEnabled ? SecurityUtil.decrypt(newPassword) : newPassword;
            
            authService.changePassword(sessionToken, decryptedCurrentPassword, decryptedNewPassword);
            return null;
        });
    }
    
    @Override
    public boolean registerUser(String username, String email, String firstName, String lastName, 
                               String password, UserRole role, String classSection) 
            throws RemoteException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("registerUser", () -> {
            logger.debug("Registration request for user: {} ({})", username, role);
            
            // Check server capacity
            checkServerCapacity();
            
            // Decrypt credentials if encryption is enabled
            String decryptedUsername = encryptionEnabled ? SecurityUtil.decrypt(username) : username;
            String decryptedEmail = encryptionEnabled ? SecurityUtil.decrypt(email) : email;
            String decryptedFirstName = encryptionEnabled ? SecurityUtil.decrypt(firstName) : firstName;
            String decryptedLastName = encryptionEnabled ? SecurityUtil.decrypt(lastName) : lastName;
            String decryptedPassword = encryptionEnabled ? SecurityUtil.decrypt(password) : password;
            String decryptedClassSection = (classSection != null && encryptionEnabled) ? SecurityUtil.decrypt(classSection) : classSection;
            
            // Validate input
            if (decryptedUsername == null || decryptedUsername.trim().isEmpty()) {
                throw new ValidationException("username", "Username is required");
            }
            if (decryptedUsername.length() < 3 || decryptedUsername.length() > 50) {
                throw new ValidationException("username", "Username must be between 3 and 50 characters");
            }
            if (!decryptedUsername.matches("^[a-zA-Z0-9._-]+$")) {
                throw new ValidationException("username", "Username can only contain letters, numbers, dots, underscores, and hyphens");
            }
            
            if (decryptedEmail == null || decryptedEmail.trim().isEmpty()) {
                throw new ValidationException("email", "Email is required");
            }
            if (!decryptedEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("email", "Invalid email format");
            }
            
            if (decryptedFirstName == null || decryptedFirstName.trim().isEmpty()) {
                throw new ValidationException("firstName", "First name is required");
            }
            if (decryptedFirstName.length() > 50) {
                throw new ValidationException("firstName", "First name must not exceed 50 characters");
            }
            
            if (decryptedLastName == null || decryptedLastName.trim().isEmpty()) {
                throw new ValidationException("lastName", "Last name is required");
            }
            if (decryptedLastName.length() > 50) {
                throw new ValidationException("lastName", "Last name must not exceed 50 characters");
            }
            
            if (decryptedPassword == null || decryptedPassword.isEmpty()) {
                throw new ValidationException("password", "Password is required");
            }
            if (decryptedPassword.length() < 8) {
                throw new ValidationException("password", "Password must be at least 8 characters");
            }
            if (!decryptedPassword.matches(".*[A-Z].*")) {
                throw new ValidationException("password", "Password must contain at least one uppercase letter");
            }
            if (!decryptedPassword.matches(".*[a-z].*")) {
                throw new ValidationException("password", "Password must contain at least one lowercase letter");
            }
            if (!decryptedPassword.matches(".*\\d.*")) {
                throw new ValidationException("password", "Password must contain at least one digit");
            }
            if (!decryptedPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                throw new ValidationException("password", "Password must contain at least one special character");
            }
            
            // Only allow STUDENT and TEACHER roles for self-registration
            if (role != UserRole.STUDENT && role != UserRole.TEACHER) {
                throw new ValidationException("role", "Invalid role for self-registration");
            }
            
            // Check username uniqueness (skip if allowed for dev)
            if (!allowDuplicateUsername && userDAO.findByUsername(decryptedUsername) != null) {
                throw new ValidationException("username", "Username already exists");
            }
            
            // Check email uniqueness
            if (userDAO.findByEmail(decryptedEmail) != null) {
                throw new ValidationException("email", "Email address already exists");
            }
            
            // Create new user
            User newUser;
            if (role == UserRole.STUDENT) {
                Student student = new Student();
                // Set default student fields
                student.setStudentNumber("STU" + System.currentTimeMillis()); // Generate unique student number
                student.setProgram("General Studies"); // Default program
                student.setYearLevel(1); // Default year level
                // Use provided class section or default to "A"
                student.setClassSection(decryptedClassSection != null && !decryptedClassSection.trim().isEmpty() 
                    ? decryptedClassSection.trim() : "A");
                student.setEnrollmentDate(java.time.LocalDate.now()); // Current date
                newUser = student;
            } else if (role == UserRole.TEACHER) {
                Teacher teacher = new Teacher();
                // Set default teacher fields
                teacher.setEmployeeId("EMP" + System.currentTimeMillis()); // Generate unique employee ID
                teacher.setDepartment("General"); // Default department
                teacher.setSpecialization("General Education"); // Default specialization
                newUser = teacher;
            } else {
                // For ADMIN role (shouldn't happen in self-registration, but just in case)
                Admin admin = new Admin();
                newUser = admin;
            }
            
            newUser.setUsername(decryptedUsername);
            newUser.setEmail(decryptedEmail);
            newUser.setFirstName(decryptedFirstName);
            newUser.setLastName(decryptedLastName);
            newUser.setRole(role);
            newUser.setActive(true);
            
            // Hash password
            String hashedPassword = SecurityUtil.hashPassword(decryptedPassword);
            newUser.setPasswordHash(hashedPassword);
            
            // Create user in database
            boolean success = userDAO.createUser(newUser);
            
            if (success) {
                logger.info("User registered successfully: {} ({})", decryptedUsername, role);
            }
            
            return success;
        });
    }
    
    // User management methods (Admin only)
    
    @Override
    public boolean createUser(String sessionToken, User user) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("createUser", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Validate user data
            attendanceServiceImpl.validateUser(user);
            
            // Check email uniqueness
            if (userDAO.findByEmail(user.getEmail()) != null) {
                throw new ValidationException("email", "Email address already exists");
            }
            
            // Check username uniqueness
            if (userDAO.findByUsername(user.getUsername()) != null) {
                throw new ValidationException("username", "Username already exists");
            }
            
            // Hash password if provided
            if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                String hashedPassword = SecurityUtil.hashPassword(user.getPasswordHash());
                user.setPasswordHash(hashedPassword);
            }
            
            // Create user
            boolean success = userDAO.createUser(user);
            
            if (success) {
                logger.info("User created by admin {}: {} ({})", 
                        currentUser.getUsername(), user.getUsername(), user.getRole());
            }
            
            return success;
        });
    }
    
    @Override
    public boolean updateUser(String sessionToken, User user) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("updateUser", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Validate user data
            attendanceServiceImpl.validateUser(user);
            
            // Check email uniqueness (excluding current user)
            User existingUserWithEmail = userDAO.findByEmail(user.getEmail());
            if (existingUserWithEmail != null && existingUserWithEmail.getUserId() != user.getUserId()) {
                throw new ValidationException("email", "Email address already exists");
            }
            
            // Update user
            boolean success = userDAO.updateUser(user);
            
            if (success) {
                logger.info("User updated by admin {}: {}", currentUser.getUsername(), user.getUsername());
                
                // Force logout if user is currently logged in (to refresh session data)
                authService.forceLogoutUser(user.getUserId());
            }
            
            return success;
        });
    }
    
    @Override
    public boolean deleteUser(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("deleteUser", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Prevent admin from deleting themselves
            if (currentUser.getUserId() == userId) {
                throw new ValidationException("user_id", "Cannot delete your own account");
            }
            
            // Force logout user if currently logged in
            authService.forceLogoutUser(userId);
            
            // Delete user
            boolean success = userDAO.deleteUser(userId);
            
            if (success) {
                logger.info("User deleted by admin {}: userId={}", currentUser.getUsername(), userId);
            }
            
            return success;
        });
    }
    
    @Override
    public List<User> getAllUsers(String sessionToken) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getAllUsers", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            return userDAO.findAll();
        });
    }
    
    @Override
    public List<User> getUsersByRole(String sessionToken, UserRole role) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getUsersByRole", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            return userDAO.findByRole(role);
        });
    }
    
    // Course management methods
    
    @Override
    public boolean createCourse(String sessionToken, Course course) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("createCourse", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Validate course data
            attendanceServiceImpl.validateCourse(course);
            
            // Check course code uniqueness
            if (courseDAO.findByCourseCode(course.getCourseCode()) != null) {
                throw new ValidationException("course_code", "Course code already exists");
            }
            
            // Verify teacher exists and is active
            User teacher = userDAO.findById(course.getTeacherId());
            if (teacher == null || teacher.getRole() != UserRole.TEACHER || !teacher.isActive()) {
                throw new ValidationException("teacher_id", "Invalid or inactive teacher");
            }
            
            // Create course
            boolean success = courseDAO.createCourse(course);
            
            if (success) {
                logger.info("Course created by admin {}: {} ({})", 
                        currentUser.getUsername(), course.getCourseCode(), course.getCourseName());
            }
            
            return success;
        });
    }
    
    @Override
    public boolean updateCourse(String sessionToken, Course course) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("updateCourse", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Validate course data
            attendanceServiceImpl.validateCourse(course);
            
            // Check course code uniqueness (excluding current course)
            Course existingCourse = courseDAO.findByCourseCode(course.getCourseCode());
            if (existingCourse != null && existingCourse.getCourseId() != course.getCourseId()) {
                throw new ValidationException("course_code", "Course code already exists");
            }
            
            // Verify teacher exists and is active
            User teacher = userDAO.findById(course.getTeacherId());
            if (teacher == null || teacher.getRole() != UserRole.TEACHER || !teacher.isActive()) {
                throw new ValidationException("teacher_id", "Invalid or inactive teacher");
            }
            
            // Update course
            boolean success = courseDAO.updateCourse(course);
            
            if (success) {
                logger.info("Course updated by admin {}: {}", currentUser.getUsername(), course.getCourseCode());
            }
            
            return success;
        });
    }
    
    @Override
    public List<Course> getAllActiveCourses(String sessionToken) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getAllActiveCourses", () -> {
            // Validate session
            authService.validateSession(sessionToken);
            
            return courseDAO.findAllActive();
        });
    }
    
    @Override
    public List<Course> getCoursesByTeacher(String sessionToken, int teacherId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getCoursesByTeacher", () -> {
            // Validate session
            User currentUser = authService.validateSession(sessionToken);
            
            // Teachers can only see their own courses, admins can see any teacher's courses
            if (currentUser.getRole() == UserRole.TEACHER && currentUser.getUserId() != teacherId) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            return courseDAO.findByTeacherId(teacherId);
        });
    }
    
    @Override
    public List<Course> getCoursesByStudent(String sessionToken, int studentId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getCoursesByStudent", () -> {
            // Validate session
            User currentUser = authService.validateSession(sessionToken);
            
            // Students can only see their own courses, teachers and admins can see any student's courses
            if (currentUser.getRole() == UserRole.STUDENT && currentUser.getUserId() != studentId) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            return courseDAO.findByStudentId(studentId);
        });
    }
    
    @Override
    public boolean enrollStudent(String sessionToken, int studentId, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("enrollStudent", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Verify student exists and is active
            User student = userDAO.findById(studentId);
            if (student == null || student.getRole() != UserRole.STUDENT || !student.isActive()) {
                throw new ValidationException("student_id", "Invalid or inactive student");
            }
            
            // Verify course exists and is active
            Course course = courseDAO.findById(courseId);
            if (course == null || !course.isActive()) {
                throw new ValidationException("course_id", "Invalid or inactive course");
            }
            
            // Check if already enrolled
            if (courseDAO.isStudentEnrolled(studentId, courseId)) {
                throw new ValidationException("enrollment", "Student is already enrolled in this course");
            }
            
            // Enroll student
            boolean success = courseDAO.enrollStudent(studentId, courseId);
            
            if (success) {
                logger.info("Student enrolled by admin {}: studentId={}, courseId={}", 
                        currentUser.getUsername(), studentId, courseId);
            }
            
            return success;
        });
    }
    
    @Override
    public List<Student> getEnrolledStudents(String sessionToken, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getEnrolledStudents", () -> {
            // Validate session
            User currentUser = authService.validateSession(sessionToken);
            
            // Teachers can only see students in their courses
            if (currentUser.getRole() == UserRole.TEACHER) {
                Course course = courseDAO.findById(courseId);
                if (course == null || course.getTeacherId() != currentUser.getUserId()) {
                    throw AuthenticationException.insufficientPermissions();
                }
            }
            
            return courseDAO.getEnrolledStudents(courseId);
        });
    }
    
    @Override
    public User getUserById(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getUserById", () -> {
            // Validate session
            authService.validateSession(sessionToken);
            
            return userDAO.findById(userId);
        });
    }
    
    @Override
    public Course getCourseById(String sessionToken, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getCourseById", () -> {
            // Validate session
            authService.validateSession(sessionToken);
            
            return courseDAO.findById(courseId);
        });
    }
    
    @Override
    public boolean deleteCourse(String sessionToken, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("deleteCourse", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Delete course
            boolean success = courseDAO.deleteCourse(courseId);
            
            if (success) {
                logger.info("Course deleted by admin {}: courseId={}", currentUser.getUsername(), courseId);
            }
            
            return success;
        });
    }
    
    @Override
    public boolean assignTeacherToCourse(String sessionToken, int courseId, int teacherId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("assignTeacherToCourse", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Verify teacher exists and is active
            User teacher = userDAO.findById(teacherId);
            if (teacher == null || teacher.getRole() != UserRole.TEACHER || !teacher.isActive()) {
                throw new ValidationException("teacher_id", "Invalid or inactive teacher");
            }
            
            // Verify course exists
            Course course = courseDAO.findById(courseId);
            if (course == null) {
                throw new ValidationException("course_id", "Course not found");
            }
            
            // Update course with new teacher
            course.setTeacherId(teacherId);
            boolean success = courseDAO.updateCourse(course);
            
            if (success) {
                logger.info("Teacher assigned to course by admin {}: courseId={}, teacherId={}", 
                        currentUser.getUsername(), courseId, teacherId);
            }
            
            return success;
        });
    }
    
    @Override
    public List<Enrollment> getAllEnrollments(String sessionToken) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getAllEnrollments", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Implementation would depend on EnrollmentDAO
            // For now, return empty list
            return List.of();
        });
    }
    
    @Override
    public boolean dropStudentFromCourse(String sessionToken, int enrollmentId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("dropStudentFromCourse", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Implementation would depend on EnrollmentDAO
            // For now, return true
            logger.info("Student dropped from course by admin {}: enrollmentId={}", 
                    currentUser.getUsername(), enrollmentId);
            return true;
        });
    }
    
    // Attendance management methods
    
    @Override
    public boolean markAttendance(String sessionToken, AttendanceRecord record) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("markAttendance", () -> {
            return attendanceServiceImpl.markAttendance(sessionToken, record);
        });
    }
    
    @Override
    public boolean updateAttendance(String sessionToken, AttendanceRecord record) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("updateAttendance", () -> {
            return attendanceServiceImpl.updateAttendance(sessionToken, record);
        });
    }
    
    @Override
    public List<AttendanceRecord> getAttendanceRecords(String sessionToken, int studentId, 
                                                      LocalDate startDate, LocalDate endDate) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getAttendanceRecords", () -> {
            return attendanceServiceImpl.getFilteredAttendanceRecords(sessionToken, studentId, 0, 
                    startDate, endDate, null);
        });
    }
    
    @Override
    public List<AttendanceRecord> getAttendanceByClassDate(String sessionToken, int courseId, LocalDate date) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getAttendanceByClassDate", () -> {
            return attendanceServiceImpl.getFilteredAttendanceRecords(sessionToken, 0, courseId, 
                    date, date, null);
        });
    }
    
    @Override
    public Map<String, Object> getAttendanceStatistics(String sessionToken, int studentId, int courseId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getAttendanceStatistics", () -> {
            // Validate session
            User currentUser = authService.validateSession(sessionToken);
            
            // Students can only see their own statistics
            if (currentUser.getRole() == UserRole.STUDENT && currentUser.getUserId() != studentId) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            // Teachers can only see statistics for their courses
            if (currentUser.getRole() == UserRole.TEACHER) {
                Course course = courseDAO.findById(courseId);
                if (course == null || course.getTeacherId() != currentUser.getUserId()) {
                    throw AuthenticationException.insufficientPermissions();
                }
            }
            
            return attendanceDAO.calculateAttendanceStatistics(studentId, courseId);
        });
    }
    
    // Notification methods
    
    @Override
    public List<Notification> getNotifications(String sessionToken, int userId, boolean unreadOnly) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getNotifications", () -> {
            // Validate session
            User currentUser = authService.validateSession(sessionToken);
            
            // Users can only see their own notifications, admins can see any user's notifications
            if (currentUser.getRole() != UserRole.ADMIN && currentUser.getUserId() != userId) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            return notificationDAO.findByUser(userId, unreadOnly);
        });
    }
    
    @Override
    public int getUnreadNotificationCount(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getUnreadNotificationCount", () -> {
            User currentUser = authService.validateSession(sessionToken);
            
            // Users can only see their own count, admins can see any user's count
            if (currentUser.getRole() != UserRole.ADMIN && currentUser.getUserId() != userId) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            return notificationDAO.getUnreadCount(userId);
        });
    }
    
    @Override
    public boolean markNotificationAsRead(String sessionToken, int notificationId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("markNotificationAsRead", () -> {
            // Validate session
            authService.validateSession(sessionToken);
            
            return notificationDAO.markAsRead(notificationId);
        });
    }
    
    @Override
    public int markAllNotificationsAsRead(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("markAllNotificationsAsRead", () -> {
            User user = authService.validateSession(sessionToken);
            
            // Users can only mark their own notifications, admins can mark anyone's
            if (user.getUserId() != userId && user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("You can only mark your own notifications as read");
            }
            
            return notificationDAO.markAllAsRead(userId);
        });
    }
    
    @Override
    public boolean sendNotification(String sessionToken, Notification notification) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("sendNotification", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            return notificationDAO.insertNotification(notification);
        });
    }
    
    // Report generation methods
    
    @Override
    public byte[] generateAttendanceReport(String sessionToken, ReportCriteria criteria) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("generateAttendanceReport", () -> {
            // Validate session
            User currentUser = authService.validateSession(sessionToken);
            
            // Apply role-based restrictions
            if (currentUser.getRole() == UserRole.STUDENT) {
                // Students can only generate reports for themselves
                criteria.setStudentId(currentUser.getUserId());
            } else if (currentUser.getRole() == UserRole.TEACHER) {
                // Teachers can only generate reports for their courses
                if (criteria.getCourseId() > 0) {
                    Course course = courseDAO.findById(criteria.getCourseId());
                    if (course == null || course.getTeacherId() != currentUser.getUserId()) {
                        throw AuthenticationException.insufficientPermissions();
                    }
                }
            }
            
            // Implementation would depend on ReportService
            // For now, return empty byte array
            return new byte[0];
        });
    }
    
    @Override
    public Map<String, Object> getSystemStatistics(String sessionToken) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getSystemStatistics", () -> {
            // Validate session and admin permissions
            User currentUser = authService.validateSession(sessionToken);
            if (currentUser.getRole() != UserRole.ADMIN) {
                throw AuthenticationException.insufficientPermissions();
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userDAO.getTotalUserCount());
            stats.put("activeUsers", userDAO.getActiveUserCount());
            stats.put("totalCourses", courseDAO.getTotalCourseCount());
            stats.put("activeCourses", courseDAO.getActiveCourseCount());
            stats.put("totalAttendanceRecords", attendanceDAO.getTotalRecordCount());
            stats.put("activeConnections", activeConnections.get());
            stats.put("totalRequests", totalRequests.get());
            stats.put("serverUptime", java.time.Duration.between(serverStartTime, LocalDateTime.now()).toMinutes());
            
            return stats;
        });
    }
    
    // System health and monitoring
    
    @Override
    public boolean isHealthy() throws RemoteException {
        try {
            // Check database connectivity
            userDAO.testConnection();
            attendanceDAO.testConnection();
            courseDAO.testConnection();
            
            // Check if server is not overloaded
            if (activeConnections.get() > maxConcurrentUsers) {
                logger.warn("Server overloaded: {} active connections (max: {})", 
                        activeConnections.get(), maxConcurrentUsers);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getServerInfo() throws RemoteException {
        Map<String, Object> info = new HashMap<>();
        info.put("serverVersion", "1.0.0");
        info.put("startTime", serverStartTime);
        info.put("uptime", java.time.Duration.between(serverStartTime, LocalDateTime.now()).toMinutes());
        info.put("activeConnections", activeConnections.get());
        info.put("totalRequests", totalRequests.get());
        info.put("maxConcurrentUsers", maxConcurrentUsers);
        info.put("encryptionEnabled", encryptionEnabled);
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        
        return info;
    }
    
    // Maintenance mode methods
    
    @Override
    public boolean enableMaintenanceMode(String sessionToken, String reason, int estimatedDurationMinutes) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("enableMaintenanceMode", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can enable maintenance mode");
            }
            
            MaintenanceManager.getInstance().enableMaintenanceMode(reason, estimatedDurationMinutes);
            logger.info("Maintenance mode enabled by admin: {}", user.getUsername());
            return true;
        });
    }
    
    @Override
    public boolean disableMaintenanceMode(String sessionToken) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("disableMaintenanceMode", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can disable maintenance mode");
            }
            
            MaintenanceManager.getInstance().disableMaintenanceMode();
            logger.info("Maintenance mode disabled by admin: {}", user.getUsername());
            return true;
        });
    }
    
    @Override
    public boolean scheduleMaintenanceMode(String sessionToken, String startTime, int estimatedDurationMinutes, String reason) 
            throws RemoteException, AuthenticationException, ValidationException {
        
        return executeWithErrorHandling("scheduleMaintenanceMode", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can schedule maintenance mode");
            }
            
            try {
                LocalDateTime maintenanceStart = LocalDateTime.parse(startTime);
                MaintenanceManager.getInstance().scheduleMaintenanceMode(maintenanceStart, estimatedDurationMinutes, reason);
                logger.info("Maintenance mode scheduled by admin: {} for {}", user.getUsername(), maintenanceStart);
                return true;
            } catch (Exception e) {
                throw new ValidationException("Invalid start time format: " + startTime);
            }
        });
    }
    
    @Override
    public boolean cancelScheduledMaintenance(String sessionToken) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("cancelScheduledMaintenance", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can cancel scheduled maintenance");
            }
            
            MaintenanceManager.getInstance().cancelScheduledMaintenance();
            logger.info("Scheduled maintenance cancelled by admin: {}", user.getUsername());
            return true;
        });
    }
    
    @Override
    public Map<String, Object> getMaintenanceModeInfo(String sessionToken) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("getMaintenanceModeInfo", () -> {
            authService.validateSession(sessionToken);
            return MaintenanceManager.getInstance().getMaintenanceModeInfo();
        });
    }
    
    @Override
    public String createBackup(String sessionToken, String backupName) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("createBackup", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can create backups");
            }
            
            String backupId = MaintenanceManager.getInstance().createBackup(backupName);
            logger.info("Backup created by admin {}: {} (ID: {})", user.getUsername(), backupName, backupId);
            return backupId;
        });
    }
    
    @Override
    public boolean restoreFromBackup(String sessionToken, String backupId) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("restoreFromBackup", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can restore backups");
            }
            
            boolean success = MaintenanceManager.getInstance().restoreFromBackup(backupId);
            if (success) {
                logger.info("Backup restored by admin {}: {}", user.getUsername(), backupId);
            }
            return success;
        });
    }
    
    @Override
    public List<Map<String, Object>> getAvailableBackups(String sessionToken) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("getAvailableBackups", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can view backups");
            }
            
            List<MaintenanceManager.BackupInfo> backups = MaintenanceManager.getInstance().getAvailableBackups();
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            
            for (MaintenanceManager.BackupInfo backup : backups) {
                Map<String, Object> backupMap = new HashMap<>();
                backupMap.put("backupId", backup.getBackupId());
                backupMap.put("name", backup.getName());
                backupMap.put("createdTime", backup.getCreatedTime());
                backupMap.put("completedTime", backup.getCompletedTime());
                backupMap.put("version", backup.getVersion());
                backupMap.put("status", backup.getStatus());
                backupMap.put("sizeBytes", backup.getSizeBytes());
                result.add(backupMap);
            }
            
            return result;
        });
    }
    
    @Override
    public boolean deleteBackup(String sessionToken, String backupId) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("deleteBackup", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can delete backups");
            }
            
            boolean success = MaintenanceManager.getInstance().deleteBackup(backupId);
            if (success) {
                logger.info("Backup deleted by admin {}: {}", user.getUsername(), backupId);
            }
            return success;
        });
    }
    
    @Override
    public boolean initiateSystemUpdate(String sessionToken, String newVersion, String updateDescription) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("initiateSystemUpdate", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can initiate system updates");
            }
            
            boolean success = MaintenanceManager.getInstance().initiateSystemUpdate(newVersion, updateDescription);
            if (success) {
                logger.info("System update initiated by admin {}: v{}", user.getUsername(), newVersion);
            }
            return success;
        });
    }
    
    @Override
    public boolean completeSystemUpdate(String sessionToken, String newVersion) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("completeSystemUpdate", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can complete system updates");
            }
            
            MaintenanceManager.getInstance().completeSystemUpdate(newVersion);
            logger.info("System update completed by admin {}: v{}", user.getUsername(), newVersion);
            return true;
        });
    }
    
    @Override
    public String getSystemVersion() throws RemoteException {
        return MaintenanceManager.getInstance().getSystemVersion();
    }
    
    @Override
    public List<Map<String, Object>> getMaintenanceHistory(String sessionToken) 
            throws RemoteException, AuthenticationException {
        
        return executeWithErrorHandling("getMaintenanceHistory", () -> {
            User user = authService.validateSession(sessionToken);
            if (user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("Only administrators can view maintenance history");
            }
            
            List<MaintenanceManager.MaintenanceEvent> events = MaintenanceManager.getInstance().getMaintenanceHistory();
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            
            for (MaintenanceManager.MaintenanceEvent event : events) {
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("eventId", event.getEventId());
                eventMap.put("eventType", event.getEventType());
                eventMap.put("details", event.getDetails());
                eventMap.put("timestamp", event.getTimestamp());
                result.add(eventMap);
            }
            
            return result;
        });
    }
    
    @Override
    public Map<String, Object> getNotificationPreferences(String sessionToken, int userId) 
            throws RemoteException, AuthenticationException, DatabaseException {
        
        return executeWithErrorHandling("getNotificationPreferences", () -> {
            User user = authService.validateSession(sessionToken);
            
            // Users can only get their own preferences, admins can get anyone's
            if (user.getUserId() != userId && user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("You can only view your own notification preferences");
            }
            
            // Return default preferences
            Map<String, Object> preferences = new HashMap<>();
            preferences.put("emailNotifications", true);
            preferences.put("smsNotifications", false);
            preferences.put("pushNotifications", true);
            return preferences;
        });
    }
    
    @Override
    public boolean updateNotificationPreferences(String sessionToken, int userId, Map<String, Object> preferences) 
            throws RemoteException, AuthenticationException, ValidationException, DatabaseException {
        
        return executeWithErrorHandling("updateNotificationPreferences", () -> {
            User user = authService.validateSession(sessionToken);
            
            // Users can only update their own preferences, admins can update anyone's
            if (user.getUserId() != userId && user.getRole() != UserRole.ADMIN) {
                throw new AuthenticationException("You can only update your own notification preferences");
            }
            
            // Preferences updated successfully (no-op for now)
            return true;
        });
    }
    
    // Private helper methods
    
    /**
     * Executes a service method with comprehensive error handling and logging.
     */
    private <T> T executeWithErrorHandling(String methodName, ServiceOperation<T> operation) 
            throws RemoteException {
        
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("Executing method: {}", methodName);
            T result = operation.execute();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Method {} completed in {} ms", methodName, duration);
            
            return result;
            
        } catch (AuthenticationException e) {
            // Preserve authentication errors without wrapping
            logger.warn("Authentication failed in {}: {}", methodName, e.getMessage());
            throw new RemoteException(e.getMessage(), e);
        } catch (ValidationException | DatabaseException e) {
            logger.warn("Method {} failed: {}", methodName, e.getMessage());
            throw new RemoteException("Service error in " + methodName + ": " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in method " + methodName, e);
            throw new RemoteException("Unexpected server error in " + methodName, e);
        }
    }
    
    /**
     * Checks if the server has capacity for new connections.
     */
    private void checkServerCapacity() throws AuthenticationException {
        if (activeConnections.get() >= maxConcurrentUsers) {
            logger.warn("Server at capacity: {} active connections (max: {})", 
                    activeConnections.get(), maxConcurrentUsers);
            throw new AuthenticationException("Server is at maximum capacity. Please try again later.");
        }
    }
    
    /**
     * Functional interface for service operations.
     */
    @FunctionalInterface
    private interface ServiceOperation<T> {
        T execute() throws AuthenticationException, ValidationException, DatabaseException;
    }
    
    /**
     * Inner class for tracking client connections.
     */
    private static class ClientConnection {
        private final User user;
        private final String sessionToken;
        private final LocalDateTime connectedAt;
        private LocalDateTime lastActivity;
        
        public ClientConnection(User user, String sessionToken) {
            this.user = user;
            this.sessionToken = sessionToken;
            this.connectedAt = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
        }
        
        public User getUser() {
            return user;
        }
        
        public String getSessionToken() {
            return sessionToken;
        }
        
        public LocalDateTime getConnectedAt() {
            return connectedAt;
        }
        
        public LocalDateTime getLastActivity() {
            return lastActivity;
        }
        
        public void updateLastActivity() {
            this.lastActivity = LocalDateTime.now();
        }
        
        public long getIdleTimeMinutes() {
            return java.time.Duration.between(lastActivity, LocalDateTime.now()).toMinutes();
        }
    }
}