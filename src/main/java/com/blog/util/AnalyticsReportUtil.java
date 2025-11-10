package com.blog.util;

import com.blog.service.AnalyticsService;
import com.blog.service.AnalyticsService.AnalyticsEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating analytics reports
 */
public class AnalyticsReportUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Private constructor to prevent instantiation
     */
    private AnalyticsReportUtil() {
    }
    
    /**
     * Print a simple analytics report to the console
     */
    public static void printSimpleReport() {
        AnalyticsService analyticsService = AnalyticsService.getInstance();
        Map<String, List<AnalyticsEvent>> allEvents = analyticsService.getAllEvents();
        
        System.out.println("\n===== ANALYTICS REPORT =====");
        System.out.println("Generated at: " + DATE_FORMAT.format(new Date()));
        System.out.println("---------------------------");
        
        if (allEvents.isEmpty()) {
            System.out.println("No events recorded.");
        } else {
            allEvents.forEach((eventType, events) -> {
                System.out.println(eventType.toUpperCase() + ": " + events.size() + " events");
            });
            
            System.out.println("---------------------------");
            
            // Print last 5 events
            System.out.println("RECENT EVENTS:");
            allEvents.values().stream()
                .flatMap(List::stream)
                .sorted((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()))
                .limit(5)
                .forEach(event -> {
                    System.out.println(
                        DATE_FORMAT.format(new Date(event.getTimestamp())) + 
                        " - " + 
                        event.getType() + 
                        ": " + 
                        event.getDetails()
                    );
                });
        }
        
        System.out.println("============================\n");
    }
} 