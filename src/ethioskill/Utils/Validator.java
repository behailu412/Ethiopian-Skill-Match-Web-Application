package ethioskill.utils;

import java.util.regex.Pattern;

public class Validator {
    
    // Prevent path traversal
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("\\.\\.\\\\\\|\\.\\.\\/|\\.\\./|\\.\\\\\\\\");
        
    // Prevent SQL injection patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("('|--|/\\\\*|\\\\*/|;|exec|drop|create|alter|delete|insert|select|union|script|<script|javascript:)", Pattern.CASE_INSENSITIVE);
        
    // Prevent XSS patterns
    private static final Pattern XSS_PATTERN = Pattern.compile("(<script|javascript:|vbscript:|onload|onerror|onmouseover|<iframe|<embed|<object|<form|document\\.cookie)", Pattern.CASE_INSENSITIVE);
        
    public static boolean containsPathTraversal(String input) {
        if (input == null) return false;
        return PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }
        
    public static boolean containsSqlInjection(String input) {
        if (input == null) return false;
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }
        
    public static boolean containsXss(String input) {
        if (input == null) return false;
        return XSS_PATTERN.matcher(input).find();
    }
        
    public static boolean isValidInput(String input) {
        if (input == null) return true; // Let other validations handle null
        return !containsPathTraversal(input) && 
               !containsSqlInjection(input) && 
               !containsXss(input);
    }
        
    public static String sanitizeInput(String input) {
        if (input == null) return null;
            
        // Basic sanitization - remove dangerous patterns
        input = input.replaceAll("<script", "&lt;script");
        input = input.replaceAll("</script>", "&lt;/script&gt;");
        input = input.replaceAll("javascript:", "javascript_");
        input = input.replaceAll("vbscript:", "vbscript_");
            
        return input.trim();
    }
    
    // Ethiopian phone number patterns
    private static final Pattern PHONE_PATTERN_09 = Pattern.compile("^09[0-9]{8}$");
    private static final Pattern PHONE_PATTERN_07 = Pattern.compile("^07[0-9]{8}$");
    private static final Pattern PHONE_PATTERN_INTL = Pattern.compile("^\\+251[0-9]{9}$");
    
    // Password pattern: at least 6 chars, 1 uppercase, 1 lowercase, 1 digit
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$");
    
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String trimmed = phone.trim();
        return PHONE_PATTERN_09.matcher(trimmed).matches() ||
               PHONE_PATTERN_07.matcher(trimmed).matches() ||
               PHONE_PATTERN_INTL.matcher(trimmed).matches();
    }
    
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    public static String formatPhone(String phone) {
        if (phone == null) return null;
        
        phone = phone.trim();
        
        // Convert international format to local
        if (phone.startsWith("+251")) {
            return "0" + phone.substring(4);
        }
        
        return phone;
    }
}