import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.model.*;
import java.rmi.Naming;
import java.util.List;

public class DebugTeacherPermission {
    public static void main(String[] args) {
        try {
            // Connect to the RMI server
            AttendanceService service = (AttendanceService) Naming.lookup("rmi://localhost:1100/AttendanceService");
            System.out.println("Connected to server successfully");
            
            // Login as teacher
            AuthenticationService.AuthenticatedUser teacherUser = service.authenticateUser("testteacher", "Password123!");
            String teacherToken = teacherUser.getSessionToken();
            System.out.println("Teacher logged in successfully");
            System.out.println("Teacher user ID: " + teacherUser.getUser().getUserId());
            
            // Get teacher's courses
            List<Course> courses = service.getCoursesByTeacher(teacherToken, teacherUser.getUser().getUserId());
            System.out.println("Teacher has " + courses.size() + " courses:");
            
            for (Course course : courses) {
                System.out.println("Course ID: " + course.getCourseId());
                System.out.println("Course Code: " + course.getCourseCode());
                System.out.println("Course Teacher ID: " + course.getTeacherId());
                System.out.println("Teacher User ID: " + teacherUser.getUser().getUserId());
                System.out.println("IDs match: " + (course.getTeacherId() == teacherUser.getUser().getUserId()));
            }
            
        } catch (Exception e) {
            System.err.println("Debug failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}