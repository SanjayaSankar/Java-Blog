package com.blog.service;

import com.blog.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for tracking analytics events
 */
public class AnalyticsService {
    // Event types
    public static final String EVENT_PAGE_VIEW = "page_view";
    public static final String EVENT_LOGIN = "login";
    public static final String EVENT_LOGOUT = "logout";
    public static final String EVENT_POST_VIEW = "post_view";
    public static final String EVENT_COMMENT = "comment";
    public static final String EVENT_REGISTRATION = "registration";
    
    private static AnalyticsService instance;
    
    private final Map<String, List<AnalyticsEvent>> events;
    
    private AnalyticsService() {
        events = new HashMap<>();
    }
    
    public static AnalyticsService getInstance() {
        if (instance == null) {
            instance = new AnalyticsService();
        }
        return instance;
    }
    
    /**
     * Track an analytics event with a simple detail string
     * @param eventType The type of event
     * @param details Additional details about the event
     */
    public void trackEvent(String eventType, String details) {
        AnalyticsEvent event = new AnalyticsEvent(eventType, details, System.currentTimeMillis());
        addEvent(eventType, event);
    }
    
    /**
     * Track an analytics event with a user and properties
     * @param eventType The type of event
     * @param user The user associated with the event
     * @param properties Properties of the event
     */
    public void trackEvent(String eventType, User user, Map<String, Object> properties) {
        String details = buildDetailsString(user, properties);
        AnalyticsEvent event = new AnalyticsEvent(eventType, details, System.currentTimeMillis());
        addEvent(eventType, event);
    }
    
    /**
     * Track an analytics event with a user and key-value pair
     * @param eventType The type of event
     * @param user The user associated with the event
     * @param key Property key
     * @param value Property value
     */
    public void trackEvent(String eventType, User user, String key, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(key, value);
        trackEvent(eventType, user, properties);
    }
    
    /**
     * Track an analytics event without a user
     * @param eventType The type of event
     * @param properties Properties of the event
     */
    public void trackEvent(String eventType, Map<String, Object> properties) {
        trackEvent(eventType, null, properties);
    }
    
    /**
     * Add an event to the events map
     * @param eventType The type of event
     * @param event The event to add
     */
    private void addEvent(String eventType, AnalyticsEvent event) {
        if (!events.containsKey(eventType)) {
            events.put(eventType, new ArrayList<>());
        }
        
        events.get(eventType).add(event);
    }
    
    /**
     * Build a details string from user and properties
     * @param user The user associated with the event
     * @param properties Properties of the event
     * @return A formatted details string
     */
    private String buildDetailsString(User user, Map<String, Object> properties) {
        StringBuilder details = new StringBuilder();
        
        if (user != null) {
            details.append("User: ").append(user.getUsername()).append(" (").append(user.getId()).append(")");
        } else {
            details.append("User: Anonymous");
        }
        
        if (properties != null && !properties.isEmpty()) {
            details.append(", Properties: {");
            boolean first = true;
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (!first) {
                    details.append(", ");
                }
                details.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            details.append("}");
        }
        
        return details.toString();
    }
    
    /**
     * Get all events of a specific type
     * @param eventType The type of events to retrieve
     * @return A list of events
     */
    public List<AnalyticsEvent> getEvents(String eventType) {
        return events.getOrDefault(eventType, new ArrayList<>());
    }
    
    /**
     * Get all tracked events
     * @return A map of event types to lists of events
     */
    public Map<String, List<AnalyticsEvent>> getAllEvents() {
        return events;
    }
    
    /**
     * Shutdown the analytics service
     */
    public void shutdown() {
        // No resources to release in this implementation
        System.out.println("Analytics service shutdown");
    }
    
    /**
     * Inner class representing an analytics event
     */
    public static class AnalyticsEvent {
        private final String type;
        private final String details;
        private final long timestamp;
        
        public AnalyticsEvent(String type, String details, long timestamp) {
            this.type = type;
            this.details = details;
            this.timestamp = timestamp;
        }
        
        public String getType() {
            return type;
        }
        
        public String getDetails() {
            return details;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
} 