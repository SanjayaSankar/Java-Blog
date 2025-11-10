package com.blog.view;

import com.blog.model.Comment;
import javax.swing.JPanel;

/**
 * Interface for comment display components
 */
public interface CommentDisplay {
    /**
     * Add a comment to the display
     * 
     * @param comment The comment to add
     */
    void addComment(Comment comment);
    
    /**
     * Add a reply to an existing comment
     * 
     * @param parentCommentId The ID of the parent comment
     * @param reply The reply comment to add
     */
    void addReply(int parentCommentId, Comment reply);
    
    /**
     * Remove a comment and all its replies from the display
     * 
     * @param commentId The ID of the comment to remove
     * @return true if the comment was found and removed
     */
    boolean removeComment(int commentId);
    
    /**
     * Refresh the display with the latest comments
     * 
     * @param postId The ID of the post to load comments for
     */
    void refreshComments(int postId);
    
    /**
     * Get the main panel containing the comments
     * 
     * @return The panel with comments
     */
    JPanel getCommentsPanel();
} 