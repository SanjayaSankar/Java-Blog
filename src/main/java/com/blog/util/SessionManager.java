package com.blog.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages user sessions for the application
 * Implemented as a singleton to ensure consistent session management
 */
public class SessionManager {
    // Singleton instance
    private static SessionManager instance;
    
    // Session expiration time in milliseconds (24 hours)
    private static final long SESSION_EXPIRATION = 24 * 60 * 60 * 1000;
    
    /**
     * Private constructor for Singleton pattern
     */
    private SessionManager() {
        // Private constructor
    }
    
    /**
     * Get the singleton instance of SessionManager
     * @return The singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Create a new session for a user
     * 
     * @param userId The user ID
     * @param sessionToken The session token
     * @return true if session was created successfully
     */
    public boolean createSession(int userId, String sessionToken) {
        String sql = "INSERT INTO sessions (user_id, session_token, expires_at) VALUES (?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            // Calculate expiration time
            Date now = new Date();
            Timestamp expiresAt = new Timestamp(now.getTime() + SESSION_EXPIRATION);
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, sessionToken);
            pstmt.setTimestamp(3, expiresAt);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating session: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt);
        }
    }
    
    /**
     * Validate a session token and return the associated user ID
     * 
     * @param sessionToken The session token to validate
     * @return Optional containing the user ID if session is valid, empty otherwise
     */
    public Optional<Integer> validateSession(String sessionToken) {
        String sql = "SELECT user_id, expires_at FROM sessions WHERE session_token = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, sessionToken);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp expiresAt = rs.getTimestamp("expires_at");
                Date now = new Date();
                
                // Check if session has expired
                if (now.after(expiresAt)) {
                    // Session has expired, remove it
                    invalidateSession(sessionToken);
                    return Optional.empty();
                }
                
                // Session is valid
                int userId = rs.getInt("user_id");
                return Optional.of(userId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error validating session: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return Optional.empty();
    }
    
    /**
     * Invalidate a session token
     * 
     * @param sessionToken The session token to invalidate
     * @return true if session was invalidated successfully
     */
    public boolean invalidateSession(String sessionToken) {
        String sql = "DELETE FROM sessions WHERE session_token = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, sessionToken);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error invalidating session: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt);
        }
    }
    
    /**
     * Generate a new session token
     */
    public String generateSessionToken() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() {
        String sql = "DELETE FROM sessions WHERE expires_at < ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error cleaning up expired sessions: " + e.getMessage());
        } finally {
            closeResources(pstmt);
        }
    }
    
    /**
     * Close database resources
     */
    private void closeResources(PreparedStatement stmt) {
        closeResources(stmt, null);
    }
    
    /**
     * Close database resources
     */
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
    
    // For testing purposes - should not be used in production
    protected static void resetInstance() {
        instance = null;
    }
} 