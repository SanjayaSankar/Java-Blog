package com.blog.controller;

import com.blog.dao.CommentDAO;
import com.blog.factory.CommentFactory;
import com.blog.model.Comment;
import com.blog.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing comments
 */
public class CommentController {
    private final CommentDAO commentDAO;
    private final UserController userController;
    
    public CommentController(UserController userController) {
        this.commentDAO = new CommentDAO();
        this.userController = userController;
    }
    
    /**
     * Add a top-level comment to a post
     * 
     * @param postId The ID of the post
     * @param content The comment text
     * @return true if the comment was added successfully
     */
    public boolean addComment(int postId, String content) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        // Validate content
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Create comment
        User currentUser = userController.getCurrentUser();
        Comment comment = CommentFactory.createTopLevelComment(content.trim(), currentUser, postId);
        
        // Save comment
        return commentDAO.createComment(comment);
    }
    
    /**
     * Add a reply to an existing comment
     * 
     * @param postId The ID of the post
     * @param parentCommentId The ID of the parent comment
     * @param content The reply text
     * @return true if the reply was added successfully
     */
    public boolean addReply(int postId, int parentCommentId, String content) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        // Validate content
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // First, get the parent comment to determine the level
        Optional<Comment> parentComment = commentDAO.getCommentById(parentCommentId);
        if (parentComment.isEmpty()) {
            return false;
        }
        
        // Calculate the level for the new reply (parent's level + 1)
        int level = parentComment.get().getLevel() + 1;
        
        // Create reply comment
        User currentUser = userController.getCurrentUser();
        Comment reply = CommentFactory.createReplyComment(content.trim(), currentUser, postId, 
                                                         parentCommentId, level);
        
        // Save reply
        return commentDAO.createComment(reply);
    }
    
    /**
     * Edit a comment
     * 
     * @param commentId The ID of the comment to edit
     * @param content The new comment text
     * @return true if the comment was edited successfully
     */
    public boolean editComment(int commentId, String content) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        // Validate content
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Get the comment
        Optional<Comment> commentOpt = commentDAO.getCommentById(commentId);
        if (commentOpt.isEmpty()) {
            return false;
        }
        
        // Check if user is allowed to edit the comment
        User currentUser = userController.getCurrentUser();
        Comment comment = commentOpt.get();
        if (comment.getAuthor().getId() != currentUser.getId() && !currentUser.isAdmin()) {
            return false;
        }
        
        // Update comment
        return commentDAO.updateComment(commentId, content.trim());
    }
    
    /**
     * Delete a comment
     * 
     * @param commentId The ID of the comment to delete
     * @return true if the comment was deleted successfully
     */
    public boolean deleteComment(int commentId) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        // Get the comment
        Optional<Comment> commentOpt = commentDAO.getCommentById(commentId);
        if (commentOpt.isEmpty()) {
            return false;
        }
        
        // Check if user is allowed to delete the comment
        User currentUser = userController.getCurrentUser();
        Comment comment = commentOpt.get();
        if (comment.getAuthor().getId() != currentUser.getId() && !currentUser.isAdmin()) {
            return false;
        }
        
        // Delete comment (this will also delete all replies)
        return commentDAO.deleteComment(commentId);
    }
    
    /**
     * Toggle the hidden state of a comment (admin only)
     * 
     * @param commentId The ID of the comment
     * @return true if the state was toggled successfully
     */
    public boolean toggleCommentHidden(int commentId) {
        // Check if user is admin
        if (!userController.isLoggedIn() || !userController.isCurrentUserAdmin()) {
            return false;
        }
        
        return commentDAO.toggleCommentHidden(commentId);
    }
    
    /**
     * Get all comments for a post with their threaded structure
     * 
     * @param postId The ID of the post
     * @return A list of top-level comments with their replies
     */
    public List<Comment> getCommentsForPost(int postId) {
        return commentDAO.getCommentsForPost(postId);
    }
    
    /**
     * Count the number of comments for a post
     * 
     * @param postId The ID of the post
     * @return The number of comments
     */
    public int countCommentsForPost(int postId) {
        return commentDAO.countCommentsForPost(postId);
    }
    
    /**
     * Get a comment by its ID
     * 
     * @param commentId The ID of the comment to retrieve
     * @return An Optional containing the comment if found
     */
    public Optional<Comment> getCommentById(int commentId) {
        return commentDAO.getCommentById(commentId);
    }
} 