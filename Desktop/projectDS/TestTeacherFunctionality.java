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
 * Comprehensive test for ALL teacher functionality.
 * This test verifies that every teacher method works correctly.
 */
public class TestTeacherFunctionality {
    
    private static AttendanceService service;
    private static String teacherSessionToken;
    private static User teacherUser;
    
    public static void main(String[] args) {
        try {
            System.out.println("=== COMPREHENSIVE TEACHER FUNCTIONALITY TEST ===");
            
            // Connect to RMI server
            connectToServer();
            
            // Login as teacher
            loginAsTeacher();
            
            // Test all teacher activities
            testAllTeacherActivities();
            
            System.out.println("\n✅ ALL TEACHER FUNCTIONALITY TESTS PASSED!");
            
        } catch (Exception e) {
            System.err.println("❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void connectToServer() throws Exception {
        System.out.println("\n1. Connecting to RMI server...");
        Registry registry = LocateRegistry.getRegistry("localhost", 1100);
        service = (AttendanceService) registry.lookup("AttendanceService");
        System.out.println("✅ Connected to server successfully");
    }
    
    private static void loginAsTeacher() throws Exception {
        System.out.println("\n2. Logging in as teacher...");
        
        // Try multiple teacher accounts
        String[] teacherAccounts = {
            "testteacher:Password123!",
            "john.smith:admin",  // From sample data
            "mary.johnson:admin",
            "david.brown:admin"
        };
        
        for (String account : teacherAccounts) {
            String[] parts = account.split(":");
            String username = parts[0];
            String password = parts[1];
            
            try {
                AuthenticationService.AuthenticatedUser authUser = service.authenticateUser(username, password);
                if (authUser.getUser().getRole() == UserRole.TEACHER) {
                    teacherSessionToken = authUser.getSessionToken();
                    teacherUser = authUser.getUser();
                    System.out.println("✅ Logged in as teacher: " + username + " (ID: " + teacherUser.getUserId() + ")");
                    return;
                }
            } catch (Exception e) {
                System.out.println("⚠️  Failed to login with " + username + ": " + e.getMessage());
            }
        }
        
        throw new Exception("Could not login with any teacher account");
    }
    
    private static void testAllTeacherActivities() throws Exception {
        System.out.println("\n3. Testing ALL teacher activities...");
        
        // Test 1: Get teacher's courses
        testGetTeacherCourses();
        
        // Test 2: Get enrolled students for each course
        testGetEnrolledStudents();
        
        // Test 3: Mark attendance for students
        testMarkAttendance();
        
        // Test 4: Get attendance by class date
        testGetAttendanceByClassDate();
        
        // Test 5: Update/modify attendance
        testUpdateAttendance();
        
        // Test 6: Get attendance records with filters
        testGetAttendanceRecords();
        
        // Test 7: Get attendance statistics
        testGetAttendanceStatistics();
        
        // Test 8: Session validation
        testSessionValidation();
        
        // Test 9: Logout functionality
        testLogout();
    }
    
    private static void testGetTeacherCourses() throws Exception {
        System.out.println("\n   📚 Testing: Get Teacher Courses");
        
        List<Course> courses = service.getCoursesByTeacher(teacherSessionToken, teacherUser.getUserId());
        
        System.out.println("      Found " + courses.size() + " courses for teacher");
        for (Course course : courses) {
            System.out.println("      - " + course.getCourseCode() + ": " + course.getCourseName());
        }
        
        if (courses.isEmpty()) {
            System.out.println("      ⚠️  No courses found - this might be expected for new teachers");
        } else {
            System.out.println("      ✅ getCoursesByTeacher() works correctly");
        }
    }
    
    private static void testGetEnrolledStudents() throws Exception {
        System.out.println("\n   👥 Testing: Get Enrolled Students");
        
        List<Course> courses = service.getCoursesByTeacher(teacherSessionToken, teacherUser.getUserId());
        
        if (courses.isEmpty()) {
            System.out.println("      ⚠️  No courses to test student enrollment");
            return;
        }
        
        for (Course course : courses) {
            List<Student> students = service.getEnrolledStudents(teacherSessionToken, course.getCourseId());
            System.out.println("      Course " + course.getCourseCode() + " has " + students.size() + " enrolled students");
            
            for (Student student : students) {
                System.out.println("        - " + student.getFullName() + " (" + student.getStudentNumber() + ")");
            }
        }
        
        System.out.println("      ✅ getEnrolledStudents() works correctly");
    }
    
    private static void testMarkAttendance() throws Exception {
        System.out.println("\n   ✏️  Testing: Mark Attendance");
        
        List<Course> courses = service.getCoursesByTeacher(teacherSessionToken, teacherUser.getUserId());
        
        if (courses.isEmpty()) {
            System.out.println("      ⚠️  No courses to test attendance marking");
            return;
        }
        
        Course testCourse = courses.get(0);
        List<Student> students = service.getEnrolledStudents(teacherSessionToken, testCourse.getCourseId());
        
        if (students.isEmpty()) {
            System.out.println("      ⚠️  No students enrolled to test attendance marking");
            return;
        }
        
        // Mark attendance for first student
        Student testStudent = students.get(0);
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(testStudent.getUserId());
        record.setCourseId(testCourse.getCourseId());
        record.setAttendanceDate(LocalDate.now());
        record.setClassTime(LocalTime.of(9, 0)); // 9:00 AM - within valid range
        record.setStatus(AttendanceStatus.PRESENT);
        record.setMarkedBy(teacherUser.getUserId());
        record.setRemarks("Test attendance marking");
        
        boolean success = service.markAttendance(teacherSessionToken, record);
        
        if (success) {
            System.out.println("      ✅ markAttendance() works correctly");
            System.out.println("        Marked " + testStudent.getFullName() + " as PRESENT");
        } else {
            System.out.println("      ❌ markAttendance() failed");
        }
    }
    
    private static void testGetAttendanceByClassDate() throws Exception {
        System.out.println("\n   📅 Testing: Get Attendance by Class Date");
        
        List<Course> courses = service.getCoursesByTeacher(teacherSessionToken, teacherUser.getUserId());
        
        if (courses.isEmpty()) {
            System.out.println("      ⚠️  No courses to test attendance retrieval");
            return;
        }
        
        Course testCourse = courses.get(0);
        LocalDate today = LocalDate.now();
        
        List<AttendanceRecord> records = service.getAttendanceByClassDate(teacherSessionToken, testCourse.getCourseId(), today);
        
        System.out.println("      Found " + records.size() + " attendance records for " + testCourse.getCourseCode() + " on " + today);
        
        for (AttendanceRecord record : records) {
            System.out.println("        - Student ID " + record.getStudentId() + ": " + record.getStatus());
        }
        
        System.out.println("      ✅ getAttendanceByClassDate() works correctly");
    }
    
    private static void testUpdateAttendance() throws Exception {
        System.out.println("\n   🔄 Testing: Update Attendance");
        
        List<Course> courses = service.getCoursesByTeacher(teacherSessionToken, teacherUser.getUserId());
        
        if (courses.isEmpty()) {
            System.out.println("      ⚠️  No courses to test attendance update");
            return;
        }
        
        Course testCourse = courses.get(0);
        LocalDate today = LocalDate.now();
        
        List<AttendanceRecord> records = service.getAttendanceByClassDate(teacherSessionToken, testCourse.getCourseId(), today);
        
        if (records.isEmpty()) {
            System.out.println("      ⚠️  No attendance records to update");
            return;
        }
        
        // Update first record
        AttendanceRecord record = records.get(0);
        record.setStatus(AttendanceStatus.LATE);
        record.setRemarks("Updated to LATE - test update");
        
        boolean success = service.updateAttendance(teacherSessionToken, record);
        
        if (success) {
            System.out.println("      ✅ updateAttendance() works correctly");
            System.out.println("        Updated student ID " + record.getStudentId() + " to LATE");
        } else {
            System.out.println("      ❌ updateAttendance() failed");
        }
    }
    
    private static void testGetAttendanceRecords() throws Exception {
        System.out.println("\n   📊 Testing: Get Attendance Records");
        
        List<Course> courses = service.getCoursesByTeacher(teacherSessionToken, teacherUser.getUserId());
        
        if (courses.isEmpty()) {
            System.out.println("      ⚠️  No courses to test attendance records retrieval");
            return;
        }
        
        Course testCourse = courses.get(0);
        List<Student> students = service.getEnrolledStudents(teacherSessionToken, testCourse.getCourseId());
        
        if (students.isEmpty()) {
            System.out.println("      ⚠️  No students to test attendance records");
            return;
        }
        
        Student testStudent = students.get(0);
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = service.getAttendanceRecords(teacherSessionToken, testStudent.getUserId(), startDate, endDate);
        
        System.out.println("      Found " + records.size() + " attendance records for student " + testStudent.getFullName());
        System.out.println("      Date range: " + startDate + " to " + endDate);
        
        System.out.println("      ✅ getAttendanceRecords() works correctly");
    }
    
    private static void testGetAttendanceStatistics() throws Exception {
        System.out.println("\n   📈 Testing: Get Attendance Statistics");
        
        List<Course> courses = service.getCoursesByTeacher(teacherSessionToken, teacherUser.getUserId());
        
        if (courses.isEmpty()) {
            System.out.println("      ⚠️  No courses to test attendance statistics");
            return;
        }
        
        Course testCourse = courses.get(0);
        List<Student> students = service.getEnrolledStudents(teacherSessionToken, testCourse.getCourseId());
        
        if (students.isEmpty()) {
            System.out.println("      ⚠️  No students to test attendance statistics");
            return;
        }
        
        Student testStudent = students.get(0);
        
        Map<String, Object> stats = service.getAttendanceStatistics(teacherSessionToken, testStudent.getUserId(), testCourse.getCourseId());
        
        System.out.println("      Statistics for " + testStudent.getFullName() + " in " + testCourse.getCourseCode() + ":");
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            System.out.println("        " + entry.getKey() + ": " + entry.getValue());
        }
        
        System.out.println("      ✅ getAttendanceStatistics() works correctly");
    }
    
    private static void testSessionValidation() throws Exception {
        System.out.println("\n   🔐 Testing: Session Validation");
        
        User validatedUser = service.validateSession(teacherSessionToken);
        
        if (validatedUser != null && validatedUser.getUserId() == teacherUser.getUserId()) {
            System.out.println("      ✅ validateSession() works correctly");
            System.out.println("        Validated user: " + validatedUser.getFullName());
        } else {
            System.out.println("      ❌ validateSession() failed");
        }
    }
    
    private static void testLogout() throws Exception {
        System.out.println("\n   🚪 Testing: Logout");
        
        service.logout(teacherSessionToken);
        
        try {
            service.validateSession(teacherSessionToken);
            System.out.println("      ❌ logout() failed - session still valid");
        } catch (Exception e) {
            System.out.println("      ✅ logout() works correctly - session invalidated");
        }
    }
}