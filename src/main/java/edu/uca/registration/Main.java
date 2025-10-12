package edu.uca.registration;

import edu.uca.registration.app.CliApplication;
import edu.uca.registration.repository.CourseRepository;
import edu.uca.registration.repository.EnrollmentRepository;
import edu.uca.registration.repository.StudentRepository;
import edu.uca.registration.repository.impl.CsvCourseRepository;
import edu.uca.registration.repository.impl.CsvEnrollmentRepository;
import edu.uca.registration.repository.impl.CsvStudentRepository;
import edu.uca.registration.service.EnrollmentException;
import edu.uca.registration.service.RegistrationService;
import edu.uca.registration.util.Config;
import edu.uca.registration.util.Logger;

/**
 * Main entry point for the UCA Course Registration System.
 * Handles dependency injection and application initialization.
 */
public class Main {
    
    public static void main(String[] args) {
        // Load configuration
        Config config = new Config();
        Logger logger = new Logger("Main", config.getLogLevel());
        
        // Check for demo mode
        boolean demoMode = args.length > 0 && "--demo".equalsIgnoreCase(args[0]);
        
        // Initialize repositories
        StudentRepository studentRepo = new CsvStudentRepository(
            config.getStudentsFile(), 
            new Logger("StudentRepo", config.getLogLevel())
        );
        
        CourseRepository courseRepo = new CsvCourseRepository(
            config.getCoursesFile(),
            new Logger("CourseRepo", config.getLogLevel())
        );
        
        EnrollmentRepository enrollmentRepo = new CsvEnrollmentRepository(
            config.getEnrollmentsFile(),
            new Logger("EnrollmentRepo", config.getLogLevel())
        );
        
        // Initialize service
        RegistrationService service = new RegistrationService(
            studentRepo,
            courseRepo,
            enrollmentRepo,
            new Logger("RegistrationService", config.getLogLevel())
        );
        
        // Seed demo data if requested
        if (demoMode) {
            seedDemoData(service, logger);
        }
        
        // Start CLI application
        CliApplication app = new CliApplication(service);
        app.run();
    }
    
    private static void seedDemoData(RegistrationService service, Logger logger) {
        try {
            logger.info("Seeding demo data...");
            
            // Add demo students
            service.addStudent("B001", "Alice", "alice@uca.edu");
            service.addStudent("B002", "Brian", "brian@uca.edu");
            
            // Add demo courses
            service.addCourse("CSCI4490", "Software Engineering", 2);
            service.addCourse("MATH1496", "Calculus I", 50);
            
            logger.info("Demo data seeded successfully");
        } catch (EnrollmentException e) {
            logger.error("Failed to seed demo data: " + e.getMessage());
        }
    }
}