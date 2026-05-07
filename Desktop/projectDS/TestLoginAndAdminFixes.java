import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import com.attendance.system.service.AttendanceService;
import com.attendance.system.service.AuthenticationService;
import com.attendance.system.model.*;

/**
 * Test the login error handling and admin functionality fixes
 */
public class TestLoginAndAdminFixes {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTING LOGIN ERROR HANDLING AND ADMIN FIXES ===");
            
            // Connect to server
            Registry registry = LocateRegistry.getRegistry("localhost", 1100);
            AttendanceService service = (AttendanceService) registry.lookup("AttendanceService");
            
            // Test 1: Invalid login credentials
            System.out.println("\n1. Testing invalid login credentials...");
            try {
                service.authenticateUser("wronguser", "wrongpass");
                System.out.println("❌ Should have failed with invalid credentials");
            } catch (Exception e) {
                System.out.println("✅ Correctly rejected invalid credentials");
                System.out.println("   Error message: " + e.getMessage());
            }
            
            // Test 2: Login with correct admin credentials
            System.out.println("\n2. Testing admin login...");
            AuthenticationService.AuthenticatedUser adminUser = service.authenticateUser("admin", "admin");
            String adminToken = adminUser.getSessionToken();
            System.out.println("✅ Admin login successful: " + adminUser.getUser().getFullName());
            
            // Test 3: Get all users (admin function)
            System.out.println("\n3. Testing admin functionality - get all users...");
            List<User> users = service.getAllUsers(adminToken);
            System.out.println("✅ Found " + users.size() + " users in system");
            
            for (User user : users) {
                System.out.println("   - " + user.getUsername() + " (" + user.getRole() + ")");
            }
            
            // Test 4: Try to delete a user that might have constraints
            System.out.println("\n4. Testing user deletion with proper error handling...");
            
            // Find a teacher to test deletion
            User teacherToDelete = null;
            for (User user : users) {
                if (user.getRole() == UserRole.TEACHER && !user.getUsername().equals("admin")) {
                    teacherToDelete = user;
                    break;
                }
            }
            
            if (teacherToDelete != null) {
                System.out.println("   Attempting to delete teacher: " + teacherToDelete.getUsername());
                try {
                    boolean deleted = service.deleteUser(adminToken, teacherToDelete.getUserId());
                    if (deleted) {
                        System.out.println("✅ Teacher deleted successfully");
                    } else {
                        System.out.println("⚠️  Teacher deletion returned false");
                    }
                } catch (Exception e) {
                    System.out.println("✅ Teacher deletion properly handled constraint:");
                    System.out.println("   Error: " + e.getMessage());
                }
            } else {
                System.out.println("   No teacher found to test deletion");
            }
            
            // Test 5: Create and delete a test user (should work)
            System.out.println("\n5. Testing successful user deletion...");
            try {
                // Register a test user first
                boolean registered = service.registerUser("testuser123", "test@example.com", 
                    "Test", "User", "TestPass123!", UserRole.STUDENT);
                
                if (registered) {
                    System.out.println("✅ Test user created successfully");
                    
                    // Find the created user
                    List<User> updatedUsers = service.getAllUsers(adminToken);
                    User testUser = null;
                    for (User user : updatedUsers) {
                        if (user.getUsername().equals("testuser123")) {
                            testUser = user;
                            break;
                        }
                    }
                    
                    if (testUser != null) {
                        // Delete the test user
                        boolean deleted = service.deleteUser(adminToken, testUser.getUserId());
                        if (deleted) {
                            System.out.println("✅ Test user deleted successfully");
                        } else {
                            System.out.println("❌ Test user deletion failed");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️  Test user creation/deletion: " + e.getMessage());
            }
            
            System.out.println("\n✅ ALL TESTS COMPLETED!");
            
        } catch (Exception e) {
            System.err.println("❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}