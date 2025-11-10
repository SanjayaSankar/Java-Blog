package com.blog.dao;

import com.blog.factory.CommentFactory;
import com.blog.model.Comment;
import com.blog.model.User;
import com.blog.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommentDAO {
    private final UserDAO userDAO;
    
    public CommentDAO() {
        this.userDAO = UserDAO.getInstance();
    }
    
    /**
     * Create a new comment in the database
     */
    public boolean createComment(Comment comment) {
        String sql = "INSERT INTO comments (post_id, user_id, content, created_at, parent_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, comment.getPostId());
            stmt.setInt(2, comment.getAuthor().getId());
            stmt.setString(3, comment.getContent());
            stmt.setTimestamp(4, new Timestamp(comment.getCreatedAt().getTime()));
            
            // Set parent_id (null for top-level comments)
            if (comment.getParentId() != null) {
                stmt.setInt(5, comment.getParentId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error creating comment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Save or update a comment in the database
     */
    public boolean saveComment(Comment comment) {
        if (comment.getId() > 0) {
            // Update existing comment
            String sql = "UPDATE comments SET content = ?, hidden = ? WHERE id = ?";
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, comment.getContent());
                stmt.setBoolean(2, comment.isHidden());
                stmt.setInt(3, comment.getId());
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
                
            } catch (SQLException e) {
                System.err.println("Error updating comment: " + e.getMessage());
                return false;
            }
        } else {
            // Create new comment
            return createComment(comment);
        }
    }
    
    /**
     * Get a comment by ID
     */
    public Optional<Comment> getCommentById(int commentId) {
        String sql = "SELECT * FROM comments WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Comment comment = extractCommentFromResultSet(rs);
                rs.close();
                return Optional.ofNullable(comment);
            }
            
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error retrieving comment: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Update a comment's content
     */
    public boolean updateComment(int commentId, String newContent) {
        String sql = "UPDATE comments SET content = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newContent);
            stmt.setInt(2, commentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating comment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Toggle the hidden state of a comment
     */
    public boolean toggleCommentHidden(int commentId) {
        // First get the current hidden state
        String selectSql = "SELECT hidden FROM comments WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            
            selectStmt.setInt(1, commentId);
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                boolean currentHidden = rs.getBoolean("hidden");
                rs.close();
                
                // Now update to the opposite state
                String updateSql = "UPDATE comments SET hidden = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setBoolean(1, !currentHidden);
                    updateStmt.setInt(2, commentId);
                    
                    int rowsAffected = updateStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
            
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error toggling comment hidden state: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Count comments for a post
     */
    public int countCommentsForPost(int postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error counting comments: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Get all comments for a specific post
     */
    public List<Comment> getCommentsForPost(int postId) {
        return getCommentsForPostWithThreading(postId);
    }
    
    /**
     * Get all comments for a post with threading (nested comments)
     */
    public List<Comment> getCommentsForPostWithThreading(int postId) {
        Map<Integer, Comment> commentMap = new HashMap<>();
        List<Comment> topLevelComments = new ArrayList<>();
        
        String sql = "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at ASC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            
            // First pass: create all Comment objects
            while (rs.next()) {
                Comment comment = extractCommentFromResultSet(rs);
                if (comment != null) {
                    commentMap.put(comment.getId(), comment);
                    
                    // Add to top-level comments list if this is a top-level comment
                    if (comment.isTopLevel()) {
                        topLevelComments.add(comment);
                    }
                }
            }
            
            rs.close();
            
            // Second pass: build the reply structure
            for (Comment comment : commentMap.values()) {
                if (!comment.isTopLevel()) {
                    Integer parentId = comment.getParentId();
                    Comment parentComment = commentMap.get(parentId);
                    
                    if (parentComment != null) {
                        parentComment.addReply(comment);
                    } else {
                        // If parent not found, treat as top-level
                        topLevelComments.add(comment);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving comments: " + e.getMessage());
        }
        
        return topLevelComments;
    }
    
    /**
     * Get comment count for a post
     */
    public int getCommentCountForPost(int postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error counting comments: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Delete a comment
     */
    public boolean deleteComment(int commentId) {
        // First, delete all replies to this comment
        deleteReplies(commentId);
        
        // Then delete the comment itself
        String sql = "DELETE FROM comments WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete all replies to a comment
     */
    private void deleteReplies(int commentId) {
        String sql = "DELETE FROM comments WHERE parent_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error deleting comment replies: " + e.getMessage());
        }
    }
    
    /**
     * Delete all comments for a post
     */
    public boolean deleteCommentsForPost(int postId) {
        String sql = "DELETE FROM comments WHERE post_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, postId);
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting comments for post: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract a Comment object from a ResultSet
     */
    private Comment extractCommentFromResultSet(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            int postId = rs.getInt("post_id");
            int userId = rs.getInt("user_id");
            String content = rs.getString("content");
            Timestamp createdAt = rs.getTimestamp("created_at");
            boolean hidden = rs.getBoolean("hidden");
            
            // Check if parent_id is null
            Integer parentId = null;
            int parentIdValue = rs.getInt("parent_id");
            if (!rs.wasNull()) {
                parentId = parentIdValue;
            }
            
            // Load the user
            User author = userDAO.getUserById(userId).orElse(null);
            if (author == null) {
                return null;
            }
            
            // Use the CommentFactory to create the comment
            return CommentFactory.createFromDatabase(id, content, createdAt, author, postId, hidden, parentId);
            
        } catch (SQLException e) {
            System.err.println("Error extracting comment from ResultSet: " + e.getMessage());
            return null;
        }
    }
} 