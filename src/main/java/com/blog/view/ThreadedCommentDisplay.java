package com.blog.view;

import com.blog.controller.BlogController;
import com.blog.controller.UserController;
import com.blog.model.Comment;
import com.blog.model.User;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Implementation of CommentDisplay that displays comments in a threaded format
 * with proper indentation for replies
 */
public class ThreadedCommentDisplay implements CommentDisplay {
    private final JPanel commentsPanel;
    private final BlogController blogController;
    private final UserController userController;
    private final DialogFactory dialogFactory;
    private final JComponent parent;
    private final int postId;
    
    /**
     * Constructor for ThreadedCommentDisplay
     * 
     * @param parent The parent component
     * @param blogController The blog controller instance
     * @param userController The user controller instance
     * @param postId The ID of the post
     */
    public ThreadedCommentDisplay(JComponent parent, BlogController blogController, 
                                 UserController userController, int postId) {
        this.parent = parent;
        this.blogController = blogController;
        this.userController = userController;
        this.postId = postId;
        this.dialogFactory = new StandardDialogFactory();
        
        // Initialize the comment panel
        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    @Override
    public void addComment(Comment comment) {
        JPanel commentPanel = createCommentPanel(comment, 0);
        commentsPanel.add(commentPanel);
        commentsPanel.add(Box.createVerticalStrut(10));
        
        // Add replies if they exist
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                addCommentWithReplies(commentsPanel, reply, 1);
            }
        }
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }
    
    @Override
    public void addReply(int parentCommentId, Comment reply) {
        // Find the parent comment panel
        int parentIndex = findCommentPanelIndex(parentCommentId);
        if (parentIndex < 0) {
            return; // Parent not found
        }
        
        // Create panel for the reply
        JPanel replyPanel = createCommentPanel(reply, calculateIndentLevel(parentCommentId));
        
        // Add the reply after the parent and any existing replies
        int insertIndex = findLastReplyIndex(parentCommentId, parentIndex) + 1;
        commentsPanel.add(replyPanel, insertIndex);
        commentsPanel.add(Box.createVerticalStrut(10), insertIndex + 1);
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }
    
    @Override
    public boolean removeComment(int commentId) {
        int index = findCommentPanelIndex(commentId);
        if (index < 0) {
            return false; // Comment not found
        }
        
        // Remove the comment and its spacer
        commentsPanel.remove(index);
        if (index < commentsPanel.getComponentCount()) {
            commentsPanel.remove(index); // Remove spacer
        }
        
        // Remove any replies
        boolean foundReplies = true;
        while (foundReplies && index < commentsPanel.getComponentCount()) {
            Component comp = commentsPanel.getComponent(index);
            if (comp instanceof JPanel && comp.getName() != null && 
                comp.getName().startsWith("comment-") &&
                isReplyToComment(commentId, comp.getName())) {
                commentsPanel.remove(index);
                if (index < commentsPanel.getComponentCount()) {
                    commentsPanel.remove(index); // Remove spacer
                }
            } else {
                foundReplies = false;
            }
        }
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
        return true;
    }
    
    @Override
    public void refreshComments(int postId) {
        commentsPanel.removeAll();
        
        // Get comments for this post with threaded replies
        List<Comment> comments = blogController.getCommentController().getCommentsForPost(postId);
        
        // Add all comments with their replies
        for (Comment comment : comments) {
            addCommentWithReplies(commentsPanel, comment, 0);
        }
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }
    
    @Override
    public JPanel getCommentsPanel() {
        return commentsPanel;
    }
    
    /**
     * Recursively add a comment and its replies
     * 
     * @param container The container to add comments to
     * @param comment The comment to add
     * @param indentLevel The current indent level
     */
    private void addCommentWithReplies(JPanel container, Comment comment, int indentLevel) {
        // Create and add the comment panel
        JPanel commentPanel = createCommentPanel(comment, indentLevel);
        container.add(commentPanel);
        container.add(Box.createVerticalStrut(10));
        
        // Recursively add replies if they exist
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                addCommentWithReplies(container, reply, indentLevel + 1);
            }
        }
    }
    
    /**
     * Create a panel for a single comment
     * 
     * @param comment The comment to display
     * @param indentLevel The indentation level (0 for top-level comments)
     * @return A JPanel containing the formatted comment
     */
    private JPanel createCommentPanel(Comment comment, int indentLevel) {
        // Create main panel with indentation based on comment level
        JPanel commentPanel = new JPanel(new BorderLayout(10, 5));
        commentPanel.setName("comment-" + comment.getId());
        
        // Calculate indent and set the appropriate left padding
        int indent = indentLevel * 20;
        
        // Create a colored indent bar based on the level
        Color indentColor;
        switch (indentLevel % 4) {
            case 1: indentColor = new Color(70, 130, 180, 200); break; // Steel blue
            case 2: indentColor = new Color(60, 179, 113, 180); break; // Medium sea green
            case 3: indentColor = new Color(255, 165, 0, 160); break;  // Orange
            default: indentColor = new Color(100, 100, 100, 140);      // Grey
        }
        
        // Apply indentation and border
        Border indentBorder = BorderFactory.createMatteBorder(0, indentLevel > 0 ? 3 : 0, 0, 0, indentColor);
        Border paddingBorder = BorderFactory.createEmptyBorder(5, indent + 5, 5, 5);
        commentPanel.setBorder(BorderFactory.createCompoundBorder(indentBorder, paddingBorder));
        
        // Set appropriate background color
        if (indentLevel > 0) {
            // Lighter background for replies
            float alpha = Math.max(0.05f, 0.15f - (indentLevel * 0.02f));
            commentPanel.setBackground(new Color(70, 130, 180, Math.round(alpha * 255)));
        } else {
            commentPanel.setBackground(new Color(245, 245, 250));
        }
        
        // Create avatar panel
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(40, 40));
        
        // Create avatar or initial circle
        JLabel avatarLabel = new JLabel(comment.getAuthor().getUsername().substring(0, 1).toUpperCase());
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Arial", Font.BOLD, 16));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(indentColor);
        avatarLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        avatarPanel.add(avatarLabel, BorderLayout.CENTER);
        
        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setOpaque(false);
        
        // Author and timestamp header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel authorLabel = new JLabel(comment.getAuthor().getUsername());
        authorLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(authorLabel, BorderLayout.WEST);
        
        // Format the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
        JLabel dateLabel = new JLabel(sdf.format(comment.getCreatedAt()));
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        dateLabel.setForeground(Color.GRAY);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Comment text
        JTextArea commentText = new JTextArea(comment.getContent());
        commentText.setEditable(false);
        commentText.setWrapStyleWord(true);
        commentText.setLineWrap(true);
        commentText.setOpaque(false);
        commentText.setFont(new Font("Arial", Font.PLAIN, 12));
        commentText.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        contentPanel.add(commentText, BorderLayout.CENTER);
        
        // Actions panel (Reply button, etc.)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setOpaque(false);
        
        // Only show reply button if user is logged in
        if (userController.isLoggedIn()) {
            JButton replyButton = new JButton("Reply");
            replyButton.setFont(new Font("Arial", Font.BOLD, 10));
            replyButton.setFocusPainted(false);
            
            replyButton.addActionListener(e -> {
                String replyText = dialogFactory.showCommentReplyDialog(
                    parent, comment.getAuthor().getUsername());
                
                if (replyText != null && !replyText.isEmpty()) {
                    boolean success = blogController.getCommentController()
                        .addReply(postId, comment.getId(), replyText);
                    
                    if (success) {
                        refreshComments(postId);
                    } else {
                        JOptionPane.showMessageDialog(
                            parent,
                            "Failed to add reply. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            });
            
            actionsPanel.add(replyButton);
        }
        
        // Allow admin or comment author to delete the comment
        if (userController.isLoggedIn()) {
            User currentUser = userController.getCurrentUser();
            if (currentUser.isAdmin() || currentUser.getId() == comment.getAuthor().getId()) {
                JButton deleteButton = new JButton("Delete");
                deleteButton.setFont(new Font("Arial", Font.BOLD, 10));
                deleteButton.setForeground(Color.RED);
                deleteButton.setFocusPainted(false);
                
                deleteButton.addActionListener(e -> {
                    boolean confirmDelete = dialogFactory.showConfirmDialog(
                        parent,
                        "Are you sure you want to delete this comment?",
                        "Confirm Deletion"
                    );
                    
                    if (confirmDelete) {
                        boolean deleted = blogController.getCommentController().deleteComment(comment.getId());
                        if (deleted) {
                            refreshComments(postId);
                        } else {
                            JOptionPane.showMessageDialog(
                                parent,
                                "Failed to delete comment.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                });
                
                actionsPanel.add(deleteButton);
            }
        }
        
        contentPanel.add(actionsPanel, BorderLayout.SOUTH);
        
        // Assemble the comment panel
        commentPanel.add(avatarPanel, BorderLayout.WEST);
        commentPanel.add(contentPanel, BorderLayout.CENTER);
        
        return commentPanel;
    }
    
    /**
     * Find the index of a comment panel by ID
     * 
     * @param commentId The comment ID to find
     * @return The index of the comment panel or -1 if not found
     */
    private int findCommentPanelIndex(int commentId) {
        String targetName = "comment-" + commentId;
        for (int i = 0; i < commentsPanel.getComponentCount(); i++) {
            Component comp = commentsPanel.getComponent(i);
            if (comp instanceof JPanel && targetName.equals(comp.getName())) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Find the index of the last reply to a comment
     * 
     * @param parentCommentId The parent comment ID
     * @param parentIndex The index of the parent comment panel
     * @return The index of the last reply or the parent index if no replies
     */
    private int findLastReplyIndex(int parentCommentId, int parentIndex) {
        int lastIndex = parentIndex;
        
        // Look for comments that are replies to this parent
        for (int i = parentIndex + 2; i < commentsPanel.getComponentCount(); i += 2) {
            if (i >= commentsPanel.getComponentCount()) break;
            
            Component comp = commentsPanel.getComponent(i);
            if (comp instanceof JPanel && comp.getName() != null && 
                comp.getName().startsWith("comment-") &&
                isReplyToComment(parentCommentId, comp.getName())) {
                lastIndex = i;
            } else {
                // Found a component that's not a reply to this comment
                break;
            }
        }
        
        return lastIndex;
    }
    
    /**
     * Check if a component is a reply to a specified comment
     * 
     * @param parentCommentId The parent comment ID
     * @param componentName The name of the component to check
     * @return true if the component is a reply to the parent
     */
    private boolean isReplyToComment(int parentCommentId, String componentName) {
        if (!componentName.startsWith("comment-")) {
            return false;
        }
        
        try {
            int commentId = Integer.parseInt(componentName.substring(8));
            Comment comment = blogController.getCommentController()
                .getCommentById(commentId)
                .orElse(null);
            
            return comment != null && comment.getParentId() == parentCommentId;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Calculate the indent level for a comment based on its parent
     * 
     * @param commentId The comment ID
     * @return The indent level (0 for top-level, 1+ for replies)
     */
    private int calculateIndentLevel(int commentId) {
        Comment comment = blogController.getCommentController()
            .getCommentById(commentId)
            .orElse(null);
        
        if (comment == null) return 0;
        
        // If it's a top-level comment, level is 0
        if (comment.getParentId() == 0) return 0;
        
        // Otherwise, it's the parent's level + 1
        return calculateIndentLevel(comment.getParentId()) + 1;
    }
} 