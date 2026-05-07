import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.model.*;
import java.rmi.Naming;
import java.time.LocalDate;

public class CreateSampleData {
    public static void main(String[] args) {
        try {
            // Connect to the RMI server
            AttendanceService service = (AttendanceService) Naming.lookup("rmi://localhost:1100/AttendanceService");
            System.out.println("Connected to server successfully");
            
            // Login as admin to create courses and enroll students
            AuthenticationService.AuthenticatedUser adminUser = service.authenticateUser("admin", "admin");
            String adminToken = adminUser.getSessionToken();
            System.out.println("Admin logged in successfully");
            
            // Create a sample course
            Course course = new Course();
            course.setCourseCode("CS101");
            course.setCourseName("Introduction to Computer Science");
            course.setDescription("Basic computer science concepts");
            course.setCredits(3);
            course.setTeacherId(9); // testteacher user ID
            course.setSemester("Fall");
            course.setAcademicYear("2024-2025");
            course.setActive(true);
            
            boolean courseCreated = service.createCourse(adminToken, course);
            System.out.println("Course created: " + courseCreated);
            
            // Get the created course ID by fetching all courses
            var courses = service.getAllActiveCourses(adminToken);
            Course createdCourse = null;
            for (Course c : courses) {
                if ("CS101".equals(c.getCourseCode())) {
                    createdCourse = c;
                    break;
                }
            }
            
            if (createdCourse != null) {
                System.out.println("Found course: " + createdCourse.getCourseCode() + " with ID: " + createdCourse.getCourseId());
                
                // Enroll the test student in the course
                boolean enrolled = service.enrollStudent(adminToken, 8, createdCourse.getCourseId()); // teststudent user ID
                System.out.println("Student enrolled: " + enrolled);
                
                // Also enroll other students if they exist
                try {
                    service.enrollStudent(adminToken, 4, createdCourse.getCourseId()); // wolde
                    service.enrollStudent(adminToken, 6, createdCourse.getCourseId()); // zegeye
                    System.out.println("Additional students enrolled");
                } catch (Exception e) {
                    System.out.println("Some students may not exist or already enrolled: " + e.getMessage());
                }
            }
            
            System.out.println("Sample data creation completed!");
            
        } catch (Exception e) {
            System.err.println("Failed to create sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}