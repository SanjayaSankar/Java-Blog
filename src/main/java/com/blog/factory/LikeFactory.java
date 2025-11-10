package com.blog.factory;

import com.blog.model.Like;
import com.blog.model.User;

import java.util.Date;

/**
 * Factory class for creating different types of likes
 * Implementation of the Factory Method pattern
 */
public class LikeFactory {
    
    /**
     * Create a new like
     * 
     * @param postId The ID of the post being liked
     * @param user The user who created the like
     * @return A new Like object
     */
    public static Like createLike(int postId, User user) {
        return new Like(postId, user, true);
    }
    
    /**
     * Create a new dislike
     * 
     * @param postId The ID of the post being disliked
     * @param user The user who created the dislike
     * @return A new Like object with isLike=false
     */
    public static Like createDislike(int postId, User user) {
        return new Like(postId, user, false);
    }
    
    /**
     * Create a like or dislike based on the value
     * 
     * @param postId The ID of the post being liked/disliked
     * @param user The user who created the like/dislike
     * @param isLike True for a like, false for a dislike
     * @return A new Like object
     */
    public static Like createReaction(int postId, User user, boolean isLike) {
        return new Like(postId, user, isLike);
    }
    
    /**
     * Create a like from database data
     * 
     * @param id The like ID
     * @param postId The ID of the post being liked/disliked
     * @param user The user who created the like/dislike
     * @param isLike True for a like, false for a dislike
     * @param createdAt The date when the like was created
     * @return A new Like object
     */
    public static Like createFromDatabase(int id, int postId, User user, boolean isLike, Date createdAt) {
        return new Like(id, postId, user, isLike, createdAt);
    }
} 