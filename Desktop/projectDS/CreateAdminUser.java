import at.favre.lib.crypto.bcrypt.BCrypt;

public class CreateAdminUser {
    public static void main(String[] args) {
        String password = "admin";
        String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Verify the hash works
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        System.out.println("Verification: " + result.verified);
    }
}