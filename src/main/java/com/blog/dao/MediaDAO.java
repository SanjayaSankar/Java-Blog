package com.blog.dao;

import com.blog.model.Media;
import com.blog.util.DatabaseUtil;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MediaDAO {
    
    /**
     * Save a media file to the database
     */
    public boolean saveMedia(Media media) {
        String sql = "INSERT INTO media (post_id, file_name, file_type, file_data, file_path, uploaded_at, caption) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, media.getPostId());
            pstmt.setString(2, media.getFileName());
            pstmt.setString(3, media.getFileType());
            
            // Set file data as binary stream
            if (media.getFileData() != null) {
                ByteArrayInputStream bais = new ByteArrayInputStream(media.getFileData());
                pstmt.setBinaryStream(4, bais, media.getFileData().length);
            } else {
                pstmt.setNull(4, Types.BLOB);
            }
            
            pstmt.setString(5, media.getFilePath());
            pstmt.setTimestamp(6, new Timestamp(media.getUploadedAt().getTime()));
            pstmt.setString(7, media.getCaption());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    media.setId(rs.getInt(1));
                    return true;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error saving media: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, rs);
        }
    }
    
    /**
     * Get all media attached to a post
     */
    public List<Media> getMediaByPostId(int postId) {
        String sql = "SELECT id, post_id, file_name, file_type, file_path, uploaded_at, caption FROM media WHERE post_id = ?";
        List<Media> mediaList = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, postId);
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Media media = new Media();
                media.setId(rs.getInt("id"));
                media.setPostId(rs.getInt("post_id"));
                media.setFileName(rs.getString("file_name"));
                media.setFileType(rs.getString("file_type"));
                media.setFilePath(rs.getString("file_path"));
                media.setUploadedAt(rs.getTimestamp("uploaded_at"));
                media.setCaption(rs.getString("caption"));
                
                mediaList.add(media);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving media: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return mediaList;
    }
    
    /**
     * Get a specific media file with its binary data
     */
    public Optional<Media> getMediaById(int id) {
        String sql = "SELECT * FROM media WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, id);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Media media = new Media();
                media.setId(rs.getInt("id"));
                media.setPostId(rs.getInt("post_id"));
                media.setFileName(rs.getString("file_name"));
                media.setFileType(rs.getString("file_type"));
                media.setFileData(rs.getBytes("file_data"));
                media.setFilePath(rs.getString("file_path"));
                media.setUploadedAt(rs.getTimestamp("uploaded_at"));
                media.setCaption(rs.getString("caption"));
                
                return Optional.of(media);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving media: " + e.getMessage());
        } finally {
            closeResources(pstmt, rs);
        }
        
        return Optional.empty();
    }
    
    /**
     * Delete a media file
     */
    public boolean deleteMedia(int id) {
        String sql = "DELETE FROM media WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting media: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
    }
    
    /**
     * Delete all media files for a post
     */
    public boolean deleteMediaForPost(int postId) {
        String sql = "DELETE FROM media WHERE post_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, postId);
            
            int affectedRows = pstmt.executeUpdate();
            return true; // Even if no rows affected, we consider this a success
            
        } catch (SQLException e) {
            System.err.println("Error deleting media for post: " + e.getMessage());
            return false;
        } finally {
            closeResources(pstmt, null);
        }
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
} 