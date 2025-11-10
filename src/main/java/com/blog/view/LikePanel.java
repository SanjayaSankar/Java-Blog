package com.blog.view;

import com.blog.controller.LikeController;
import com.blog.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for displaying and managing likes on a blog post
 */
public class LikePanel extends JPanel {
    private final BlogApp app;
    private final int postId;
    private JLabel likesCountLabel;
    private JLabel dislikesCountLabel;
    private JButton likeButton;
    private JButton dislikeButton;
    
    /**
     * Create a new like panel for the specified post
     */
    public LikePanel(BlogApp app, int postId) {
        this.app = app;
        this.postId = postId;
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Like button with counter
        likeButton = new JButton("👍");
        likeButton.setFont(new Font(likeButton.getFont().getName(), Font.PLAIN, 14));
        likeButton.setFocusPainted(false);
        
        likesCountLabel = new JLabel("0");
        likesCountLabel.setFont(new Font(likesCountLabel.getFont().getName(), Font.BOLD, 12));
        
        // Dislike button with counter
        dislikeButton = new JButton("👎");
        dislikeButton.setFont(new Font(dislikeButton.getFont().getName(), Font.PLAIN, 14));
        dislikeButton.setFocusPainted(false);
        
        dislikesCountLabel = new JLabel("0");
        dislikesCountLabel.setFont(new Font(dislikesCountLabel.getFont().getName(), Font.BOLD, 12));
        
        // Add action listeners
        likeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLike(true);
            }
        });
        
        dislikeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLike(false);
            }
        });
        
        // Add components to panel
        add(likeButton);
        add(likesCountLabel);
        add(Box.createHorizontalStrut(10));
        add(dislikeButton);
        add(dislikesCountLabel);
        
        // Load initial like state
        refreshLikeState();
    }
    
    /**
     * Toggle like or dislike for the current post
     */
    private void toggleLike(boolean isLike) {
        User currentUser = app.getUserController().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                "You must be logged in to like or dislike posts.",
                "Authentication Required",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        LikeController likeController = app.getBlogController().getLikeController();
        Boolean userReaction = likeController.getUserReaction(postId);
        boolean userLiked = userReaction != null && userReaction;
        boolean userDisliked = userReaction != null && !userReaction;
        
        if (isLike) {
            // Handle like button click
            if (userLiked) {
                // Remove like if already liked
                likeController.removeReaction(postId);
            } else {
                // Add like
                likeController.likePost(postId);
            }
        } else {
            // Handle dislike button click
            if (userDisliked) {
                // Remove dislike if already disliked
                likeController.removeReaction(postId);
            } else {
                // Add dislike
                likeController.dislikePost(postId);
            }
        }
        
        refreshLikeState();
    }
    
    /**
     * Refresh the UI to show current like/dislike state
     */
    public void refreshLikeState() {
        LikeController likeController = app.getBlogController().getLikeController();
        
        // Update like/dislike counts
        int likeCount = likeController.getLikeCount(postId);
        int dislikeCount = likeController.getDislikeCount(postId);
        
        likesCountLabel.setText(String.valueOf(likeCount));
        dislikesCountLabel.setText(String.valueOf(dislikeCount));
        
        // Update button appearance based on current user's state
        User currentUser = app.getUserController().getCurrentUser();
        if (currentUser != null) {
            Boolean userReaction = likeController.getUserReaction(postId);
            boolean userLiked = userReaction != null && userReaction;
            boolean userDisliked = userReaction != null && !userReaction;
            
            // Highlight the button if user has already liked/disliked
            likeButton.setForeground(userLiked ? new Color(0, 150, 0) : Color.BLACK);
            dislikeButton.setForeground(userDisliked ? new Color(150, 0, 0) : Color.BLACK);
            
            likeButton.setToolTipText(userLiked ? "Remove Like" : "Like this post");
            dislikeButton.setToolTipText(userDisliked ? "Remove Dislike" : "Dislike this post");
        } else {
            // Reset to default if no user is logged in
            likeButton.setForeground(Color.BLACK);
            dislikeButton.setForeground(Color.BLACK);
            
            likeButton.setToolTipText("Login to like this post");
            dislikeButton.setToolTipText("Login to dislike this post");
        }
    }
} 