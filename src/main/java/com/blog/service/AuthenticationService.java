package com.blog.service;

import com.blog.controller.UserController;
import com.blog.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Singleton service class for handling authentication operations including
 * login, registration, and session management.
 */
public class AuthenticationService {
    // Singleton instance
    private static AuthenticationService instance;
    
    // Reference to UserController (also a singleton)
    private final UserController userController;
    
    // Reference to AnalyticsService
    private final AnalyticsService analyticsService;
    
    // Timestamp of last login
    private long lastLoginTime;

    // Private constructor for Singleton pattern
    private AuthenticationService() {
        this.userController = UserController.getInstance();
        this.analyticsService = AnalyticsService.getInstance();
        this.lastLoginTime = 0;
    }
    
    /**
     * Get the singleton instance of AuthenticationService
     * @return The singleton instance
     */
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }
    
    /**
     * Authenticate a user and create a session
     * 
     * @param username The username
     * @param password The password
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password) {
        boolean success = userController.login(username, password);
        
        if (success) {
            // Record login time
            lastLoginTime = System.currentTimeMillis();
            
            // Track login event
            User user = userController.getCurrentUser();
            Map<String, Object> properties = new HashMap<>();
            properties.put("method", "credentials");
            properties.put("username", username);
            
            analyticsService.trackEvent(AnalyticsService.EVENT_LOGIN, user, properties);
        }
        
        return success;
    }
    
    /**
     * Register a new user with basic information
     * 
     * @param username The username
     * @param password The password
     * @param email The email address
     * @param fullName The user's full name
     * @return true if registration successful, false otherwise
     */
    public boolean register(String username, String password, String email, String fullName) {
        boolean success = userController.registerUser(username, password, email, fullName);
        
        if (success) {
            // Track registration event
            Map<String, Object> properties = new HashMap<>();
            properties.put("username", username);
            properties.put("email", email);
            
            analyticsService.trackEvent(AnalyticsService.EVENT_REGISTRATION, null, properties);
            
            // Auto login
            if (login(username, password)) {
                // Now the user is logged in, update analytics with user info
                analyticsService.trackEvent(AnalyticsService.EVENT_REGISTRATION, 
                    userController.getCurrentUser(), "auto_login", true);
            }
        }
        
        return success;
    }
    
    /**
     * Register a new user with extended information
     * 
     * @param username The username
     * @param password The password
     * @param email The email address
     * @param fullName The user's full name
     * @param role The user's role
     * @param bio The user's biography
     * @return true if registration successful, false otherwise
     */
    public boolean register(String username, String password, String email, String fullName, 
                           String role, String bio) {
        boolean success = userController.registerUser(username, password, email, fullName, role, bio);
        
        if (success) {
            // Track registration event
            Map<String, Object> properties = new HashMap<>();
            properties.put("username", username);
            properties.put("email", email);
            properties.put("role", role);
            properties.put("has_bio", bio != null && !bio.trim().isEmpty());
            
            analyticsService.trackEvent(AnalyticsService.EVENT_REGISTRATION, null, properties);
            
            // Auto login
            if (login(username, password)) {
                // Now the user is logged in, update analytics with user info
                analyticsService.trackEvent(AnalyticsService.EVENT_REGISTRATION, 
                    userController.getCurrentUser(), "auto_login", true);
            }
        }
        
        return success;
    }
    
    /**
     * Register a new user with extended information and profile picture
     * 
     * @param username The username
     * @param password The password
     * @param email The email address
     * @param fullName The user's full name
     * @param role The user's role
     * @param bio The user's biography
     * @param profileImageData The user's profile picture data
     * @return true if registration successful, false otherwise
     */
    public boolean register(String username, String password, String email, String fullName, 
                           String role, String bio, byte[] profileImageData) {
        // Register user first
        boolean success = userController.registerUser(username, password, email, fullName, role, bio);
        
        // If registration was successful and we have profile image data, update it
        if (success && profileImageData != null && profileImageData.length > 0) {
            int userId = userController.getLastRegisteredUserId();
            if (userId > 0) {
                userController.updateProfilePicture(userId, profileImageData);
            }
        }
        
        if (success) {
            // Track registration event
            Map<String, Object> properties = new HashMap<>();
            properties.put("username", username);
            properties.put("email", email);
            properties.put("role", role);
            properties.put("has_bio", bio != null && !bio.trim().isEmpty());
            properties.put("has_profile_picture", profileImageData != null && profileImageData.length > 0);
            
            analyticsService.trackEvent(AnalyticsService.EVENT_REGISTRATION, null, properties);
            
            // Auto login
            if (login(username, password)) {
                // Now the user is logged in, update analytics with user info
                analyticsService.trackEvent(AnalyticsService.EVENT_REGISTRATION, 
                    userController.getCurrentUser(), "auto_login", true);
            }
        }
        
        return success;
    }
    
    /**
     * Log out the current user
     */
    public void logout() {
        // Track logout event before logging out
        if (userController.isLoggedIn()) {
            User user = userController.getCurrentUser();
            Map<String, Object> properties = new HashMap<>();
            properties.put("session_duration_seconds", (System.currentTimeMillis() - lastLoginTime) / 1000);
            
            analyticsService.trackEvent(AnalyticsService.EVENT_LOGOUT, user, properties);
        }
        
        userController.logout();
    }
    
    /**
     * Get the timestamp of the last login
     * @return The last login time in milliseconds
     */
    public long getLastLoginTime() {
        return lastLoginTime;
    }
    
    /**
     * Check if a user is currently logged in
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return userController.isLoggedIn();
    }
    
    /**
     * Get the currently logged in user
     * 
     * @return The current user or null if no user is logged in
     */
    public User getCurrentUser() {
        return userController.getCurrentUser();
    }
    
    /**
     * Resume a user session with a saved token
     * 
     * @param sessionToken The session token
     * @return true if session was resumed successfully, false otherwise
     */
    public boolean resumeSession(String sessionToken) {
        boolean success = userController.resumeSession(sessionToken);
        
        if (success) {
            // Record login time
            lastLoginTime = System.currentTimeMillis();
            
            // Track session resume event
            User user = userController.getCurrentUser();
            analyticsService.trackEvent(AnalyticsService.EVENT_LOGIN, user, "method", "session_token");
        }
        
        return success;
    }
    
    /**
     * Get the current session token
     * 
     * @return The current session token or null if no session is active
     */
    public String getCurrentSessionToken() {
        return userController.getCurrentSessionToken();
    }
    
    // For testing purposes - should not be used in production
    protected static void resetInstance() {
        instance = null;
    }
} 