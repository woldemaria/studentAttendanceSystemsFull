import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.model.*;

/**
 * Test with sample teacher from database (john.smith)
 */
public class TestSampleTeacher {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTING SAMPLE TEACHER (john.smith) ===");
            
            // Connect to server
            Registry registry = LocateRegistry.getRegistry("localhost", 1100);
            AttendanceService service = (AttendanceService) registry.lookup("AttendanceService");
            
            // Login as john.smith (sample teacher)
            AuthenticationService.AuthenticatedUser authUser = service.authenticateUser("john.smith", "admin");
            String sessionToken = authUser.getSessionToken();
            User teacher = authUser.getUser();
            
            System.out.println("✅ Logged in as: " + teacher.getFullName() + " (ID: " + teacher.getUserId() + ")");
            
            // Test teacher activities
            List<Course> courses = service.getCoursesByTeacher(sessionToken, teacher.getUserId());
            System.out.println("📚 Found " + courses.size() + " courses:");
            
            for (Course course : courses) {
                System.out.println("   - " + course.getCourseCode() + ": " + course.getCourseName());
                
                // Get enrolled students
                List<Student> students = service.getEnrolledStudents(sessionToken, course.getCourseId());
                System.out.println("     👥 " + students.size() + " enrolled students:");
                
                for (Student student : students) {
                    System.out.println("       - " + student.getFullName() + " (" + student.getStudentNumber() + ")");
                }
                
                // Test attendance marking for first student if available
                if (!students.isEmpty()) {
                    Student testStudent = students.get(0);
                    AttendanceRecord record = new AttendanceRecord();
                    record.setStudentId(testStudent.getUserId());
                    record.setCourseId(course.getCourseId());
                    record.setAttendanceDate(LocalDate.now());
                    record.setClassTime(LocalTime.of(10, 0)); // 10:00 AM
                    record.setStatus(AttendanceStatus.PRESENT);
                    record.setMarkedBy(teacher.getUserId());
                    record.setRemarks("Sample teacher test");
                    
                    boolean success = service.markAttendance(sessionToken, record);
                    System.out.println("     ✏️  Attendance marking: " + (success ? "SUCCESS" : "FAILED"));
                }
                
                // Get today's attendance
                List<AttendanceRecord> todayRecords = service.getAttendanceByClassDate(sessionToken, course.getCourseId(), LocalDate.now());
                System.out.println("     📅 Today's attendance records: " + todayRecords.size());
            }
            
            System.out.println("\n✅ SAMPLE TEACHER TEST COMPLETED SUCCESSFULLY!");
            
        } catch (Exception e) {
            System.err.println("❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}