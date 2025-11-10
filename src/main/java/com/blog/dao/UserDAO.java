package com.blog.dao;

import com.blog.model.User;
import com.blog.util.DatabaseUtil;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entity.
 * Implemented as a singleton to ensure consistent database access.
 */
public class UserDAO {
    // Singleton instance
    private static UserDAO instance;
    
    /**
     * Private constructor for Singleton pattern
     */
    private UserDAO() {
        // Private constructor
    }
    
    /**
     * Get the singleton instance of UserDAO
     * @return The singleton instance
     */
    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }
    
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, email, full_name, role, bio) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Password is already encrypted by User.setPassword()
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getBio());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    public boolean updateProfilePicture(int userId, byte[] imageData) {
        String sql = "UPDATE users SET profile_image_data = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            if (imageData != null && imageData.length > 0) {
                ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                pstmt.setBinaryStream(1, bais, imageData.length);
            } else {
                pstmt.setNull(1, java.sql.Types.BLOB);
            }
            
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating profile picture: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, bio = ?, role = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getBio());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            // Encrypt the password
            String encryptedPassword = User.encryptPassword(newPassword);
            pstmt.setString(1, encryptedPassword);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    public Optional<User> authenticateUser(String username, String password) {
        // First check if user exists
        if (username == null || password == null) {
            return Optional.empty();
        }
        
        String sql = "SELECT * FROM users WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, username);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                // User exists, check password
                String storedPassword = rs.getString("password");
                
                // Check if stored password matches input password directly
                // This handles the case where passwords are stored in plaintext in the database
                if (password.equals(storedPassword)) {
                    User user = extractUserFromResultSet(rs);
                    return Optional.of(user);
                }
                
                // If direct comparison fails, try with encrypted password
                // This handles the case where passwords are expected to be encrypted
                String encryptedInputPassword = User.encryptPassword(password);
                if (encryptedInputPassword.equals(storedPassword)) {
                    User user = extractUserFromResultSet(rs);
                    return Optional.of(user);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return Optional.empty();
    }
    
    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, id);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                return Optional.of(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return Optional.empty();
    }
    
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, username);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return false;
    }
    
    public boolean isEmailTaken(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, email);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return false;
    }
    
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY username";
        List<User> users = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                users.add(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return users;
    }
    
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEncryptedPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        
        // Handle new fields which might be null
        String role = rs.getString("role");
        if (role != null) {
            user.setRole(role);
        }
        
        String profilePicture = rs.getString("profile_picture");
        if (profilePicture != null) {
            user.setProfilePicture(profilePicture);
        }
        
        String bio = rs.getString("bio");
        if (bio != null) {
            user.setBio(bio);
        }
        
        try {
            byte[] imageData = rs.getBytes("profile_image_data");
            if (imageData != null) {
                user.setProfileImageData(imageData);
            }
        } catch (SQLException e) {
            // Profile image data might not exist yet
            System.err.println("Warning: Could not retrieve profile image data: " + e.getMessage());
        }
        
        return user;
    }
    
    private void closeResources(PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
    
    public int getLastRegisteredUserId() {
        String sql = "SELECT MAX(id) as last_id FROM users";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("last_id");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting last user ID: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return -1; // Return -1 if no user found or error occurs
    }
    
    // For testing purposes - should not be used in production
    protected static void resetInstance() {
        instance = null;
    }
}