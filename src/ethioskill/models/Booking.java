package ethioskill.models;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;

public class Booking implements Serializable {
    private int id;
    private int seekerId;
    private int providerId;
    private String seekerName;
    private String phone;
    private String address;
    private LocalDate date;
    private String details;
    private String status;
    
    // Constructors
    public Booking() {}
    
    public Booking(int id, String seekerName, String phone, String address, 
                   LocalDate date, String details, String status) {
        this.id = id;
        this.seekerName = seekerName;
        this.phone = phone;
        this.address = address;
        this.date = date;
        this.details = details;
        this.status = status;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSeekerId() { return seekerId; }
    public void setSeekerId(int seekerId) { this.seekerId = seekerId; }
    
    public int getProviderId() { return providerId; }
    public void setProviderId(int providerId) { this.providerId = providerId; }
    
    public String getSeekerName() { return seekerName; }
    public void setSeekerName(String seekerName) { this.seekerName = seekerName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setDate(Date date) { this.date = date.toLocalDate(); }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}