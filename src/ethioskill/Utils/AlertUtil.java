package ethioskill.utils;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AlertUtil {
    
    private static final Gson gson = new Gson();
    
    /**
     * Sanitizes output to prevent XSS
     */
    private static String sanitizeOutput(String input) {
        if (input == null) return null;
        return input.replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("&", "&amp;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;");
    }
    
    public static void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(
            new Response(false, sanitizeOutput(message))
        ));
    }
    
    public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(
            new Response(true, sanitizeOutput(message))
        ));
    }
    
    public static void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(
            new DataResponse(true, "Success", data)
        ));
    }
    
    // Response classes
    public static class Response {
        private boolean success;
        private String message;
        
        public Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    public static class DataResponse extends Response {
        private Object data;
        
        public DataResponse(boolean success, String message, Object data) {
            super(success, message);
            this.data = data;
        }
        
        public Object getData() { return data; }
    }
}