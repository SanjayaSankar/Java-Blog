package com.blog.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a comment on a blog post, with support for threaded replies.
 */
public class Comment {
    private int id;
    private String content;
    private Date createdAt;
    private User author;
    private int postId;
    private boolean hidden;
    
    // Fields for nested comments
    private Integer parentId;  // ID of parent comment, null if top-level
    private List<Comment> replies;  // List of reply comments
    private int level;  // Nesting level (0 for top-level, 1+ for replies)
    
    // Constructor for top-level comments
    public Comment(String content, User author, int postId) {
        this.content = content;
        this.author = author;
        this.postId = postId;
        this.createdAt = new Date();
        this.hidden = false;
        this.parentId = null;
        this.replies = new ArrayList<>();
        this.level = 0;
    }
    
    // Constructor for reply comments
    public Comment(String content, User author, int postId, int parentId, int level) {
        this(content, author, postId);
        this.parentId = parentId;
        this.level = level;
    }
    
    // Constructor for database loading
    public Comment(int id, String content, Date createdAt, User author, int postId, boolean hidden, Integer parentId) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.author = author;
        this.postId = postId;
        this.hidden = hidden;
        this.parentId = parentId;
        this.replies = new ArrayList<>();
        this.level = (parentId == null) ? 0 : 1;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public int getPostId() {
        return postId;
    }
    
    public void setPostId(int postId) {
        this.postId = postId;
    }
    
    public boolean isHidden() {
        return hidden;
    }
    
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    // Methods for nested comments
    public Integer getParentId() {
        return parentId;
    }
    
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    
    public List<Comment> getReplies() {
        return replies;
    }
    
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }
    
    public void addReply(Comment reply) {
        if (replies == null) {
            replies = new ArrayList<>();
        }
        replies.add(reply);
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public boolean isTopLevel() {
        return parentId == null;
    }
    
    public int getReplyCount() {
        return replies.size();
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", author=" + author.getUsername() +
                ", postId=" + postId +
                ", parentId=" + parentId +
                ", level=" + level +
                ", replies=" + replies.size() +
                '}';
    }
} 