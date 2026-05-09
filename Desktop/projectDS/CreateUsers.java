import com.attendance.system.util.SecurityUtil;
import java.sql.*;

public class CreateUsers {
    public static void main(String[] args) {
        try {
            System.out.println("===========================================");
            System.out.println("  Creating Default Users");
            System.out.println("===========================================\n");
            
            // Generate password hashes
            String adminHash = SecurityUtil.hashPassword("Admin@123");
            String teacherHash = SecurityUtil.hashPassword("Teacher@123");
            String studentHash = SecurityUtil.hashPassword("Student@123");
            
            System.out.println("Generated password hashes:");
            System.out.println("Admin@123   -> " + adminHash);
            System.out.println("Teacher@123 -> " + teacherHash);
            System.out.println("Student@123 -> " + studentHash);
            System.out.println();
            
            // Connect to database
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/Wolde";
            Connection conn = DriverManager.getConnection(url, "root", "");
            
            System.out.println("Connected to database: Wolde\n");
            
            // Delete existing users
            String deleteSql = "DELETE FROM USERS WHERE username IN ('admin', 'teacher1', 'student1')";
            Statement stmt = conn.createStatement();
            int deleted = stmt.executeUpdate(deleteSql);
            System.out.println("Deleted " + deleted + " existing users\n");
            
            // Create Admin
            String adminSql = "INSERT INTO USERS (username, password_hash, email, first_name, last_name, role, is_active, created_at) " +
                            "VALUES ('admin', ?, 'admin@attendance.system', 'System', 'Administrator', 'ADMIN', 1, NOW())";
            PreparedStatement pstmt = conn.prepareStatement(adminSql);
            pstmt.setString(1, adminHash);
            pstmt.executeUpdate();
            System.out.println("✓ Created admin user");
            
            // Create Teacher
            String teacherSql = "INSERT INTO USERS (username, password_hash, email, first_name, last_name, role, is_active, created_at) " +
                              "VALUES ('teacher1', ?, 'teacher1@attendance.system', 'John', 'Teacher', 'TEACHER', 1, NOW())";
            pstmt = conn.prepareStatement(teacherSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, teacherHash);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int teacherUserId = rs.getInt(1);
                String teacherRecordSql = "INSERT INTO TEACHERS (user_id, employee_id, department, specialization) " +
                                        "VALUES (?, 'EMP001', 'Computer Science', 'Software Engineering')";
                PreparedStatement pstmt2 = conn.prepareStatement(teacherRecordSql);
                pstmt2.setInt(1, teacherUserId);
                pstmt2.executeUpdate();
            }
            System.out.println("✓ Created teacher1 user");
            
            // Create Student
            String studentSql = "INSERT INTO USERS (username, password_hash, email, first_name, last_name, role, is_active, created_at) " +
                              "VALUES ('student1', ?, 'student1@attendance.system', 'Jane', 'Student', 'STUDENT', 1, NOW())";
            pstmt = conn.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, studentHash);
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int studentUserId = rs.getInt(1);
                String studentRecordSql = "INSERT INTO STUDENTS (user_id, student_number, program, year_level, class_section, enrollment_date) " +
                                        "VALUES (?, 'STU001', 'Computer Science', 1, 'A', NOW())";
                PreparedStatement pstmt2 = conn.prepareStatement(studentRecordSql);
                pstmt2.setInt(1, studentUserId);
                pstmt2.executeUpdate();
            }
            System.out.println("✓ Created student1 user");
            
            System.out.println("\n===========================================");
            System.out.println("  Users Created Successfully!");
            System.out.println("===========================================\n");
            
            System.out.println("Login Credentials:");
            System.out.println("  Admin:   username: admin    password: Admin@123");
            System.out.println("  Teacher: username: teacher1 password: Teacher@123");
            System.out.println("  Student: username: student1 password: Student@123");
            System.out.println();
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
