package com.jfsd.quize.dto;

public class LoginRequest {

    private String id;
    private String password;
    private String role;   // ✅ NEW

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }          // ✅ NEW
    public void setRole(String role) { this.role = role; }  // ✅ NEW
}