package com.blog.view;

import com.blog.model.BlogPost;
import com.blog.model.Comment;
import com.blog.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * Panel for viewing a full blog post
 */
public class PostView extends BasePanel {
    private final int postId;
    private BlogPost post;
    
    private JPanel contentPanel;
    private JButton backButton;
    private JPanel commentContainer;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
    private final DialogFactory dialogFactory;

    public PostView(BlogApp app, int postId) {
        super(app);
        this.postId = postId;
        this.dialogFactory = new StandardDialogFactory();
        
        System.out.println("DEBUG: PostView constructor called for postId=" + postId);
        
        // Load post data
        loadPost();
        
        // Initialize UI components
        initComponents();
    }
    
    private void loadPost() {
        System.out.println("DEBUG: loadPost() called for postId=" + postId);
        Optional<BlogPost> postOpt = app.getBlogController().getPostByIdAndIncrementViews(postId);
        if (postOpt.isPresent()) {
            post = postOpt.get();
            System.out.println("DEBUG: Post loaded successfully: " + post.getTitle());
            
            // Debug more post information
            System.out.println("DEBUG: Post author: " + post.getAuthor().getUsername());
            System.out.println("DEBUG: Post content length: " + post.getContent().length());
            System.out.println("DEBUG: Post media attachments: " + 
                (post.getMediaAttachments() != null ? post.getMediaAttachments().size() : 0));
            System.out.println("DEBUG: Post view count: " + post.getViewCount());
        } else {
            // Post not found
            System.out.println("DEBUG: ERROR - Post not found for postId=" + postId);
            post = null;
        }
    }
    
    @Override
    protected void initComponents() {
        System.out.println("DEBUG: initComponents() starting for postId=" + postId);
        setLayout(new BorderLayout(0, 10));
        
        // Create header panel with back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 245));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        backButton = new JButton("← Back to Posts");
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> app.showBlogPanel());
        headerPanel.add(backButton, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Post content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        if (post == null) {
            contentPanel.add(new JLabel("Post not found"));
        } else {
            // Post title
            JLabel titleLabel = new JLabel(post.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(titleLabel);
            
            contentPanel.add(Box.createVerticalStrut(5));
            
            // Author and date
            JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            metaPanel.setOpaque(false);
            
            JLabel authorLabel = new JLabel("By " + post.getAuthor().getUsername());
            authorLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            metaPanel.add(authorLabel);
            
            metaPanel.add(Box.createHorizontalStrut(10));
            
            JLabel dateLabel = new JLabel("Posted on " + dateFormat.format(post.getCreatedAt()));
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            metaPanel.add(dateLabel);
            
            metaPanel.add(Box.createHorizontalStrut(15));
            
            // View count
            JLabel viewCountLabel = new JLabel("👁 " + post.getViewCount() + " views");
            viewCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            metaPanel.add(viewCountLabel);
            
            contentPanel.add(metaPanel);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            // Tags
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                tagsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                tagsPanel.setOpaque(false);
                
                JLabel tagsLabel = new JLabel("Tags: ");
                tagsLabel.setFont(new Font("Arial", Font.BOLD, 12));
                tagsPanel.add(tagsLabel);
                
                for (String tag : post.getTagsArray()) {
                    JLabel tagLabel = new JLabel(tag);
                    tagLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    tagLabel.setOpaque(true);
                    tagLabel.setBackground(new Color(230, 230, 250));
                    tagLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                    tagsPanel.add(tagLabel);
                }
                
                contentPanel.add(tagsPanel);
                contentPanel.add(Box.createVerticalStrut(15));
            }
            
            // Separator
            JSeparator separator = new JSeparator();
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(separator);
            contentPanel.add(Box.createVerticalStrut(15));
            
            // Post content
            JTextArea contentArea = new JTextArea(post.getContent());
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setOpaque(false);
            contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
            contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(contentArea);
            
            contentPanel.add(Box.createVerticalStrut(15));
            
            // Like section
            JPanel likeSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
            likeSection.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Get current counts from controllers
            int likeCount = app.getBlogController().getLikeCount(postId);
            int dislikeCount = app.getBlogController().getDislikeCount(postId);
            
            JButton likeButton = new JButton("👍 Like (" + likeCount + ")");
            likeButton.addActionListener(e -> {
                app.getBlogController().likePost(postId);
                refreshContent();
            });
            likeSection.add(likeButton);
            
            JButton dislikeButton = new JButton("👎 Dislike (" + dislikeCount + ")");
            dislikeButton.addActionListener(e -> {
                app.getBlogController().dislikePost(postId);
                refreshContent();
            });
            likeSection.add(dislikeButton);
            
            contentPanel.add(likeSection);
            contentPanel.add(Box.createVerticalStrut(20));
            
            // YouTube-style Comment section
            int commentCount = app.getBlogController().getCommentCount(postId);
            JLabel commentsSectionLabel = new JLabel("Comments (" + commentCount + ")");
            commentsSectionLabel.setFont(new Font("Arial", Font.BOLD, 18));
            commentsSectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(commentsSectionLabel);
            
            contentPanel.add(Box.createVerticalStrut(15));
            
            // Add comment section
            JPanel addCommentPanel = new JPanel();
            addCommentPanel.setLayout(new BorderLayout(10, 5));
            addCommentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            addCommentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            
            // User avatar/icon
            JPanel avatarPanel = new JPanel();
            avatarPanel.setPreferredSize(new Dimension(40, 40));
            avatarPanel.setBackground(new Color(70, 130, 180));
            
            // Add user initials to avatar
            User currentUser = app.getUserController().getCurrentUser();
            if (currentUser != null) {
                JLabel initialsLabel = new JLabel(String.valueOf(currentUser.getUsername().charAt(0)).toUpperCase());
                initialsLabel.setForeground(Color.WHITE);
                initialsLabel.setFont(new Font("Arial", Font.BOLD, 16));
                avatarPanel.add(initialsLabel);
            }
            
            addCommentPanel.add(avatarPanel, BorderLayout.WEST);
            
            // Comment input
            JTextArea commentTextArea = new JTextArea();
            commentTextArea.setLineWrap(true);
            commentTextArea.setWrapStyleWord(true);
            commentTextArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            commentTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
            commentTextArea.setRows(3);
            
            JPanel textAreaContainer = new JPanel(new BorderLayout());
            textAreaContainer.add(commentTextArea, BorderLayout.CENTER);
            
            JButton addCommentButton = new JButton("Comment");
            addCommentButton.addActionListener(e -> {
                String comment = commentTextArea.getText().trim();
                if (!comment.isEmpty()) {
                    app.getBlogController().addComment(postId, comment);
                    commentTextArea.setText("");
                    refreshContent();
                }
            });
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(addCommentButton);
            
            textAreaContainer.add(buttonPanel, BorderLayout.SOUTH);
            addCommentPanel.add(textAreaContainer, BorderLayout.CENTER);
            
            addCommentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addCommentPanel.getPreferredSize().height));
            contentPanel.add(addCommentPanel);
            
            contentPanel.add(Box.createVerticalStrut(15));
            
            // Separator before comments
            JSeparator commentsSeparator = new JSeparator();
            commentsSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(commentsSeparator);
            contentPanel.add(Box.createVerticalStrut(10));
            
            // List of comments
            List<Comment> comments = app.getBlogController().getCommentController().getCommentsForPost(postId);
            if (comments.isEmpty()) {
                JLabel noCommentsLabel = new JLabel("No comments yet. Be the first to comment!");
                noCommentsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                noCommentsLabel.setForeground(Color.GRAY);
                noCommentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(noCommentsLabel);
                
                // Initialize the comments container even when empty
                commentContainer = new JPanel();
                commentContainer.setLayout(new BoxLayout(commentContainer, BoxLayout.Y_AXIS));
                commentContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(commentContainer);
            } else {
                // Add comment header
                JLabel commentsHeader = new JLabel("Comments (" + comments.size() + ")");
                commentsHeader.setFont(new Font("Arial", Font.BOLD, 16));
                commentsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(commentsHeader);
                contentPanel.add(Box.createVerticalStrut(10));
                
                // Initialize the comments container panel
                commentContainer = new JPanel();
                commentContainer.setLayout(new BoxLayout(commentContainer, BoxLayout.Y_AXIS));
                commentContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(commentContainer);
                
                // Display comments with their replies in a threaded structure
                for (Comment comment : comments) {
                    addCommentWithReplies(commentContainer, comment, 0);
                }
            }
            
            // Edit/Delete buttons if current user is author
            if (app.getUserController().isLoggedIn() && 
                (app.getUserController().getCurrentUser().getId() == post.getAuthor().getId() ||
                 app.getUserController().getCurrentUser().isAdmin())) {
                
                System.out.println("DEBUG: Showing edit/delete buttons for post ID: " + postId);
                System.out.println("DEBUG: Current user ID: " + app.getUserController().getCurrentUser().getId());
                System.out.println("DEBUG: Post author ID: " + post.getAuthor().getId());
                System.out.println("DEBUG: Current user role: " + app.getUserController().getCurrentUser().getRole());
                System.out.println("DEBUG: Is admin method result: " + app.getUserController().getCurrentUser().isAdmin());
                System.out.println("DEBUG: Is admin controller check: " + app.getUserController().isCurrentUserAdmin());
                
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JButton editButton = new JButton("Edit Post");
                editButton.addActionListener(e -> app.showEditPostEditor(postId));
                actionPanel.add(editButton);
                
                JButton deleteButton = new JButton("Delete Post");
                deleteButton.addActionListener(e -> {
                    if (dialogFactory.showConfirmDialog(
                            this, 
                            "Are you sure you want to delete this post?", 
                            "Confirm Delete")) {
                        boolean success = app.getBlogController().deletePost(postId);
                        if (success) {
                            app.showBlogPanel();
                        } else {
                            showErrorMessage("Failed to delete post");
                        }
                    }
                });
                actionPanel.add(deleteButton);
                
                contentPanel.add(Box.createVerticalStrut(15));
                contentPanel.add(actionPanel);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Recursively add a comment and its replies
     * @param container The container to add comments to
     * @param comment The comment to add
     * @param indentLevel The current indent level
     */
    private void addCommentWithReplies(JPanel container, Comment comment, int indentLevel) {
        // Create and add the comment panel
        JPanel commentPanel = createCommentPanel(comment, indentLevel);
        container.add(commentPanel);
        container.add(Box.createVerticalStrut(10));
        
        // Debug the comment structure
        System.out.println("DEBUG: Adding comment ID: " + comment.getId() + 
                          ", Level: " + indentLevel + 
                          ", Has replies: " + (comment.getReplies() != null ? comment.getReplies().size() : 0));
        
        // Recursively add replies if they exist
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment reply : comment.getReplies()) {
                addCommentWithReplies(container, reply, indentLevel + 1);
            }
        }
    }
    
    /**
     * Create a panel for a single comment
     * @param comment The comment to display
     * @param indentLevel The indentation level (0 for top-level comments)
     * @return A JPanel containing the formatted comment
     */
    private JPanel createCommentPanel(Comment comment, int indentLevel) {
        // Create main panel with indentation based on comment level
        JPanel commentPanel = new JPanel(new BorderLayout(10, 5));
        // Set a unique name for the comment panel to identify it later
        commentPanel.setName("comment-" + comment.getId());
        
        // Calculate indent and set the appropriate left padding
        int baseIndent = 10;
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
        commentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, indentLevel > 0 ? 3 : 0, 0, 0, indentColor),
                BorderFactory.createEmptyBorder(5, indent + 5, 5, 5)));
        
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
        if (app.getUserController().isLoggedIn()) {
            JButton replyButton = new JButton("Reply");
            replyButton.setFont(new Font("Arial", Font.BOLD, 10));
            replyButton.setFocusPainted(false);
            
            replyButton.addActionListener(e -> {
                System.out.println("DEBUG: Reply button clicked for comment ID: " + comment.getId());
                
                String replyText = dialogFactory.showCommentReplyDialog(this, comment.getAuthor().getUsername());
                
                if (replyText != null && !replyText.isEmpty()) {
                    System.out.println("DEBUG: Submitting reply to comment ID: " + comment.getId());
                    boolean success = app.getBlogController().getCommentController()
                        .addReply(postId, comment.getId(), replyText);
                    
                    if (success) {
                        System.out.println("DEBUG: Reply added successfully");
                        refreshContent();
                    } else {
                        showErrorMessage("Failed to add reply. Please try again.");
                    }
                }
            });
            
            actionsPanel.add(replyButton);
        }
        
        // Allow admin or comment author to delete the comment
        if (app.getUserController().isLoggedIn()) {
            User currentUser = app.getUserController().getCurrentUser();
            if (currentUser.isAdmin() || currentUser.getId() == comment.getAuthor().getId()) {
                JButton deleteButton = new JButton("Delete");
                deleteButton.setFont(new Font("Arial", Font.BOLD, 10));
                deleteButton.setForeground(Color.RED);
                deleteButton.setFocusPainted(false);
                
                deleteButton.addActionListener(e -> {
                    if (dialogFactory.showConfirmDialog(
                            this,
                            "Are you sure you want to delete this comment?",
                            "Confirm Deletion")) {
                        boolean deleted = app.getBlogController().getCommentController().deleteComment(comment.getId());
                        if (deleted) {
                            refreshContent();
                        } else {
                            showErrorMessage("Failed to delete comment.");
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
    
    @Override
    public void refreshContent() {
        loadPost();
        super.refreshContent();
    }

    /**
     * Refresh the comments section with the latest comments
     */
    private void refreshComments() {
        commentContainer.removeAll();
        
        // Get comments for this post with threaded replies
        List<Comment> comments = app.getBlogController().getCommentController().getCommentsForPost(postId);
        
        System.out.println("DEBUG: Number of top-level comments retrieved: " + comments.size());
        
        // Add all comments with their replies
        for (Comment comment : comments) {
            addCommentWithReplies(commentContainer, comment, 0);
        }
        
        // Debug the component structure
        System.out.println("DEBUG: CommentContainer components after adding comments:");
        Component[] components = commentContainer.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            System.out.println("DEBUG:   Component " + i + ": " + comp.getClass().getSimpleName() + 
                ", name=" + (comp.getName() != null ? comp.getName() : "null"));
        }
        
        // Add the comment form at the bottom if user is logged in
        if (app.getUserController().isLoggedIn()) {
            commentContainer.add(Box.createVerticalStrut(20));
            JPanel newCommentPanel = new JPanel(new BorderLayout(5, 5));
            newCommentPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Add a Comment"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            
            JTextArea commentText = new JTextArea(3, 30);
            commentText.setLineWrap(true);
            commentText.setWrapStyleWord(true);
            commentText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JScrollPane scrollPane = new JScrollPane(commentText);
            newCommentPanel.add(scrollPane, BorderLayout.CENTER);
            
            JButton submitButton = new JButton("Submit Comment");
            submitButton.setFont(new Font("Arial", Font.BOLD, 12));
            submitButton.setBackground(new Color(70, 130, 180)); // Steel blue
            submitButton.setForeground(Color.WHITE);
            submitButton.setFocusPainted(false);
            
            submitButton.addActionListener(e -> {
                String content = commentText.getText().trim();
                if (!content.isEmpty()) {
                    boolean success = app.getBlogController().getCommentController().addComment(
                            postId, content);
                    
                    if (success) {
                        commentText.setText("");
                        refreshContent();
                    } else {
                        showErrorMessage("Failed to add comment. Please try again.");
                    }
                } else {
                    showErrorMessage("Comment cannot be empty.");
                }
            });
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(submitButton);
            newCommentPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            commentContainer.add(newCommentPanel);
        }
        
        commentContainer.revalidate();
        commentContainer.repaint();
    }
} 