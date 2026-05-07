import com.attendance.system.service.AttendanceService;
import com.attendance.system.model.UserRole;
import java.rmi.Naming;

public class TestRegistration {
    public static void main(String[] args) {
        try {
            // Connect to the RMI server
            AttendanceService service = (AttendanceService) Naming.lookup("rmi://localhost:1100/AttendanceService");
            System.out.println("Connected to server successfully");
            
            // Test student registration
            System.out.println("\nTesting STUDENT registration...");
            boolean studentResult = service.registerUser(
                "teststudent", 
                "student@test.com", 
                "Test", 
                "Student", 
                "Password123!", 
                UserRole.STUDENT
            );
            System.out.println("Student registration result: " + studentResult);
            
            // Test teacher registration
            System.out.println("\nTesting TEACHER registration...");
            boolean teacherResult = service.registerUser(
                "testteacher", 
                "teacher@test.com", 
                "Test", 
                "Teacher", 
                "Password123!", 
                UserRole.TEACHER
            );
            System.out.println("Teacher registration result: " + teacherResult);
            
            // Test admin registration (should fail)
            System.out.println("\nTesting ADMIN registration (should fail)...");
            try {
                boolean adminResult = service.registerUser(
                    "testadmin", 
                    "admin@test.com", 
                    "Test", 
                    "Admin", 
                    "Password123!", 
                    UserRole.ADMIN
                );
                System.out.println("Admin registration result: " + adminResult);
            } catch (Exception e) {
                System.out.println("Admin registration failed as expected: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Registration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}