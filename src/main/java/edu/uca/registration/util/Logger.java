package edu.uca.registration.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//Simple logging utility for the application.

public class Logger {
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final String context;
    private final LogLevel minLevel;

    public enum LogLevel {
        DEBUG(0), INFO(1), WARN(2), ERROR(3);
        
        private final int priority;
        LogLevel(int priority) { this.priority = priority; }
        
        public boolean shouldLog(LogLevel minLevel) {
            return this.priority >= minLevel.priority;
        }
    }

    public Logger(String context) {
        this(context, LogLevel.INFO);
    }

    public Logger(String context, LogLevel minLevel) {
        this.context = context;
        this.minLevel = minLevel;
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    private void log(LogLevel level, String message) {
        if (level.shouldLog(minLevel)) {
            String timestamp = LocalDateTime.now().format(formatter);
            System.err.println(String.format("[%s] %s [%s] %s", 
                timestamp, level, context, message));
        }
    }
}