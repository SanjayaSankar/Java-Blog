package com.blog.dao;

import com.blog.model.BlogPost;
import com.blog.model.Media;
import com.blog.model.User;
import com.blog.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BlogPostDAO {
    
    private final MediaDAO mediaDAO = new MediaDAO();
    private final UserDAO userDAO = UserDAO.getInstance();
    
    public boolean createPost(BlogPost post) {
        String sql = "INSERT INTO blog_posts (title, content, user_id, status, tags, created_at, updated_at, view_count) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setInt(3, post.getAuthor().getId());
            pstmt.setString(4, post.getStatus());
            pstmt.setString(5, post.getTags());
            pstmt.setTimestamp(6, new Timestamp(post.getCreatedAt().getTime()));
            pstmt.setTimestamp(7, new Timestamp(post.getUpdatedAt().getTime()));
            pstmt.setInt(8, post.getViewCount());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int postId = rs.getInt(1);
                    post.setId(postId);
                    
                    // Save media attachments if any
                    if (post.getMediaAttachments() != null && !post.getMediaAttachments().isEmpty()) {
                        for (Media media : post.getMediaAttachments()) {
                            media.setPostId(postId);
                            mediaDAO.saveMedia(media);
                        }
                    }
                    
                    return true;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error creating post: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, rs);
        }
    }
    
    public List<BlogPost> getAllPosts() {
        String sql = "SELECT * FROM blog_posts WHERE status = 'published' ORDER BY created_at DESC";
        List<BlogPost> posts = new ArrayList<>();
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                BlogPost post = extractPostFromResultSet(rs);
                if (post != null) {
                    // Load media attachments
                    List<Media> mediaList = mediaDAO.getMediaByPostId(post.getId());
                    post.setMediaAttachments(mediaList);
                    
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving posts: " + e.getMessage());
        } finally {
            closeResources(stmt, rs);
        }
        
        return posts;
    }
    
    public List<BlogPost> getAllPostsForAdmin() {
        String sql = "SELECT * FROM blog_posts ORDER BY created_at DESC";
        List<BlogPost> posts = new ArrayList<>();
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                BlogPost post = extractPostFromResultSet(rs);
                if (post != null) {
                    // Load media attachments
                    List<Media> mediaList = mediaDAO.getMediaByPostId(post.getId());
                    post.setMediaAttachments(mediaList);
                    
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving posts: " + e.getMessage());
        } finally {
            closeResources(stmt, rs);
        }
        
        return posts;
    }
    
    public List<BlogPost> getPostsByUser(int userId) {
        String sql = "SELECT * FROM blog_posts WHERE user_id = ? ORDER BY created_at DESC";
        List<BlogPost> posts = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, userId);
            
            rs = pstmt.executeQuery();
            Optional<User> userOpt = userDAO.getUserById(userId);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                while (rs.next()) {
                    int postId = rs.getInt("id");
                    BlogPost post = new BlogPost(
                        postId,
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at"),
                        user,
                        rs.getString("status"),
                        rs.getString("tags")
                    );
                    
                    // Load media attachments
                    List<Media> mediaList = mediaDAO.getMediaByPostId(postId);
                    post.setMediaAttachments(mediaList);
                    
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving user posts: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return posts;
    }
    
    public List<BlogPost> getPostsByTag(String tag) {
        // Match posts that have this tag in their tags field
        String sql = "SELECT * FROM blog_posts WHERE status = 'published' AND (tags LIKE ? OR tags LIKE ? OR tags LIKE ? OR tags = ?) ORDER BY created_at DESC";
        List<BlogPost> posts = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            // Match tag as a single tag or part of a comma-separated list
            pstmt.setString(1, tag + ",%");  // Starts with this tag
            pstmt.setString(2, "%," + tag + ",%");  // Has this tag in middle
            pstmt.setString(3, "%," + tag);  // Ends with this tag
            pstmt.setString(4, tag);  // Is exactly this tag
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BlogPost post = extractPostFromResultSet(rs);
                if (post != null) {
                    // Load media attachments
                    List<Media> mediaList = mediaDAO.getMediaByPostId(post.getId());
                    post.setMediaAttachments(mediaList);
                    
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving posts by tag: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return posts;
    }
    
    public Optional<BlogPost> getPostById(int id) {
        String sql = "SELECT * FROM blog_posts WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, id);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                BlogPost post = extractPostFromResultSet(rs);
                if (post != null) {
                    // Load media attachments
                    List<Media> mediaList = mediaDAO.getMediaByPostId(post.getId());
                    post.setMediaAttachments(mediaList);
                    
                    return Optional.of(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving post: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return Optional.empty();
    }
    
    public boolean updatePost(BlogPost post) {
        String sql = "UPDATE blog_posts SET title = ?, content = ?, status = ?, tags = ?, updated_at = ?, view_count = ? " +
                     "WHERE id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            post.setUpdatedAt(new Date()); // Update the timestamp
            
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getStatus());
            pstmt.setString(4, post.getTags());
            pstmt.setTimestamp(5, new Timestamp(post.getUpdatedAt().getTime()));
            pstmt.setInt(6, post.getViewCount());
            pstmt.setInt(7, post.getId());
            pstmt.setInt(8, post.getAuthor().getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating post: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    public boolean deletePost(int postId, int userId) {
        // First, delete all media attachments
        if (!mediaDAO.deleteMediaForPost(postId)) {
            return false;
        }
        
        // Then delete the post
        String sql = "DELETE FROM blog_posts WHERE id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting post: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    /**
     * Extract a BlogPost object from a ResultSet
     */
    private BlogPost extractPostFromResultSet(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        Optional<User> userOpt = userDAO.getUserById(userId);
        
        if (userOpt.isPresent()) {
            int postId = rs.getInt("id");
            BlogPost post = new BlogPost(
                postId,
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"),
                userOpt.get(),
                rs.getString("status"),
                rs.getString("tags")
            );
            
            // Set the view count
            post.setViewCount(rs.getInt("view_count"));
            
            return post;
        }
        
        return null;
    }
    
    private void closeResources(Statement stmt, ResultSet rs) {
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
    
    /**
     * Search for blog posts that match the given query in title or content
     * 
     * @param query The search query
     * @return List of blog posts matching the search criteria
     */
    public List<BlogPost> searchPosts(String query) {
        String sql = "SELECT * FROM blog_posts WHERE status = 'published' AND (title LIKE ? OR content LIKE ?) ORDER BY created_at DESC";
        List<BlogPost> posts = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            // Add wildcards to search for partial matches
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BlogPost post = extractPostFromResultSet(rs);
                if (post != null) {
                    // Load media attachments
                    List<Media> mediaList = mediaDAO.getMediaByPostId(post.getId());
                    post.setMediaAttachments(mediaList);
                    
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching posts: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return posts;
    }
    
    /**
     * Increment the view count for a post by 1
     * 
     * @param postId The ID of the post
     * @return true if successful, false otherwise
     */
    public boolean incrementViewCount(int postId) {
        String sql = "UPDATE blog_posts SET view_count = view_count + 1 WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, postId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error incrementing view count: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    /**
     * Get all posts sorted by view count (most viewed first)
     * 
     * @return List of blog posts sorted by view count
     */
    public List<BlogPost> getPostsByPopularity() {
        String sql = "SELECT * FROM blog_posts WHERE status = 'published' ORDER BY view_count DESC, created_at DESC";
        List<BlogPost> posts = new ArrayList<>();
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                BlogPost post = extractPostFromResultSet(rs);
                if (post != null) {
                    // Load media attachments
                    List<Media> mediaList = mediaDAO.getMediaByPostId(post.getId());
                    post.setMediaAttachments(mediaList);
                    
                    posts.add(post);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving posts by popularity: " + e.getMessage());
        } finally {
            closeResources(stmt, rs);
        }
        
        return posts;
    }
} 