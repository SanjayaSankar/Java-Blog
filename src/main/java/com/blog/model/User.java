package com.blog.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;          // User role: "admin", "author", "reader"
    private String profilePicture; // Path to profile picture
    private String bio;           // User biography
    private byte[] profileImageData; // Binary data for profile image

    public User() {
        this.role = "reader"; // Default role
    }

    public User(String username, String password, String email, String fullName) {
        this();
        this.username = username;
        setPassword(password);    // Use the setter to encrypt the password
        this.email = email;
        this.fullName = fullName;
    }

    public User(int id, String username, String password, String email, String fullName) {
        this(username, password, email, fullName);
        this.id = id;
        // Don't encrypt the password again as it should already be encrypted in database
        this.password = password;
    }
    
    public User(int id, String username, String password, String email, String fullName,
                String role, String profilePicture, String bio) {
        this(id, username, password, email, fullName);
        this.role = role;
        this.profilePicture = profilePicture;
        this.bio = bio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        // Encrypt the password
        this.password = encryptPassword(password);
    }
    
    // Set encrypted password directly (for loading from DB)
    public void setEncryptedPassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public byte[] getProfileImageData() {
        return profileImageData;
    }

    public void setProfileImageData(byte[] profileImageData) {
        this.profileImageData = profileImageData;
    }
    
    // Password encryption using SHA-256
    public static String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error encrypting password: " + e.getMessage());
            return password; // Return unencrypted password if encryption fails
        }
    }
    
    // Validate password
    public boolean validatePassword(String inputPassword) {
        String encryptedInput = encryptPassword(inputPassword);
        return encryptedInput.equals(this.password);
    }
    
    // Determine if user is admin
    public boolean isAdmin() {
        System.out.println("DEBUG: isAdmin called, current role is: '" + this.role + "', equal to 'admin'? " + "admin".equals(this.role));
        return "admin".equals(this.role);
    }
    
    // Determine if user can edit content (admin or author)
    public boolean canEditContent() {
        return "admin".equals(this.role) || "author".equals(this.role);
    }
} 