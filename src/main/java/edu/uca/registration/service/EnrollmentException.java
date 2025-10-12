package edu.uca.registration.service;

//Exception thrown when enrollment operations fail.

public class EnrollmentException extends Exception {
    
    public EnrollmentException(String message) {
        super(message);
    }
    
    public EnrollmentException(String message, Throwable cause) {
        super(message, cause);
    }
}