package ethioskill.controllers;

import ethioskill.models.User;
import ethioskill.database.DBConnection;
import ethioskill.database.DatabaseHelper;
import ethioskill.utils.AlertUtil;
import com.google.gson.Gson;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/SeekerServlet")
public class SeekerServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            AlertUtil.sendError(response, "Authentication required");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        if (!"Seeker".equals(user.getRole())) {
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
            case "getProviders":
                getProviders(request, response);
                break;
            case "getStats":
                getStats(response);
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
        if (!"Seeker".equals(user.getRole())) {
            AlertUtil.sendError(response, "Access denied");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("book".equals(action)) {
            handleBooking(request, response, user.getId());
        } else {
            AlertUtil.sendError(response, "Invalid action");
        }
    }
    
    private void getSkills(HttpServletResponse response) throws IOException {
        List<String> skills = DatabaseHelper.getSkills();
        AlertUtil.sendSuccess(response, skills);
    }
    
    private void getCities(HttpServletResponse response) throws IOException {
        List<String> cities = DatabaseHelper.getCities();
        AlertUtil.sendSuccess(response, cities);
    }
    
    private void getProviders(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String skill = request.getParameter("skill");
        String city = request.getParameter("city");
        String search = request.getParameter("search");
        
        List<User> providers = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT u.user_id, u.full_name, u.phone_number, u.status, " +
                "pd.city, pd.primary_skill, pd.payment_type, pd.description, " +
                "pd.availability_status, pd.photo_path " +
                "FROM users u " +
                "JOIN provider_details pd ON u.user_id = pd.provider_id " +
                "WHERE u.role = 'Provider' AND u.status = 'Verified'"
            );
            
            List<String> conditions = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            
            if (skill != null && !skill.isEmpty()) {
                conditions.add("pd.primary_skill = ?");
                params.add(skill);
            }
            
            if (city != null && !city.isEmpty()) {
                conditions.add("pd.city = ?");
                params.add(city);
            }
            
            if (search != null && !search.isEmpty()) {
                conditions.add("(u.full_name LIKE ? OR pd.primary_skill LIKE ?)");
                params.add("%" + search + "%");
                params.add("%" + search + "%");
            }
            
            if (!conditions.isEmpty()) {
                sql.append(" AND ").append(String.join(" AND ", conditions));
            }
            
            sql.append(" ORDER BY u.full_name");
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User provider = new User();
                provider.setId(rs.getInt("user_id"));
                provider.setFullName(rs.getString("full_name"));
                provider.setPhone(rs.getString("phone_number"));
                provider.setCity(rs.getString("city"));
                provider.setSkill(rs.getString("primary_skill"));
                provider.setPaymentType(rs.getString("payment_type"));
                provider.setDescription(rs.getString("description"));
                provider.setAvailability(rs.getString("availability_status"));
                provider.setPhotoPath(rs.getString("photo_path"));
                provider.setStatus(rs.getString("status"));
                
                providers.add(provider);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.sendError(response, "Database error");
            return;
        }
        
        AlertUtil.sendSuccess(response, providers);
    }
    
    private void getStats(HttpServletResponse response) throws IOException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql1 = "SELECT COUNT(*) FROM users WHERE role = 'Provider' AND status = 'Verified'";
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(sql1);
            if (rs1.next()) {
                stats.put("totalProviders", rs1.getInt(1));
            }
            
            String sql2 = "SELECT COUNT(DISTINCT pd.city) FROM provider_details pd " +
                         "JOIN users u ON pd.provider_id = u.user_id WHERE u.status = 'Verified'";
            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery(sql2);
            if (rs2.next()) {
                stats.put("citiesCount", rs2.getInt(1));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        AlertUtil.sendSuccess(response, stats);
    }
    
    private void checkNotifications(int userId, HttpServletResponse response) throws IOException {
        List<String> notifications = DatabaseHelper.getUnreadNotifications(userId);
        AlertUtil.sendSuccess(response, notifications);
    }
    
    private void handleBooking(HttpServletRequest request, HttpServletResponse response, int seekerId) 
            throws IOException {
        
        try {
            int providerId = Integer.parseInt(request.getParameter("providerId"));
            String seekerName = request.getParameter("seekerName");
            String seekerPhone = request.getParameter("seekerPhone");
            String serviceAddress = request.getParameter("serviceAddress");
            String serviceDate = request.getParameter("serviceDate");
            String serviceDetails = request.getParameter("serviceDetails");
            
            if (seekerName == null || seekerPhone == null || serviceAddress == null || 
                serviceDate == null || serviceDetails == null) {
                AlertUtil.sendError(response, "All fields are required");
                return;
            }
            
            LocalDate date = LocalDate.parse(serviceDate);
            
            try (Connection conn = DBConnection.getConnection()) {
                String checkSql = "SELECT COUNT(*) FROM bookings " +
                                 "WHERE provider_id = ? AND service_date = ? AND status != 'Cancelled'";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, providerId);
                // FIX: Use java.sql.Date explicitly
                checkStmt.setDate(2, java.sql.Date.valueOf(date));
                
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    AlertUtil.sendError(response, "Provider is already booked on this date");
                    return;
                }
                
                String insertSql = "INSERT INTO bookings " +
                                 "(seeker_id, provider_id, seeker_name, seeker_phone, " +
                                 "service_address, service_date, service_details, status) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending')";
                
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, seekerId);
                insertStmt.setInt(2, providerId);
                insertStmt.setString(3, seekerName.trim());
                insertStmt.setString(4, seekerPhone.trim());
                insertStmt.setString(5, serviceAddress.trim());
                // FIX: Use java.sql.Date explicitly
                insertStmt.setDate(6, java.sql.Date.valueOf(date));
                insertStmt.setString(7, serviceDetails.trim());
                
                int rows = insertStmt.executeUpdate();
                
                if (rows > 0) {
                    String providerName = getProviderName(conn, providerId);
                    String message = String.format(
                        "New booking request from %s for %s",
                        seekerName, date.toString()
                    );
                    DatabaseHelper.sendNotification(providerId, message);
                    
                    AlertUtil.sendSuccess(response, "Booking request sent successfully!");
                } else {
                    AlertUtil.sendError(response, "Failed to create booking");
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.sendError(response, "Database error occurred");
            }
            
        } catch (NumberFormatException e) {
            AlertUtil.sendError(response, "Invalid provider ID");
        }
    }
    
    private String getProviderName(Connection conn, int providerId) throws SQLException {
        String sql = "SELECT full_name FROM users WHERE user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, providerId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getString("full_name") : "Provider";
    }
}