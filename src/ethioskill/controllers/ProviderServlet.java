package ethioskill.controllers;

import ethioskill.models.User;
import ethioskill.models.Booking;
import ethioskill.database.DBConnection;
import ethioskill.database.DatabaseHelper;
import ethioskill.utils.AlertUtil;
import ethioskill.utils.Validator;
import com.google.gson.Gson;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/ProviderServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class ProviderServlet extends HttpServlet {
    
    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            AlertUtil.sendError(response, "Authentication required");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (!"Provider".equals(user.getRole())) {
            AlertUtil.sendError(response, "Access denied");
            return;
        }
        
        String action = request.getParameter("action");
        if (action == null) action = "";
        
        switch (action) {
            case "getSkills":
                getSkills(response);
                break;
            case "getCities":
                getCities(response);
                break;
            case "getProviderDetails":
                getProviderDetails(user.getId(), response);
                break;
            case "getBookings":
                getBookings(user.getId(), response);
                break;
            case "checkNotifications":
                checkNotifications(user.getId(), response);
                break;
            default:
                AlertUtil.sendError(response, "Invalid action");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            AlertUtil.sendError(response, "Authentication required");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (!"Provider".equals(user.getRole())) {
            AlertUtil.sendError(response, "Access denied");
            return;
        }
        
        String action = request.getParameter("action");
        
        // Handle multipart requests (File Uploads)
        if (request.getContentType() != null && 
            request.getContentType().toLowerCase().startsWith("multipart/form-data")) {
            
            if ("registerProvider".equals(action)) {
                handleProviderRegistration(request, response, user.getId());
                return;
            } else if ("updateProfile".equals(action)) {
                updateProfile(request, response, user);
                return;
            } else if ("updateService".equals(action)) {
                 updateService(request, response, user.getId());
                 return;
            }
        }
        
        // Handle standard requests
        switch (action != null ? action : "") {
            case "updateStatus":
                updateStatus(request, response, user.getId());
                break;
            case "acceptBooking":
                acceptBooking(request, response, user.getId());
                break;
            case "updateService":
                updateService(request, response, user.getId());
                break;
            default:
                AlertUtil.sendError(response, "Invalid action");
        }
    }
    
    // ================== GET METHODS ==================
    
    private void getSkills(HttpServletResponse response) throws IOException {
        List<String> skills = DatabaseHelper.getSkills();
        AlertUtil.sendSuccess(response, skills);
    }
    
    private void getCities(HttpServletResponse response) throws IOException {
        List<String> cities = DatabaseHelper.getCities();
        AlertUtil.sendSuccess(response, cities);
    }
    
    private void getProviderDetails(int providerId, HttpServletResponse response) throws IOException {
        Map<String, Object> details = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM provider_details WHERE provider_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, providerId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                details.put("skill", rs.getString("primary_skill"));
                details.put("city", rs.getString("city"));
                details.put("paymentType", rs.getString("payment_type"));
                details.put("description", rs.getString("description"));
                details.put("photoPath", rs.getString("photo_path"));
                details.put("availability", rs.getString("availability_status"));
                AlertUtil.sendSuccess(response, details);
            } else {
                AlertUtil.sendError(response, "Provider details not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error");
        }
    }
    
    private void getBookings(int providerId, HttpServletResponse response) throws IOException {
        List<Booking> bookings = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM bookings WHERE provider_id = ? ORDER BY service_date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, providerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("booking_id"));
                booking.setSeekerName(rs.getString("seeker_name"));
                booking.setPhone(rs.getString("seeker_phone"));
                booking.setAddress(rs.getString("service_address"));
                booking.setDate(rs.getDate("service_date"));
                booking.setDetails(rs.getString("service_details"));
                booking.setStatus(rs.getString("status"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error");
            return;
        }
        AlertUtil.sendSuccess(response, bookings);
    }
    
    private void checkNotifications(int userId, HttpServletResponse response) throws IOException {
        List<String> notifications = DatabaseHelper.getUnreadNotifications(userId);
        AlertUtil.sendSuccess(response, notifications);
    }
    
    // ================== POST METHODS ==================
    
    private void handleProviderRegistration(HttpServletRequest request, HttpServletResponse response, int userId) 
            throws IOException, ServletException {
        
        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT * FROM provider_details WHERE provider_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            
            if (checkStmt.executeQuery().next()) {
                AlertUtil.sendError(response, "Provider already registered");
                return;
            }
            
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");
            String city = request.getParameter("city");
            String skill = request.getParameter("skill");
            String paymentType = request.getParameter("paymentType");
            String description = request.getParameter("description");
            
            if (fullName == null || phone == null || city == null || skill == null || 
                paymentType == null || description == null) {
                AlertUtil.sendError(response, "All fields are required");
                return;
            }
            
            String photoPath = "default";
            Part filePart = request.getPart("photo");
            
            if (filePart != null && filePart.getSize() > 0) {
                String uploadedPath = saveUploadedFile(filePart, userId);
                if (uploadedPath != null) {
                    photoPath = uploadedPath;
                } else {
                    AlertUtil.sendError(response, "Failed to save photo. Check file type/size.");
                    return;
                }
            }
            
            String insertSql = "INSERT INTO provider_details " +
                             "(provider_id, city, primary_skill, payment_type, description, photo_path, availability_status) " +
                             "VALUES (?, ?, ?, ?, ?, ?, 'Available')";
            
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, city);
            insertStmt.setString(3, skill);
            insertStmt.setString(4, paymentType);
            insertStmt.setString(5, description);
            insertStmt.setString(6, photoPath);
            
            int rows = insertStmt.executeUpdate();
            
            if (rows > 0) {
                if (!phone.equals(request.getSession().getAttribute("userPhone"))) {
                    String updateSql = "UPDATE users SET phone_number = ?, full_name = ? WHERE user_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, phone);
                    updateStmt.setString(2, fullName);
                    updateStmt.setInt(3, userId);
                    updateStmt.executeUpdate();
                    
                    User user = (User) request.getSession().getAttribute("user");
                    user.setPhone(phone);
                    user.setFullName(fullName);
                }
                
                boolean autoVerify = DatabaseHelper.isAutoVerifyEnabled();
                String status = autoVerify ? "Verified" : "Pending";
                
                if (autoVerify) {
                    String updateStatusSql = "UPDATE users SET status = 'Verified' WHERE user_id = ?";
                    PreparedStatement statusStmt = conn.prepareStatement(updateStatusSql);
                    statusStmt.setInt(1, userId);
                    statusStmt.executeUpdate();
                }
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Registration submitted successfully!");
                responseData.put("status", status);
                responseData.put("photoPath", photoPath);
                
                if (!autoVerify) {
                    String adminMsg = String.format("New provider registration pending: %s (%s)", fullName, skill);
                    notifyAdmins(conn, adminMsg);
                }
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new Gson().toJson(responseData));
            } else {
                AlertUtil.sendError(response, "Registration failed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error occurred");
        }
    }
    
    private void updateProfile(HttpServletRequest request, HttpServletResponse response, User user) 
            throws IOException, ServletException {
        
        try (Connection conn = DBConnection.getConnection()) {
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");
            String city = request.getParameter("city");
            String paymentType = request.getParameter("paymentType");
            
            if (fullName == null || phone == null || city == null || paymentType == null) {
                AlertUtil.sendError(response, "All fields are required");
                return;
            }
            
            if (!phone.equals(user.getPhone())) {
                String checkSql = "SELECT COUNT(*) FROM users WHERE phone_number = ? AND user_id != ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, phone);
                checkStmt.setInt(2, user.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    AlertUtil.sendError(response, "Phone number already in use");
                    return;
                }
            }
            
            String relativeWebPath = null;
            Part filePart = request.getPart("photo");
            if (filePart != null && filePart.getSize() > 0) {
                relativeWebPath = saveUploadedFile(filePart, user.getId());
                if (relativeWebPath == null) {
                    AlertUtil.sendError(response, "Invalid image file");
                    return;
                }
            }
            
            String updateUserSql = "UPDATE users SET full_name = ?, phone_number = ? WHERE user_id = ?";
            PreparedStatement userStmt = conn.prepareStatement(updateUserSql);
            userStmt.setString(1, fullName.trim());
            userStmt.setString(2, phone);
            userStmt.setInt(3, user.getId());
            userStmt.executeUpdate();
            
            StringBuilder updateProviderSql = new StringBuilder("UPDATE provider_details SET city = ?, payment_type = ?");
            List<Object> params = new ArrayList<>();
            params.add(city);
            params.add(paymentType);
            
            if (relativeWebPath != null) {
                updateProviderSql.append(", photo_path = ?");
                params.add(relativeWebPath);
            }
            
            updateProviderSql.append(" WHERE provider_id = ?");
            params.add(user.getId());
            
            PreparedStatement providerStmt = conn.prepareStatement(updateProviderSql.toString());
            for (int i = 0; i < params.size(); i++) {
                providerStmt.setObject(i + 1, params.get(i));
            }
            providerStmt.executeUpdate();
            
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setCity(city);
            user.setPaymentType(paymentType);
            if (relativeWebPath != null) {
                user.setPhotoPath(relativeWebPath);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Profile updated successfully");
            if (relativeWebPath != null) {
                responseData.put("photoPath", relativeWebPath);
            }
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new Gson().toJson(responseData));
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error occurred");
        }
    }
    
    // ================== HELPER METHODS ==================
    
    private String saveUploadedFile(Part part, int userId) throws IOException {
        String fileName = getFileName(part);
        if (fileName.isEmpty()) return null;
        
        String contentType = part.getContentType();
        if (!isValidImageType(contentType)) return null;
        
        // This regex was the fix for your previous error
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        String uniqueFileName = userId + "_" + fileName;
        String filePath = uploadPath + File.separator + uniqueFileName;
        part.write(filePath);
        
        return UPLOAD_DIR + "/" + uniqueFileName;
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response, int providerId) throws IOException {
        String status = request.getParameter("status");
        if (status == null || (!"Available".equals(status) && !"Rest".equals(status))) {
            AlertUtil.sendError(response, "Invalid status");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE provider_details SET availability_status = ? WHERE provider_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, providerId);
            stmt.executeUpdate();
            AlertUtil.sendSuccess(response, "Status updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error");
        }
    }

    private void acceptBooking(HttpServletRequest request, HttpServletResponse response, int providerId) throws IOException {
        try {
            int bookingId = Integer.parseInt(request.getParameter("bookingId"));
            try (Connection conn = DBConnection.getConnection()) {
                String updateSql = "UPDATE bookings SET status = 'Accepted' WHERE booking_id = ? AND provider_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, bookingId);
                updateStmt.setInt(2, providerId);
                
                if (updateStmt.executeUpdate() > 0) {
                    notifySeeker(conn, bookingId, providerId);
                    AlertUtil.sendSuccess(response, "Booking accepted successfully");
                } else {
                    AlertUtil.sendError(response, "Failed to accept booking");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.sendError(response, "Database error");
            }
        } catch (NumberFormatException e) {
            AlertUtil.sendError(response, "Invalid booking ID");
        }
    }

    private void updateService(HttpServletRequest request, HttpServletResponse response, int providerId) throws IOException {
        String skill = request.getParameter("skill");
        String paymentType = request.getParameter("paymentType");
        String description = request.getParameter("description");
        
        if (skill == null || paymentType == null) {
            AlertUtil.sendError(response, "Skill and payment type are required");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE provider_details SET primary_skill = ?, payment_type = ?, description = ? WHERE provider_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, skill);
            stmt.setString(2, paymentType);
            stmt.setString(3, description != null ? description : "");
            stmt.setInt(4, providerId);
            
            if (stmt.executeUpdate() > 0) {
                AlertUtil.sendSuccess(response, "Service updated successfully");
            } else {
                AlertUtil.sendError(response, "Failed to update service");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error");
        }
    }
    
    private void notifyAdmins(Connection conn, String message) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE role = 'Admin'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DatabaseHelper.sendNotification(rs.getInt("user_id"), message);
            }
        }
    }

    private void notifySeeker(Connection conn, int bookingId, int providerId) throws SQLException {
        String sql = "SELECT seeker_id FROM bookings WHERE booking_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String providerName = getProviderName(conn, providerId);
                    String message = "Your booking with " + providerName + " has been ACCEPTED!";
                    DatabaseHelper.sendNotification(rs.getInt("seeker_id"), message);
                }
            }
        }
    }

    private String getProviderName(Connection conn, int providerId) throws SQLException {
        String sql = "SELECT full_name FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, providerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("full_name") : "Provider";
            }
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
    
    private boolean isValidImageType(String contentType) {
        return "image/jpeg".equalsIgnoreCase(contentType) ||
               "image/jpg".equalsIgnoreCase(contentType) ||
               "image/png".equalsIgnoreCase(contentType);
    }
}