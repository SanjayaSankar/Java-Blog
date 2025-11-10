package com.blog.controller;

import com.blog.dao.UserDAO;
import com.blog.model.User;
import com.blog.util.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserController {
    // Singleton instance
    private static UserController instance;
    
    private final UserDAO userDAO;
    private final SessionManager sessionManager;
    private User currentUser;
    private String currentSessionToken;

    // Private constructor for Singleton pattern
    private UserController() {
        this.userDAO = UserDAO.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Get the singleton instance of UserController
     * @return The singleton instance
     */
    public static synchronized UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public boolean registerUser(String username, String password, String email, String fullName) {
        // Validate inputs
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            System.out.println("Registration failed: Empty fields");
            return false;
        }

        // Check if username or email already exists
        if (userDAO.isUsernameTaken(username)) {
            System.out.println("Registration failed: Username taken: " + username);
            return false;
        }

        if (userDAO.isEmailTaken(email)) {
            System.out.println("Registration failed: Email taken: " + email);
            return false;
        }

        // Create and save user
        User user = new User(username, password, email, fullName);
        boolean result = userDAO.registerUser(user);
        if (result) {
            System.out.println("User registered successfully: " + username);
        } else {
            System.out.println("Failed to register user in database: " + username);
        }
        return result;
    }
    
    public boolean registerUser(String username, String password, String email, String fullName, 
                               String role, String bio) {
        // Validate inputs
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            System.out.println("Registration failed: Empty fields");
            return false;
        }

        // Check if username or email already exists
        if (userDAO.isUsernameTaken(username)) {
            System.out.println("Registration failed: Username taken: " + username);
            return false;
        }

        if (userDAO.isEmailTaken(email)) {
            System.out.println("Registration failed: Email taken: " + email);
            return false;
        }

        // Create user with role and bio
        User user = new User(username, password, email, fullName);
        if (role != null && !role.trim().isEmpty()) {
            user.setRole(role);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        
        boolean result = userDAO.registerUser(user);
        if (result) {
            System.out.println("User registered successfully: " + username + " with role: " + role);
        } else {
            System.out.println("Failed to register user in database: " + username);
        }
        return result;
    }
    
    public int getLastRegisteredUserId() {
        return userDAO.getLastRegisteredUserId();
    }
    
    public boolean updateProfilePicture(int userId, byte[] imageData) {
        if (imageData == null) {
            return false;
        }
        
        return userDAO.updateProfilePicture(userId, imageData);
    }

    public boolean login(String username, String password) {
        // Validate inputs
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            System.out.println("Login failed: Empty username or password");
            return false;
        }

        System.out.println("Attempting login for user: " + username);
        
        // Authenticate user
        Optional<User> userOpt = userDAO.authenticateUser(username, password);
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            // Create a new session
            currentSessionToken = UUID.randomUUID().toString();
            sessionManager.createSession(currentUser.getId(), currentSessionToken);
            
            System.out.println("Login successful for user: " + username);
            return true;
        }
        System.out.println("Login failed: Invalid credentials for user: " + username);
        return false;
    }
    
    public boolean resumeSession(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return false;
        }
        
        Optional<Integer> userIdOpt = sessionManager.validateSession(sessionToken);
        if (userIdOpt.isPresent()) {
            int userId = userIdOpt.get();
            Optional<User> userOpt = userDAO.getUserById(userId);
            
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                currentSessionToken = sessionToken;
                return true;
            }
        }
        
        return false;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
            if (currentSessionToken != null) {
                sessionManager.invalidateSession(currentSessionToken);
                currentSessionToken = null;
            }
        }
        currentUser = null;
    }
    
    public boolean updateProfile(String fullName, String email, String bio) {
        if (!isLoggedIn()) {
            return false;
        }
        
        // Validate email hasn't been taken by another user
        if (!email.equals(currentUser.getEmail()) && userDAO.isEmailTaken(email)) {
            return false;
        }
        
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setBio(bio);
        
        return userDAO.updateUserProfile(currentUser);
    }
    
    /**
     * Update the role of the current user
     * Should only be called if the current user is an admin
     */
    public boolean updateCurrentUserRole(String newRole) {
        if (!isLoggedIn() || !isCurrentUserAdmin()) {
            return false;
        }
        
        if (newRole == null || newRole.trim().isEmpty()) {
            return false;
        }
        
        // Only allow valid roles
        if (!newRole.equals("admin") && !newRole.equals("author") && !newRole.equals("reader")) {
            return false;
        }
        
        currentUser.setRole(newRole);
        return userDAO.updateUserProfile(currentUser);
    }
    
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!isLoggedIn() || oldPassword == null || newPassword == null) {
            return false;
        }
        
        // Verify old password
        if (!currentUser.validatePassword(oldPassword)) {
            return false;
        }
        
        return userDAO.changePassword(currentUser.getId(), newPassword);
    }
    
    public boolean updateProfilePicture(byte[] imageData) {
        if (!isLoggedIn() || imageData == null) {
            return false;
        }
        
        boolean success = userDAO.updateProfilePicture(currentUser.getId(), imageData);
        if (success) {
            // Update current user's profile image
            currentUser.setProfileImageData(imageData);
        }
        return success;
    }
    
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    
    public boolean promoteToAuthor(int userId) {
        if (!isLoggedIn() || !currentUser.isAdmin()) {
            return false;
        }
        
        Optional<User> userOpt = userDAO.getUserById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole("author");
            return userDAO.updateUserProfile(user);
        }
        
        return false;
    }
    
    public boolean promoteToAdmin(int userId) {
        if (!isLoggedIn() || !currentUser.isAdmin()) {
            return false;
        }
        
        Optional<User> userOpt = userDAO.getUserById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole("admin");
            return userDAO.updateUserProfile(user);
        }
        
        return false;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isCurrentUserAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }
    
    public boolean canCurrentUserEditContent() {
        return isLoggedIn() && currentUser.canEditContent();
    }

    public User getCurrentUser() {
        return currentUser;
    }
    
    public String getCurrentSessionToken() {
        return currentSessionToken;
    }

    // For testing purposes - should not be used in production
    protected static void resetInstance() {
        instance = null;
    }
}