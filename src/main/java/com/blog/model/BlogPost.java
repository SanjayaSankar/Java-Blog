package com.blog.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogPost {
    private int id;
    private String title;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private User author;
    private String status;       // "draft", "published", "archived"
    private String tags;         // Comma-separated tags
    private List<Media> mediaAttachments;
    private List<Comment> comments;
    private List<Like> likes;
    private int commentCount;    // Cache for comment count
    private int likeCount;       // Cache for likes count
    private int dislikeCount;    // Cache for dislikes count
    private int viewCount;       // Count of post views

    public BlogPost() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = "draft";
        this.mediaAttachments = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.likes = new ArrayList<>();
        this.commentCount = 0;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.viewCount = 0;
    }

    public BlogPost(String title, String content, User author) {
        this();
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public BlogPost(int id, String title, String content, Date createdAt, User author) {
        this();
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.author = author;
    }
    
    public BlogPost(int id, String title, String content, Date createdAt, 
                   Date updatedAt, User author, String status, String tags) {
        this(id, title, content, createdAt, author);
        this.updatedAt = updatedAt;
        this.status = status;
        this.tags = tags;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
    
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public List<Media> getMediaAttachments() {
        return mediaAttachments;
    }

    public void setMediaAttachments(List<Media> mediaAttachments) {
        this.mediaAttachments = mediaAttachments;
    }
    
    public void addMediaAttachment(Media media) {
        if (this.mediaAttachments == null) {
            this.mediaAttachments = new ArrayList<>();
        }
        this.mediaAttachments.add(media);
    }
    
    public boolean removeMediaAttachment(int mediaId) {
        if (this.mediaAttachments == null) {
            return false;
        }
        return this.mediaAttachments.removeIf(media -> media.getId() == mediaId);
    }
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
        updateCommentCount();
    }
    
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
        this.commentCount++;
    }
    
    public boolean removeComment(int commentId) {
        if (this.comments == null) {
            return false;
        }
        boolean removed = this.comments.removeIf(comment -> comment.getId() == commentId);
        if (removed) {
            this.commentCount--;
        }
        return removed;
    }
    
    public List<Like> getLikes() {
        return likes;
    }
    
    public void setLikes(List<Like> likes) {
        this.likes = likes;
        updateLikeCounts();
    }
    
    public void addLike(Like like) {
        if (this.likes == null) {
            this.likes = new ArrayList<>();
        }
        // Check if the user already liked/disliked the post
        for (int i = 0; i < this.likes.size(); i++) {
            Like existingLike = this.likes.get(i);
            if (existingLike.getUser().getId() == like.getUser().getId()) {
                // Replace the existing like with the new one
                this.likes.set(i, like);
                updateLikeCounts();
                return;
            }
        }
        // If no existing like found, add the new one
        this.likes.add(like);
        if (like.isLike()) {
            this.likeCount++;
        } else {
            this.dislikeCount++;
        }
    }
    
    public boolean removeLike(int userId) {
        if (this.likes == null) {
            return false;
        }
        for (int i = 0; i < this.likes.size(); i++) {
            Like like = this.likes.get(i);
            if (like.getUser().getId() == userId) {
                this.likes.remove(i);
                if (like.isLike()) {
                    this.likeCount--;
                } else {
                    this.dislikeCount--;
                }
                return true;
            }
        }
        return false;
    }
    
    public int getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
    public int getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
    
    public int getDislikeCount() {
        return dislikeCount;
    }
    
    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }
    
    /**
     * Get the view count for this post
     */
    public int getViewCount() {
        return viewCount;
    }

    /**
     * Set the view count for this post
     */
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * Increment the view count by 1
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    /**
     * Returns the list of tags as a String array
     */
    public String[] getTagsArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }
    
    /**
     * Returns true if the post is published
     */
    public boolean isPublished() {
        return "published".equals(status);
    }
    
    /**
     * Returns true if the post is a draft
     */
    public boolean isDraft() {
        return "draft".equals(status);
    }
    
    /**
     * Returns true if the post is archived
     */
    public boolean isArchived() {
        return "archived".equals(status);
    }
    
    /**
     * Update the comment count based on the comments list
     */
    private void updateCommentCount() {
        this.commentCount = (this.comments != null) ? this.comments.size() : 0;
    }
    
    /**
     * Update like and dislike counts based on the likes list
     */
    private void updateLikeCounts() {
        this.likeCount = 0;
        this.dislikeCount = 0;
        
        if (this.likes != null) {
            for (Like like : this.likes) {
                if (like.isLike()) {
                    this.likeCount++;
                } else {
                    this.dislikeCount++;
                }
            }
        }
    }
    
    /**
     * Check if a user has already liked or disliked this post
     */
    public boolean hasUserReaction(int userId) {
        if (this.likes == null) {
            return false;
        }
        
        for (Like like : this.likes) {
            if (like.getUser().getId() == userId) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the user's reaction (like or dislike) to this post
     * @return true if liked, false if disliked, null if no reaction
     */
    public Boolean getUserReaction(int userId) {
        if (this.likes == null) {
            return null;
        }
        
        for (Like like : this.likes) {
            if (like.getUser().getId() == userId) {
                return like.isLike();
            }
        }
        
        return null;
    }
} 