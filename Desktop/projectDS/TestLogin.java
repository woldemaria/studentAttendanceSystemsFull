import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import java.rmi.Naming;

public class TestLogin {
    public static void main(String[] args) {
        try {
            // Connect to the RMI server
            AttendanceService service = (AttendanceService) Naming.lookup("rmi://localhost:1100/AttendanceService");
            System.out.println("Connected to server successfully");
            
            // Test authentication with admin/admin
            System.out.println("Testing login with username: admin, password: admin");
            AuthenticationService.AuthenticatedUser user = service.authenticateUser("admin", "admin");
            
            System.out.println("Login successful!");
            System.out.println("User: " + user.getUser().getUsername());
            System.out.println("Role: " + user.getUser().getRole());
            System.out.println("Session Token: " + user.getSessionToken());
            
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}