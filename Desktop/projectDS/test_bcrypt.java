import at.favre.lib.crypto.bcrypt.BCrypt;

public class test_bcrypt {
    public static void main(String[] args) {
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G";
        
        // Test common passwords
        String[] passwords = {"admin", "Admin@123", "secret", "password", "123456"};
        
        for (String password : passwords) {
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
            System.out.println("Password '" + password + "': " + result.verified);
        }
    }
}