package com.blog.controller;

import com.blog.dao.LikeDAO;
import com.blog.factory.LikeFactory;
import com.blog.model.Like;
import com.blog.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing likes and dislikes
 */
public class LikeController {
    private final LikeDAO likeDAO;
    private final UserController userController;
    
    public LikeController(UserController userController) {
        this.likeDAO = new LikeDAO();
        this.userController = userController;
    }
    
    /**
     * Like a post
     */
    public boolean likePost(int postId) {
        return reactToPost(postId, true);
    }
    
    /**
     * Dislike a post
     */
    public boolean dislikePost(int postId) {
        return reactToPost(postId, false);
    }
    
    /**
     * React to a post (like or dislike)
     */
    private boolean reactToPost(int postId, boolean isLike) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        User currentUser = userController.getCurrentUser();
        
        // Check if user already has a reaction
        Optional<Like> existingReaction = likeDAO.getLikeByUserAndPost(currentUser.getId(), postId);
        
        if (existingReaction.isPresent()) {
            Like reaction = existingReaction.get();
            
            // If same reaction type, remove it (toggle off)
            if (reaction.isLike() == isLike) {
                return likeDAO.deleteLike(currentUser.getId(), postId);
            } else {
                // If different reaction type, update it
                reaction.setLike(isLike);
                return likeDAO.createLike(reaction);
            }
        } else {
            // Create new reaction
            Like newReaction = LikeFactory.createReaction(postId, currentUser, isLike);
            return likeDAO.createLike(newReaction);
        }
    }
    
    /**
     * Remove a reaction (like or dislike) from a post
     */
    public boolean removeReaction(int postId) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return false;
        }
        
        User currentUser = userController.getCurrentUser();
        return likeDAO.deleteLike(currentUser.getId(), postId);
    }
    
    /**
     * Get a user's reaction to a post
     * @return true if liked, false if disliked, null if no reaction
     */
    public Boolean getUserReaction(int postId) {
        // Check if user is logged in
        if (!userController.isLoggedIn()) {
            return null;
        }
        
        User currentUser = userController.getCurrentUser();
        Optional<Like> reaction = likeDAO.getLikeByUserAndPost(currentUser.getId(), postId);
        
        return reaction.map(Like::isLike).orElse(null);
    }
    
    /**
     * Get all likes for a post
     */
    public List<Like> getLikesForPost(int postId) {
        return likeDAO.getLikesForPost(postId);
    }
    
    /**
     * Count likes and dislikes for a post
     * @return int array where [0] is likes count and [1] is dislikes count
     */
    public int[] getLikeCountsForPost(int postId) {
        int[] counts = new int[2];
        counts[0] = likeDAO.countLikesForPost(postId);
        counts[1] = likeDAO.countDislikesForPost(postId);
        return counts;
    }
    
    /**
     * Get like count for a post
     */
    public int getLikeCount(int postId) {
        return likeDAO.countLikesForPost(postId);
    }
    
    /**
     * Get dislike count for a post
     */
    public int getDislikeCount(int postId) {
        return likeDAO.countDislikesForPost(postId);
    }
} 