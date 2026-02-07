package ethioskill.models;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String fullName;
    private String phone;
    private String role;
    private String status;
    private String city;
    private String skill;
    private String paymentType;
    private String description;
    private String availability;
    private String photoPath;

    // Constructors
    public User() {}

    public User(int id, String fullName, String phone, String role, String status) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }

    public User(int id, String fullName, String phone, String city, String skill, 
                String paymentType, String status, String availability, String photoPath) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.city = city;
        this.skill = skill;
        this.paymentType = paymentType;
        this.status = status;
        this.availability = availability;
        this.photoPath = photoPath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}