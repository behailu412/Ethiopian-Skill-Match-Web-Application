package ethioskill.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    
    public static boolean isAutoVerifyEnabled() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT setting_value FROM settings WHERE setting_key = 'auto_verify'"
            );
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            System.err.println("Database error in isAutoVerifyEnabled: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public static List<String> getCities() {
        List<String> cities = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT city_name FROM cities ORDER BY city_name");
            while (rs.next()) {
                cities.add(rs.getString("city_name"));
            }
        } catch (SQLException e) {
            System.err.println("Database error in getCities: " + e.getMessage());
            e.printStackTrace();
        }
        return cities;
    }
    
    public static List<String> getSkills() {
        List<String> skills = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT skill_name FROM skills ORDER BY skill_name");
            while (rs.next()) {
                skills.add(rs.getString("skill_name"));
            }
        } catch (SQLException e) {
            System.err.println("Database error in getSkills: " + e.getMessage());
            e.printStackTrace();
        }
        return skills;
    }
    
    public static boolean addCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            System.err.println("Cannot add city: city name is null or empty");
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO cities (city_name) VALUES (?)"
            );
            stmt.setString(1, cityName.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error in addCity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean deleteCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            System.err.println("Cannot delete city: city name is null or empty");
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM cities WHERE city_name = ?"
            );
            stmt.setString(1, cityName.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error in deleteCity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean addSkill(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            System.err.println("Cannot add skill: skill name is null or empty");
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO skills (skill_name) VALUES (?)"
            );
            stmt.setString(1, skillName.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error in addSkill: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean deleteSkill(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            System.err.println("Cannot delete skill: skill name is null or empty");
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM skills WHERE skill_name = ?"
            );
            stmt.setString(1, skillName.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error in deleteSkill: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void sendNotification(int userId, String message) {
        if (message == null) {
            System.err.println("Cannot send notification: message is null");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO notifications (user_id, message) VALUES (?, ?)"
            );
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error in sendNotification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static List<String> getUnreadNotifications(int userId) {
        List<String> notifications = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT notification_id, message FROM notifications " +
                "WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                notifications.add(rs.getString("message"));
                
                // Mark as read
                PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?"
                );
                updateStmt.setInt(1, rs.getInt("notification_id"));
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Database error in getUnreadNotifications: " + e.getMessage());
            e.printStackTrace();
        }
        return notifications;
    }
    
    public static void updateSetting(String key, String value) {
        if (key == null || value == null) {
            System.err.println("Cannot update setting: key or value is null");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE settings SET setting_value = ? WHERE setting_key = ?"
            );
            stmt.setString(1, value);
            stmt.setString(2, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error in updateSetting: " + e.getMessage());
            e.printStackTrace();
        }
    }
}