package com.blog.model;

import java.util.Date;

/**
 * Represents a like/dislike on a blog post from a user.
 */
public class Like {
    private int id;
    private int postId;
    private User user;
    private boolean isLike; // true for like, false for dislike
    private Date createdAt;
    
    /**
     * Create a new Like object
     * 
     * @param postId The ID of the post being liked/disliked
     * @param user The user who created the like/dislike
     * @param isLike True for a like, false for a dislike
     */
    public Like(int postId, User user, boolean isLike) {
        this.postId = postId;
        this.user = user;
        this.isLike = isLike;
        this.createdAt = new Date();
    }
    
    /**
     * Create a Like object from database data
     * 
     * @param id The like ID
     * @param postId The ID of the post being liked/disliked
     * @param user The user who created the like/dislike
     * @param isLike True for a like, false for a dislike
     * @param createdAt The date when the like was created
     */
    public Like(int id, int postId, User user, boolean isLike, Date createdAt) {
        this.id = id;
        this.postId = postId;
        this.user = user;
        this.isLike = isLike;
        this.createdAt = createdAt;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getPostId() {
        return postId;
    }
    
    public void setPostId(int postId) {
        this.postId = postId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isLike() {
        return isLike;
    }
    
    public void setLike(boolean like) {
        isLike = like;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", postId=" + postId +
                ", user=" + user.getUsername() +
                ", isLike=" + isLike +
                '}';
    }
    
    /**
     * Toggle the like/dislike state
     */
    public void toggleLikeState() {
        this.isLike = !this.isLike;
    }
} 