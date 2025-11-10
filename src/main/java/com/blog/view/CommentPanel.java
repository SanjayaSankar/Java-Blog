package com.blog.view;

import com.blog.controller.CommentController;
import com.blog.model.Comment;
import com.blog.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel for displaying and managing comments on a blog post
 */
public class CommentPanel extends JPanel {
    private final BlogApp app;
    private final int postId;
    private JPanel commentsListPanel;
    private JTextArea newCommentArea;
    private JButton submitButton;
    private JScrollPane commentsScrollPane;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    /**
     * Create a new comment panel for the specified post
     */
    public CommentPanel(BlogApp app, int postId) {
        this.app = app;
        this.postId = postId;
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Comments section header
        JLabel headerLabel = new JLabel("Comments");
        headerLabel.setFont(new Font(headerLabel.getFont().getName(), Font.BOLD, 16));
        add(headerLabel, BorderLayout.NORTH);
        
        // Comments list panel with scroll
        commentsListPanel = new JPanel();
        commentsListPanel.setLayout(new BoxLayout(commentsListPanel, BoxLayout.Y_AXIS));
        
        commentsScrollPane = new JScrollPane(commentsListPanel);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        commentsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(commentsScrollPane, BorderLayout.CENTER);
        
        // New comment panel
        JPanel newCommentPanel = new JPanel(new BorderLayout(0, 5));
        newCommentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel addCommentLabel = new JLabel("Add a comment:");
        addCommentLabel.setFont(new Font(addCommentLabel.getFont().getName(), Font.PLAIN, 12));
        newCommentPanel.add(addCommentLabel, BorderLayout.NORTH);
        
        newCommentArea = new JTextArea(3, 10);
        newCommentArea.setLineWrap(true);
        newCommentArea.setWrapStyleWord(true);
        JScrollPane commentScrollPane = new JScrollPane(newCommentArea);
        commentScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        newCommentPanel.add(commentScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitComment();
            }
        });
        buttonPanel.add(submitButton);
        newCommentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(newCommentPanel, BorderLayout.SOUTH);
        
        // Load initial comments
        refreshComments();
    }
    
    /**
     * Submit a new comment to the post
     */
    private void submitComment() {
        User currentUser = app.getUserController().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                "You must be logged in to add comments.",
                "Authentication Required",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String commentText = newCommentArea.getText().trim();
        if (commentText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Comment cannot be empty.",
                "Empty Comment",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CommentController commentController = app.getBlogController().getCommentController();
        boolean success = commentController.addComment(postId, commentText);
        
        if (success) {
            newCommentArea.setText("");
            refreshComments();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to add comment. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Refresh the comments list from the database
     */
    public void refreshComments() {
        commentsListPanel.removeAll();
        
        CommentController commentController = app.getBlogController().getCommentController();
        List<Comment> comments = commentController.getCommentsForPost(postId);
        
        if (comments.isEmpty()) {
            JLabel noCommentsLabel = new JLabel("No comments yet. Be the first to comment!");
            noCommentsLabel.setForeground(Color.GRAY);
            noCommentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            noCommentsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            commentsListPanel.add(noCommentsLabel);
        } else {
            for (Comment comment : comments) {
                JPanel commentPanel = createCommentPanel(comment);
                commentsListPanel.add(commentPanel);
                commentsListPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        commentsListPanel.revalidate();
        commentsListPanel.repaint();
    }
    
    /**
     * Create a panel to display a single comment
     */
    private JPanel createCommentPanel(Comment comment) {
        JPanel panel = new JPanel(new BorderLayout(5, 3));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            new EmptyBorder(5, 5, 10, 5)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Header with username and date
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel usernameLabel = new JLabel(comment.getAuthor().getUsername());
        usernameLabel.setFont(new Font(usernameLabel.getFont().getName(), Font.BOLD, 12));
        headerPanel.add(usernameLabel, BorderLayout.WEST);
        
        JLabel dateLabel = new JLabel(dateFormat.format(comment.getCreatedAt()));
        dateLabel.setFont(new Font(dateLabel.getFont().getName(), Font.PLAIN, 10));
        dateLabel.setForeground(Color.GRAY);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Comment content
        JTextArea contentArea = new JTextArea(comment.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setOpaque(false);
        contentArea.setBorder(null);
        contentArea.setFont(new Font(contentArea.getFont().getName(), Font.PLAIN, 12));
        panel.add(contentArea, BorderLayout.CENTER);
        
        // Delete button (only shown for user's own comments or admin)
        User currentUser = app.getUserController().getCurrentUser();
        if (currentUser != null && 
            (currentUser.getId() == comment.getAuthor().getId() || currentUser.isAdmin())) {
            
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            JButton deleteButton = new JButton("Delete");
            deleteButton.setFont(new Font(deleteButton.getFont().getName(), Font.PLAIN, 10));
            deleteButton.setForeground(Color.RED);
            deleteButton.setBorderPainted(false);
            deleteButton.setContentAreaFilled(false);
            deleteButton.setFocusPainted(false);
            
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(CommentPanel.this,
                        "Are you sure you want to delete this comment?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        CommentController commentController = 
                            app.getBlogController().getCommentController();
                        boolean success = commentController.deleteComment(comment.getId());
                        
                        if (success) {
                            refreshComments();
                        } else {
                            JOptionPane.showMessageDialog(CommentPanel.this,
                                "Failed to delete comment. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            
            actionPanel.add(deleteButton);
            panel.add(actionPanel, BorderLayout.SOUTH);
        }
        
        return panel;
    }
} 