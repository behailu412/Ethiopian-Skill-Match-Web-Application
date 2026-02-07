package ethioskill.controllers;

import ethioskill.models.User;
import ethioskill.database.DBConnection;
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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if ("login".equals(action)) {
            handleLogin(request, response);
        } else {
            AlertUtil.sendError(response, "Invalid action");
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        
        // Validation
        if (phone == null || password == null || role == null ||
            phone.trim().isEmpty() || password.trim().isEmpty()) {
            AlertUtil.sendError(response, "All fields are required");
            return;
        }
        
        if (!Validator.isValidPhone(phone)) {
            AlertUtil.sendError(response, "Invalid phone number format");
            return;
        }
        
        // Format phone for database comparison
        phone = Validator.formatPhone(phone);
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE phone_number = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, phone);
            stmt.setString(2, role);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Verify the password against the stored hash
                String storedHash = rs.getString("password");
                if (!PasswordUtil.verifyPassword(password, storedHash)) {
                    AlertUtil.sendError(response, "Invalid credentials or role mismatch");
                    return;
                }
                // Create user object
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setPhone(rs.getString("phone_number"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                
                // Create session
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                
                // Prepare response
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("user", user);
                
                // Check if provider needs to complete registration
                if ("Provider".equals(role)) {
                    boolean hasDetails = checkProviderDetails(conn, user.getId());
                    responseData.put("needsRegistration", !hasDetails);
                }
                
                // Send response
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new Gson().toJson(responseData));
                
            } else {
                AlertUtil.sendError(response, "Invalid credentials or role mismatch");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error occurred");
        }
    }
    
    private boolean checkProviderDetails(Connection conn, int userId) throws SQLException {
        String sql = "SELECT * FROM provider_details WHERE provider_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        response.sendRedirect(request.getContextPath() + "/pages/login.html");
    }
}