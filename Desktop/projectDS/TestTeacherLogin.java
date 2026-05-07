import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.model.*;
import java.rmi.Naming;
import java.time.LocalDate;
import java.util.List;

public class TestTeacherLogin {
    public static void main(String[] args) {
        try {
            // Connect to the RMI server
            AttendanceService service = (AttendanceService) Naming.lookup("rmi://localhost:1100/AttendanceService");
            System.out.println("Connected to server successfully");
            
            // Login as teacher
            AuthenticationService.AuthenticatedUser teacherUser = service.authenticateUser("testteacher", "Password123!");
            String teacherToken = teacherUser.getSessionToken();
            System.out.println("Teacher logged in successfully: " + teacherUser.getUser().getUsername());
            System.out.println("Teacher role: " + teacherUser.getUser().getRole());
            
            // Get teacher's courses
            List<Course> courses = service.getCoursesByTeacher(teacherToken, teacherUser.getUser().getUserId());
            System.out.println("Teacher has " + courses.size() + " courses:");
            for (Course course : courses) {
                System.out.println("- " + course.getCourseCode() + ": " + course.getCourseName());
                
                // Get enrolled students for this course
                List<Student> students = service.getEnrolledStudents(teacherToken, course.getCourseId());
                System.out.println("  Enrolled students: " + students.size());
                for (Student student : students) {
                    System.out.println("    - " + student.getFullName() + " (" + student.getStudentNumber() + ")");
                }
                
                // Test attendance marking
                System.out.println("  Testing attendance marking for today...");
                for (Student student : students) {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setStudentId(student.getUserId());
                    record.setCourseId(course.getCourseId());
                    record.setAttendanceDate(LocalDate.now());
                    record.setClassTime(java.time.LocalTime.of(10, 0)); // 10:00 AM
                    record.setStatus(AttendanceStatus.PRESENT);
                    record.setRemarks("Test attendance");
                    record.setMarkedBy(teacherUser.getUser().getUserId());
                    
                    boolean marked = service.markAttendance(teacherToken, record);
                    System.out.println("    Attendance marked for " + student.getFullName() + ": " + marked);
                }
                
                // Get attendance records for today
                List<AttendanceRecord> todayRecords = service.getAttendanceByClassDate(teacherToken, course.getCourseId(), LocalDate.now());
                System.out.println("  Today's attendance records: " + todayRecords.size());
            }
            
            System.out.println("Teacher functionality test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Teacher test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}