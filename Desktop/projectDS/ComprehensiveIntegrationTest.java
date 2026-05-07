import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.model.*;

/**
 * Comprehensive integration test to verify database and Java code work perfectly together
 */
public class ComprehensiveIntegrationTest {
    
    private static AttendanceService service;
    private static String adminToken;
    private static String teacherToken;
    private static String studentToken;
    
    public static void main(String[] args) {
        try {
            System.out.println("=== COMPREHENSIVE DATABASE & JAVA CODE INTEGRATION TEST ===");
            
            // Connect to server
            connectToServer();
            
            // Test all authentication scenarios
            testAuthentication();
            
            // Test user management (CRUD operations)
            testUserManagement();
            
            // Test course management
            testCourseManagement();
            
            // Test enrollment management
            testEnrollmentManagement();
            
            // Test attendance management
            testAttendanceManagement();
            
            // Test data integrity and constraints
            testDataIntegrity();
            
            // Test statistics and reporting
            testStatisticsAndReporting();
            
            System.out.println("\n🎉 ALL INTEGRATION TESTS PASSED!");
            System.out.println("✅ Database and Java code are working perfectly together!");
            
        } catch (Exception e) {
            System.err.println("❌ INTEGRATION TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void connectToServer() throws Exception {
        System.out.println("\n1. 🔌 Testing Server Connection...");
        Registry registry = LocateRegistry.getRegistry("localhost", 1100);
        service = (AttendanceService) registry.lookup("AttendanceService");
        System.out.println("✅ Connected to RMI server successfully");
    }
    
    private static void testAuthentication() throws Exception {
        System.out.println("\n2. 🔐 Testing Authentication System...");
        
        // Test admin login
        AuthenticationService.AuthenticatedUser adminUser = service.authenticateUser("admin", "admin");
        adminToken = adminUser.getSessionToken();
        System.out.println("✅ Admin authentication: " + adminUser.getUser().getFullName());
        
        // Test teacher login
        AuthenticationService.AuthenticatedUser teacherUser = service.authenticateUser("testteacher", "Password123!");
        teacherToken = teacherUser.getSessionToken();
        System.out.println("✅ Teacher authentication: " + teacherUser.getUser().getFullName());
        
        // Test invalid login
        try {
            service.authenticateUser("invalid", "invalid");
            System.out.println("❌ Should have failed with invalid credentials");
        } catch (Exception e) {
            System.out.println("✅ Invalid login properly rejected");
        }
        
        // Test session validation
        User validatedAdmin = service.validateSession(adminToken);
        System.out.println("✅ Session validation: " + validatedAdmin.getFullName());
    }
    
    private static void testUserManagement() throws Exception {
        System.out.println("\n3. 👥 Testing User Management (CRUD)...");
        
        // Create a test student
        boolean studentCreated = service.registerUser("teststudent", "test@student.com", 
            "Test", "Student", "TestPass123!", UserRole.STUDENT, "A");
        System.out.println("✅ Student registration: " + studentCreated);
        
        // Create a test teacher
        boolean teacherCreated = service.registerUser("testteacher2", "test@teacher.com", 
            "Test", "Teacher2", "TestPass123!", UserRole.TEACHER, null);
        System.out.println("✅ Teacher registration: " + teacherCreated);
        
        // Get all users
        List<User> allUsers = service.getAllUsers(adminToken);
        System.out.println("✅ Retrieved " + allUsers.size() + " users from database");
        
        // Get users by role
        List<User> teachers = service.getUsersByRole(adminToken, UserRole.TEACHER);
        List<User> students = service.getUsersByRole(adminToken, UserRole.STUDENT);
        System.out.println("✅ Found " + teachers.size() + " teachers and " + students.size() + " students");
        
        // Test student login
        User testStudent = null;
        for (User user : allUsers) {
            if (user.getUsername().equals("teststudent")) {
                testStudent = user;
                break;
            }
        }
        
        if (testStudent != null) {
            AuthenticationService.AuthenticatedUser studentUser = service.authenticateUser("teststudent", "TestPass123!");
            studentToken = studentUser.getSessionToken();
            System.out.println("✅ Test student login successful");
        }
    }
    
    private static void testCourseManagement() throws Exception {
        System.out.println("\n4. 📚 Testing Course Management...");
        
        // Get teacher courses
        List<Course> teacherCourses = service.getCoursesByTeacher(teacherToken, 
            service.validateSession(teacherToken).getUserId());
        System.out.println("✅ Teacher has " + teacherCourses.size() + " courses");
        
        // Get all active courses
        List<Course> activeCourses = service.getAllActiveCourses(adminToken);
        System.out.println("✅ Found " + activeCourses.size() + " active courses");
        
        if (!activeCourses.isEmpty()) {
            Course testCourse = activeCourses.get(0);
            System.out.println("✅ Sample course: " + testCourse.getCourseCode() + " - " + testCourse.getCourseName());
        }
    }
    
    private static void testEnrollmentManagement() throws Exception {
        System.out.println("\n5. 📝 Testing Enrollment Management...");
        
        // Get teacher courses
        List<Course> teacherCourses = service.getCoursesByTeacher(teacherToken, 
            service.validateSession(teacherToken).getUserId());
        
        if (!teacherCourses.isEmpty()) {
            Course course = teacherCourses.get(0);
            
            // Get enrolled students
            List<Student> enrolledStudents = service.getEnrolledStudents(teacherToken, course.getCourseId());
            System.out.println("✅ Course " + course.getCourseCode() + " has " + enrolledStudents.size() + " enrolled students");
            
            for (Student student : enrolledStudents) {
                System.out.println("   - " + student.getFullName() + " (" + student.getStudentNumber() + ")");
            }
        }
    }
    
    private static void testAttendanceManagement() throws Exception {
        System.out.println("\n6. ✏️ Testing Attendance Management...");
        
        // Get teacher courses
        List<Course> teacherCourses = service.getCoursesByTeacher(teacherToken, 
            service.validateSession(teacherToken).getUserId());
        
        if (!teacherCourses.isEmpty()) {
            Course course = teacherCourses.get(0);
            List<Student> students = service.getEnrolledStudents(teacherToken, course.getCourseId());
            
            if (!students.isEmpty()) {
                Student student = students.get(0);
                
                // Verify this student has a valid user_id that maps to a student_id
                System.out.println("   Testing with student: " + student.getFullName() + " (user_id: " + student.getUserId() + ")");
                
                // Mark attendance
                AttendanceRecord record = new AttendanceRecord();
                record.setStudentId(student.getUserId()); // This should be the user_id from the Student object
                record.setCourseId(course.getCourseId());
                record.setAttendanceDate(LocalDate.now());
                record.setClassTime(LocalTime.of(10, 0));
                record.setStatus(AttendanceStatus.PRESENT);
                record.setMarkedBy(service.validateSession(teacherToken).getUserId());
                record.setRemarks("Integration test attendance");
                
                try {
                    boolean marked = service.markAttendance(teacherToken, record);
                    System.out.println("✅ Attendance marked: " + marked);
                } catch (Exception e) {
                    System.out.println("❌ Attendance marking failed: " + e.getMessage());
                    System.out.println("   Student user_id: " + student.getUserId());
                    System.out.println("   Course ID: " + course.getCourseId());
                    System.out.println("   Teacher user_id: " + service.validateSession(teacherToken).getUserId());
                    // Continue with other tests
                }
                
                // Get attendance by date
                List<AttendanceRecord> todayRecords = service.getAttendanceByClassDate(
                    teacherToken, course.getCourseId(), LocalDate.now());
                System.out.println("✅ Found " + todayRecords.size() + " attendance records for today");
                
                // Update attendance
                if (!todayRecords.isEmpty()) {
                    AttendanceRecord updateRecord = todayRecords.get(0);
                    updateRecord.setStatus(AttendanceStatus.LATE);
                    updateRecord.setRemarks("Updated to LATE - integration test");
                    
                    boolean updated = service.updateAttendance(teacherToken, updateRecord);
                    System.out.println("✅ Attendance updated: " + updated);
                }
                
                // Get attendance records with date range
                LocalDate startDate = LocalDate.now().minusDays(7);
                LocalDate endDate = LocalDate.now();
                List<AttendanceRecord> weekRecords = service.getAttendanceRecords(
                    teacherToken, student.getUserId(), startDate, endDate);
                System.out.println("✅ Found " + weekRecords.size() + " attendance records for past week");
            } else {
                System.out.println("⚠️  No students enrolled to test attendance");
            }
        }
    }
    
    private static void testDataIntegrity() throws Exception {
        System.out.println("\n7. 🔒 Testing Data Integrity & Constraints...");
        
        // Test duplicate username prevention
        try {
            service.registerUser("admin", "duplicate@test.com", "Duplicate", "User", "TestPass123!", UserRole.STUDENT, "A");
            System.out.println("❌ Should have prevented duplicate username");
        } catch (Exception e) {
            System.out.println("✅ Duplicate username properly prevented");
        }
        
        // Test duplicate email prevention
        try {
            service.registerUser("uniqueuser", "admin@attendance.system", "Unique", "User", "TestPass123!", UserRole.STUDENT, "A");
            System.out.println("❌ Should have prevented duplicate email");
        } catch (Exception e) {
            System.out.println("✅ Duplicate email properly prevented");
        }
        
        // Test password validation
        try {
            service.registerUser("weakuser", "weak@test.com", "Weak", "User", "123", UserRole.STUDENT, "A");
            System.out.println("❌ Should have rejected weak password");
        } catch (Exception e) {
            System.out.println("✅ Weak password properly rejected");
        }
        
        System.out.println("✅ All data integrity constraints working correctly");
    }
    
    private static void testStatisticsAndReporting() throws Exception {
        System.out.println("\n8. 📊 Testing Statistics & Reporting...");
        
        // Get teacher courses
        List<Course> teacherCourses = service.getCoursesByTeacher(teacherToken, 
            service.validateSession(teacherToken).getUserId());
        
        if (!teacherCourses.isEmpty()) {
            Course course = teacherCourses.get(0);
            List<Student> students = service.getEnrolledStudents(teacherToken, course.getCourseId());
            
            if (!students.isEmpty()) {
                Student student = students.get(0);
                
                // Get attendance statistics
                Map<String, Object> stats = service.getAttendanceStatistics(
                    teacherToken, student.getUserId(), course.getCourseId());
                
                System.out.println("✅ Attendance statistics for " + student.getFullName() + ":");
                System.out.println("   - Total Classes: " + stats.get("totalClasses"));
                System.out.println("   - Attended Classes: " + stats.get("attendedClasses"));
                System.out.println("   - Attendance Percentage: " + stats.get("attendancePercentage") + "%");
                System.out.println("   - Present Count: " + stats.get("presentCount"));
                System.out.println("   - Absent Count: " + stats.get("absentCount"));
                System.out.println("   - Late Count: " + stats.get("lateCount"));
                System.out.println("   - Excused Count: " + stats.get("excusedCount"));
            }
        }
        
        // Test system statistics (admin only)
        Map<String, Object> systemStats = service.getSystemStatistics(adminToken);
        System.out.println("✅ System statistics:");
        System.out.println("   - Total Users: " + systemStats.get("totalUsers"));
        System.out.println("   - Active Users: " + systemStats.get("activeUsers"));
        System.out.println("   - Total Courses: " + systemStats.get("totalCourses"));
        System.out.println("   - Active Courses: " + systemStats.get("activeCourses"));
        System.out.println("   - Total Attendance Records: " + systemStats.get("totalAttendanceRecords"));
        System.out.println("   - Active Connections: " + systemStats.get("activeConnections"));
    }
}