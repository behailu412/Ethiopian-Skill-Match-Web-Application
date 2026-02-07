package ethioskill.controllers;

import ethioskill.models.User;
import ethioskill.database.DBConnection;
import ethioskill.database.DatabaseHelper;
import ethioskill.utils.AlertUtil;
import ethioskill.utils.Validator;
import ethioskill.utils.PasswordUtil;
import com.google.gson.Gson;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if ("signup".equals(action)) {
            handleSignup(request, response);
        } else {
            AlertUtil.sendError(response, "Invalid action");
        }
    }
    
    private void handleSignup(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String accountType = request.getParameter("accountType");
        
        // Validation
        if (fullName == null || phone == null || password == null || accountType == null ||
            fullName.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
            AlertUtil.sendError(response, "All fields are required");
            return;
        }
        
        if (!Validator.isValidPhone(phone)) {
            AlertUtil.sendError(response, "Invalid phone number format. Use 09XXXXXXXX or +251XXXXXXXXX");
            return;
        }
        
        if (!Validator.isValidPassword(password)) {
            AlertUtil.sendError(response, 
                "Password must be at least 6 characters with uppercase, lowercase, and number");
            return;
        }
        
        // Format phone
        phone = Validator.formatPhone(phone);
        
        try (Connection conn = DBConnection.getConnection()) {
            // Check if phone already exists
            if (phoneExists(conn, phone)) {
                AlertUtil.sendError(response, "This phone number is already registered");
                return;
            }
            
            // Determine status based on role
            String status = "Seeker".equals(accountType) ? "Verified" : "Pending";
            
            // Hash the password before storing
            String hashedPassword = PasswordUtil.hashPassword(password);
            
            // Insert user
            String sql = "INSERT INTO users (full_name, phone_number, password, role, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, fullName.trim());
            stmt.setString(2, phone);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, accountType);
            stmt.setString(5, status);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get generated user ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int userId = 0;
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                }
                
                // If provider and auto-verify is enabled, update status
                if ("Provider".equals(accountType) && DatabaseHelper.isAutoVerifyEnabled()) {
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE users SET status = 'Verified' WHERE user_id = ?"
                    );
                    updateStmt.setInt(1, userId);
                    updateStmt.executeUpdate();
                    status = "Verified";
                }
                
                // Prepare response
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Account created successfully!");
                responseData.put("userId", userId);
                responseData.put("status", status);
                
                // Send welcome notification for providers
                if ("Provider".equals(accountType)) {
                    String welcomeMsg = "Welcome to Ethiopian Skill Match System! ";
                    if ("Pending".equals(status)) {
                        welcomeMsg += "Your account is pending admin approval.";
                    } else {
                        welcomeMsg += "Your account has been auto-verified. Please complete your profile.";
                    }
                    DatabaseHelper.sendNotification(userId, welcomeMsg);
                }
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new Gson().toJson(responseData));
                
            } else {
                AlertUtil.sendError(response, "Registration failed. Please try again.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error occurred");
        }
    }
    
    private boolean phoneExists(Connection conn, String phone) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE phone_number = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, phone);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}