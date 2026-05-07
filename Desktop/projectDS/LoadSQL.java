import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoadSQL {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/attendance_system?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "attendance_user";
        String pass = "attendance_pass";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/schema.sql")));
            String[] statements = sql.split(";");
            for (String statement : statements) {
                if (statement.trim().length() > 0) {
                    stmt.execute(statement.trim());
                }
            }
            System.out.println("Schema loaded successfully.");
            
            // Also load sample data
            try {
                String dataSql = new String(Files.readAllBytes(Paths.get("scripts/database/sample-data.sql")));
                String[] dataStatements = dataSql.split(";");
                for (String statement : dataStatements) {
                    if (statement.trim().length() > 0) {
                        stmt.execute(statement.trim());
                    }
                }
                System.out.println("Sample data loaded successfully.");
            } catch (Exception e) {
                System.out.println("No sample data or failed to load: " + e.getMessage());
            }
        }
    }
}
