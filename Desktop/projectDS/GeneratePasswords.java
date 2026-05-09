import org.mindrot.jbcrypt.BCrypt;

public class GeneratePasswords {
    public static void main(String[] args) {
        System.out.println("Generating BCrypt password hashes...\n");
        
        // Admin password: Admin@123
        String adminPassword = "Admin@123";
        String adminHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt(10));
        System.out.println("Admin Password: " + adminPassword);
        System.out.println("Admin Hash: " + adminHash);
        System.out.println();
        
        // Teacher password: Teacher@123
        String teacherPassword = "Teacher@123";
        String teacherHash = BCrypt.hashpw(teacherPassword, BCrypt.gensalt(10));
        System.out.println("Teacher Password: " + teacherPassword);
        System.out.println("Teacher Hash: " + teacherHash);
        System.out.println();
        
        // Student password: Student@123
        String studentPassword = "Student@123";
        String studentHash = BCrypt.hashpw(studentPassword, BCrypt.gensalt(10));
        System.out.println("Student Password: " + studentPassword);
        System.out.println("Student Hash: " + studentHash);
        System.out.println();
        
        // Verify the hashes work
        System.out.println("Verification:");
        System.out.println("Admin verify: " + BCrypt.checkpw(adminPassword, adminHash));
        System.out.println("Teacher verify: " + BCrypt.checkpw(teacherPassword, teacherHash));
        System.out.println("Student verify: " + BCrypt.checkpw(studentPassword, studentHash));
    }
}
