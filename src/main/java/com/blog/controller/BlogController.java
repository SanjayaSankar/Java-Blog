package com.blog.controller;

import com.blog.dao.BlogPostDAO;
import com.blog.dao.MediaDAO;
import com.blog.model.BlogPost;
import com.blog.model.Media;
import com.blog.model.User;
import com.blog.service.AnalyticsService;
import com.blog.util.DatabaseUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlogController {
    private final BlogPostDAO blogPostDAO;
    private final MediaDAO mediaDAO;
    private final UserController userController;
    private final CommentController commentController;
    private final LikeController likeController;
    private final AnalyticsService analyticsService;

    public BlogController(UserController userController) {
        this.blogPostDAO = new BlogPostDAO();
        this.mediaDAO = new MediaDAO();
        this.userController = userController;
        this.commentController = new CommentController(userController);
        this.likeController = new LikeController(userController);
        this.analyticsService = AnalyticsService.getInstance();
    }

    /**
     * Create a new blog post
     */
    public boolean createPost(String title, String content, String tags, String status) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }

        // Validate inputs
        if (title == null || title.trim().isEmpty() ||
            content == null || content.trim().isEmpty()) {
            return false;
        }

        // Set default status if not provided
        if (status == null || status.trim().isEmpty()) {
            status = "draft";
        }

        // Create and save post
        User currentUser = userController.getCurrentUser();
        BlogPost post = new BlogPost(title, content, currentUser);
        post.setTags(tags);
        post.setStatus(status);
        
        boolean success = blogPostDAO.createPost(post);
        
        // Track event if successful
        if (success && post.getId() > 0) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("postId", post.getId());
            properties.put("title", title);
            properties.put("status", status);
            properties.put("tags", tags);
            properties.put("contentLength", content.length());
            
            analyticsService.trackEvent(AnalyticsService.EVENT_POST_CREATE, 
                currentUser, properties);
        }
        
        return success;
    }

    /**
     * Add a media attachment to a blog post
     */
    public boolean addMediaToPost(int postId, File file, String caption) {
        // Check if user is logged in and can edit this post
        if (!userController.isLoggedIn()) {
            return false;
        }

        // Validate post ownership
        Optional<BlogPost> postOpt = blogPostDAO.getPostById(postId);
        if (postOpt.isEmpty() || postOpt.get().getAuthor().getId() != userController.getCurrentUser().getId()) {
            return false;
        }

        try {
            // Read file data
            byte[] fileData = Files.readAllBytes(file.toPath());
            
            // Determine file type based on extension
            String fileName = file.getName();
            String fileType = getFileType(fileName);
            
            // Create media object
            Media media = new Media(fileName, fileType, fileData);
            media.setPostId(postId);
            media.setCaption(caption);
            
            // Save media
            return mediaDAO.saveMedia(media);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add a media attachment to a blog post using binary data
     */
    public boolean addMediaToPost(int postId, String fileName, String fileType, byte[] fileData, String caption) {
        // Check if user is logged in and can edit this post
        if (!userController.isLoggedIn()) {
            return false;
        }

        // Validate post ownership
        Optional<BlogPost> postOpt = blogPostDAO.getPostById(postId);
        if (postOpt.isEmpty() || postOpt.get().getAuthor().getId() != userController.getCurrentUser().getId()) {
            return false;
        }

        // Create media object
        Media media = new Media(fileName, fileType, fileData);
        media.setPostId(postId);
        media.setCaption(caption);
        
        // Save media
        return mediaDAO.saveMedia(media);
    }
    
    /**
     * Remove a media attachment from a post
     */
    public boolean removeMediaFromPost(int mediaId) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }

        // Get the media
        Optional<Media> mediaOpt = mediaDAO.getMediaById(mediaId);
        if (mediaOpt.isEmpty()) {
            return false;
        }

        // Check post ownership
        Media media = mediaOpt.get();
        Optional<BlogPost> postOpt = blogPostDAO.getPostById(media.getPostId());
        if (postOpt.isEmpty() || postOpt.get().getAuthor().getId() != userController.getCurrentUser().getId()) {
            return false;
        }

        // Delete the media
        return mediaDAO.deleteMedia(mediaId);
    }

    /**
     * Get media attachment by ID
     */
    public Optional<Media> getMediaById(int mediaId) {
        return mediaDAO.getMediaById(mediaId);
    }

    /**
     * Get all published blog posts
     */
    public List<BlogPost> getAllPosts() {
        return blogPostDAO.getAllPosts();
    }
    
    /**
     * Get all blog posts (for admin)
     */
    public List<BlogPost> getAllPostsForAdmin() {
        if (!userController.isLoggedIn() || !userController.isCurrentUserAdmin()) {
            return List.of();
        }
        return blogPostDAO.getAllPostsForAdmin();
    }

    /**
     * Get all posts by the current user
     */
    public List<BlogPost> getCurrentUserPosts() {
        if (!userController.isLoggedIn()) {
            return List.of();
        }
        
        return blogPostDAO.getPostsByUser(userController.getCurrentUser().getId());
    }
    
    /**
     * Get posts by a specific tag
     */
    public List<BlogPost> getPostsByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return List.of();
        }
        
        return blogPostDAO.getPostsByTag(tag.trim());
    }

    /**
     * Get a specific blog post by ID
     */
    public Optional<BlogPost> getPostById(int id) {
        Optional<BlogPost> postOpt = blogPostDAO.getPostById(id);
        
        if (postOpt.isPresent()) {
            BlogPost post = postOpt.get();
            
            // Load comments
            post.setComments(commentController.getCommentsForPost(id));
            
            // Load likes
            post.setLikes(likeController.getLikesForPost(id));
            
            return Optional.of(post);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get a specific blog post by ID and increment its view count
     */
    public Optional<BlogPost> getPostByIdAndIncrementViews(int id) {
        Optional<BlogPost> postOpt = getPostById(id);
        
        if (postOpt.isPresent()) {
            BlogPost post = postOpt.get();
            
            // Increment the view count in the database
            blogPostDAO.incrementViewCount(id);
            
            // Update the view count in the post object
            post.incrementViewCount();
            
            return Optional.of(post);
        }
        
        return Optional.empty();
    }

    /**
     * Update an existing blog post
     */
    public boolean updatePost(int postId, String title, String content, String tags, String status) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }

        // Validate inputs
        if (title == null || title.trim().isEmpty() ||
            content == null || content.trim().isEmpty()) {
            return false;
        }

        // Get the post
        Optional<BlogPost> postOpt = blogPostDAO.getPostById(postId);
        if (postOpt.isEmpty()) {
            return false;
        }

        BlogPost post = postOpt.get();
        
        // Check if the current user is the author or admin
        User currentUser = userController.getCurrentUser();
        if (post.getAuthor().getId() != currentUser.getId() && !currentUser.isAdmin()) {
            return false;
        }

        // Update the post
        post.setTitle(title);
        post.setContent(content);
        post.setTags(tags);
        
        // Only update status if provided
        if (status != null && !status.trim().isEmpty()) {
            post.setStatus(status);
        }
        
        boolean success = blogPostDAO.updatePost(post);
        
        // Track event if successful
        if (success) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("postId", postId);
            properties.put("title", title);
            properties.put("status", post.getStatus());
            properties.put("tags", tags);
            properties.put("contentLength", content.length());
            
            analyticsService.trackEvent(AnalyticsService.EVENT_POST_EDIT, 
                currentUser, properties);
        }
        
        return success;
    }
    
    /**
     * Change the status of a post (publish, archive, etc.)
     */
    public boolean changePostStatus(int postId, String status) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        // Valid statuses
        if (!"draft".equals(status) && !"published".equals(status) && !"archived".equals(status)) {
            return false;
        }

        // Get the post
        Optional<BlogPost> postOpt = blogPostDAO.getPostById(postId);
        if (postOpt.isEmpty()) {
            return false;
        }

        BlogPost post = postOpt.get();
        
        // Check if the current user is the author or admin
        User currentUser = userController.getCurrentUser();
        if (post.getAuthor().getId() != currentUser.getId() && !currentUser.isAdmin()) {
            return false;
        }

        // Update the status
        post.setStatus(status);
        return blogPostDAO.updatePost(post);
    }

    /**
     * Delete a blog post
     */
    public boolean deletePost(int postId) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        // Check if user is the post author or an admin
        Optional<BlogPost> postOpt = blogPostDAO.getPostById(postId);
        if (postOpt.isEmpty()) {
            return false;
        }
        
        User currentUser = userController.getCurrentUser();
        int authorId = postOpt.get().getAuthor().getId();
        
        if (currentUser.getId() != authorId && !currentUser.isAdmin()) {
            return false;
        }

        // Track event before deleting
        Map<String, Object> properties = new HashMap<>();
        properties.put("postId", postId);
        properties.put("title", postOpt.get().getTitle());
        properties.put("authorId", authorId);
        
        boolean success = blogPostDAO.deletePost(postId, authorId);
        
        if (success) {
            analyticsService.trackEvent(AnalyticsService.EVENT_POST_DELETE, 
                currentUser, properties);
        }
        
        return success;
    }
    
    /**
     * Add a comment to a post
     */
    public boolean addComment(int postId, String content) {
        boolean success = commentController.addComment(postId, content);
        
        // Track event if successful
        if (success && userController.isLoggedIn()) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("postId", postId);
            properties.put("commentLength", content.length());
            
            analyticsService.trackEvent(AnalyticsService.EVENT_COMMENT_CREATE, 
                userController.getCurrentUser(), properties);
        }
        
        return success;
    }
    
    /**
     * Add a reply to a comment
     */
    public boolean addReply(int postId, int parentCommentId, String content) {
        return commentController.addReply(postId, parentCommentId, content);
    }
    
    /**
     * Edit a comment
     */
    public boolean editComment(int commentId, String content) {
        return commentController.editComment(commentId, content);
    }
    
    /**
     * Delete a comment
     */
    public boolean deleteComment(int commentId) {
        boolean success = commentController.deleteComment(commentId);
        
        // Track event if successful
        if (success && userController.isLoggedIn()) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("commentId", commentId);
            
            analyticsService.trackEvent(AnalyticsService.EVENT_COMMENT_DELETE, 
                userController.getCurrentUser(), properties);
        }
        
        return success;
    }
    
    /**
     * Hide or unhide a comment (for moderation)
     */
    public boolean toggleCommentHidden(int commentId) {
        return commentController.toggleCommentHidden(commentId);
    }
    
    /**
     * Like a post
     */
    public boolean likePost(int postId) {
        boolean success = likeController.likePost(postId);
        
        // Track event if successful
        if (success && userController.isLoggedIn()) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("postId", postId);
            properties.put("reaction", "like");
            
            analyticsService.trackEvent(AnalyticsService.EVENT_LIKE, 
                userController.getCurrentUser(), properties);
        }
        
        return success;
    }
    
    /**
     * Dislike a post
     */
    public boolean dislikePost(int postId) {
        boolean success = likeController.dislikePost(postId);
        
        // Track event if successful
        if (success && userController.isLoggedIn()) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("postId", postId);
            properties.put("reaction", "dislike");
            
            analyticsService.trackEvent(AnalyticsService.EVENT_DISLIKE, 
                userController.getCurrentUser(), properties);
        }
        
        return success;
    }
    
    /**
     * Remove a like/dislike from a post
     */
    public boolean removeReaction(int postId) {
        return likeController.removeReaction(postId);
    }
    
    /**
     * Get the current user's reaction to a post
     */
    public Boolean getUserReaction(int postId) {
        return likeController.getUserReaction(postId);
    }
    
    /**
     * Get the comment controller
     */
    public CommentController getCommentController() {
        return commentController;
    }
    
    /**
     * Get the like controller
     */
    public LikeController getLikeController() {
        return likeController;
    }
    
    /**
     * Get the like count for a post
     */
    public int getLikeCount(int postId) {
        return likeController.getLikeCount(postId);
    }
    
    /**
     * Get the dislike count for a post
     */
    public int getDislikeCount(int postId) {
        return likeController.getDislikeCount(postId);
    }
    
    /**
     * Get the comment count for a post
     */
    public int getCommentCount(int postId) {
        return commentController.countCommentsForPost(postId);
    }
    
    /**
     * Search for posts matching the query in title or content
     */
    public List<BlogPost> searchPosts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPosts(); // Return all posts if search query is empty
        }
        
        return blogPostDAO.searchPosts(query.trim());
    }
    
    /**
     * Get posts sorted by popularity (view count)
     */
    public List<BlogPost> getPopularPosts() {
        return blogPostDAO.getPostsByPopularity();
    }
    
    /**
     * Determine file type based on file name
     */
    public String getFileType(String fileName) {
        return DatabaseUtil.getFileType(fileName);
    }
} 