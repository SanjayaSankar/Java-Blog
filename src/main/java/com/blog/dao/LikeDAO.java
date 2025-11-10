package com.blog.dao;

import com.blog.factory.LikeFactory;
import com.blog.model.Like;
import com.blog.model.User;
import com.blog.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LikeDAO {
    private final UserDAO userDAO;
    
    public LikeDAO() {
        this.userDAO = UserDAO.getInstance();
    }
    
    /**
     * Create a new like or dislike
     */
    public boolean createLike(Like like) {
        // First, check if the user already has a like/dislike for this post
        Optional<Like> existingLike = getLikeByUserAndPost(like.getUser().getId(), like.getPostId());
        
        if (existingLike.isPresent()) {
            // Update existing like/dislike
            String updateSql = "UPDATE likes SET is_like = ? WHERE id = ?";
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                
                stmt.setBoolean(1, like.isLike());
                stmt.setInt(2, existingLike.get().getId());
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
                
            } catch (SQLException e) {
                System.err.println("Error updating like: " + e.getMessage());
                return false;
            }
        } else {
            // Create new like/dislike
            String insertSql = "INSERT INTO likes (post_id, user_id, is_like, created_at) VALUES (?, ?, ?, ?)";
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setInt(1, like.getPostId());
                stmt.setInt(2, like.getUser().getId());
                stmt.setBoolean(3, like.isLike());
                stmt.setTimestamp(4, new Timestamp(like.getCreatedAt().getTime()));
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        like.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
                
                return false;
                
            } catch (SQLException e) {
                System.err.println("Error creating like: " + e.getMessage());
                return false;
            }
        }
    }
    
    /**
     * Get a like by user ID and post ID
     */
    public Optional<Like> getLikeByUserAndPost(int userId, int postId) {
        String sql = "SELECT * FROM likes WHERE user_id = ? AND post_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Like like = extractLikeFromResultSet(rs);
                rs.close();
                return Optional.ofNullable(like);
            }
            
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving like: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Delete a like/dislike by user ID and post ID
     */
    public boolean deleteLike(int userId, int postId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND post_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting like: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete all likes for a post
     */
    public boolean deleteLikesForPost(int postId) {
        String sql = "DELETE FROM likes WHERE post_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting likes for post: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all likes for a post
     */
    public List<Like> getLikesForPost(int postId) {
        List<Like> likes = new ArrayList<>();
        String sql = "SELECT * FROM likes WHERE post_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Like like = extractLikeFromResultSet(rs);
                if (like != null) {
                    likes.add(like);
                }
            }
            
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving likes: " + e.getMessage());
        }
        
        return likes;
    }
    
    /**
     * Count likes for a post
     */
    public int countLikesForPost(int postId) {
        return countReactionsForPost(postId, true);
    }
    
    /**
     * Count dislikes for a post
     */
    public int countDislikesForPost(int postId) {
        return countReactionsForPost(postId, false);
    }
    
    /**
     * Count reactions (likes or dislikes) for a post
     */
    private int countReactionsForPost(int postId, boolean isLike) {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ? AND is_like = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            stmt.setBoolean(2, isLike);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error counting reactions: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Extract a Like object from a ResultSet
     */
    private Like extractLikeFromResultSet(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            int postId = rs.getInt("post_id");
            int userId = rs.getInt("user_id");
            boolean isLike = rs.getBoolean("is_like");
            Timestamp createdAt = rs.getTimestamp("created_at");
            
            // Load the user
            User user = userDAO.getUserById(userId).orElse(null);
            if (user == null) {
                return null;
            }
            
            // Use the LikeFactory to create the like
            return LikeFactory.createFromDatabase(id, postId, user, isLike, createdAt);
            
        } catch (SQLException e) {
            System.err.println("Error extracting like from ResultSet: " + e.getMessage());
            return null;
        }
    }
} 