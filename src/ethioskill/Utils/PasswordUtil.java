package ethioskill.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    
    /**
     * Hashes a password with a random salt
     * @param password The plain text password
     * @return A string containing the salt and hashed password, separated by ':'
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password with the salt
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt);
            byte[] hashedPassword = digest.digest(password.getBytes());
            
            // Combine salt and hashed password
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashedPasswordBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            return saltBase64 + ":" + hashedPasswordBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies a password against a stored hash
     * @param password The plain text password to verify
     * @param storedHash The stored hash (salt:hashedPassword)
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            if (storedHash == null || password == null) {
                return false;
            }
            
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false; // Invalid format
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);
            
            // Hash the provided password with the stored salt
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt);
            byte[] hashedPassword = digest.digest(password.getBytes());
            
            // Compare the hashes
            return MessageDigest.isEqual(storedHashBytes, hashedPassword);
        } catch (Exception e) {
            return false; // In case of any error during verification
        }
    }
    
    /**
     * Checks if the stored hash format is valid
     * @param storedHash The hash to validate
     * @return true if the format is valid, false otherwise
     */
    public static boolean isValidHashFormat(String storedHash) {
        if (storedHash == null) {
            return false;
        }
        
        String[] parts = storedHash.split(":");
        return parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty();
    }
}