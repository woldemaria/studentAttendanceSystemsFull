import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SetupDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS attendance_system;");
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS attendance_system_test;");
            // Drop user if exists to avoid errors on alter
            try { stmt.executeUpdate("DROP USER 'attendance_user'@'localhost';"); } catch (Exception e) {}
            stmt.executeUpdate("CREATE USER 'attendance_user'@'localhost' IDENTIFIED BY 'attendance_pass';");
            stmt.executeUpdate("GRANT ALL PRIVILEGES ON attendance_system.* TO 'attendance_user'@'localhost';");
            stmt.executeUpdate("GRANT ALL PRIVILEGES ON attendance_system_test.* TO 'attendance_user'@'localhost';");
            stmt.executeUpdate("FLUSH PRIVILEGES;");
            
            System.out.println("Database and user created successfully.");
        }
    }
}
