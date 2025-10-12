package edu.uca.registration.app;

import java.util.Collection;
import java.util.Scanner;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Student;
import edu.uca.registration.service.EnrollmentException;
import edu.uca.registration.service.RegistrationService;
import edu.uca.registration.service.RegistrationService.CourseEnrollmentInfo;
import edu.uca.registration.service.RegistrationService.DropResult;
import edu.uca.registration.service.RegistrationService.EnrollmentResult;

/**
 * Command-line interface for the course registration system.
 * Handles user input/output and delegates business logic to RegistrationService.
 */
public class CliApplication {
    private final RegistrationService service;
    private final Scanner scanner;

    public CliApplication(RegistrationService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        displayWelcome();
        menuLoop();
        displayGoodbye();
    }

    private void displayWelcome() {
        println("=== UCA Course Registration System ===");
        println("Refactored with clean architecture and SOLID principles");
    }

    private void displayGoodbye() {
        println("\nGoodbye!");
        scanner.close();
    }

    private void menuLoop() {
        while (true) {
            displayMenu();
            String choice = readInput("Choose: ").trim();
            
            try {
                if (!handleMenuChoice(choice)) {
                    break; // Exit
                }
            } catch (Exception e) {
                println("Error: " + e.getMessage());
            }
        }
    }

    private void displayMenu() {
        println("\nMenu:");
        println("1) Add student");
        println("2) Add course");
        println("3) Enroll student in course");
        println("4) Drop student from course");
        println("5) List students");
        println("6) List courses");
        println("0) Exit");
    }

    private boolean handleMenuChoice(String choice) {
        switch (choice) {
            case "1":
                addStudentUI();
                return true;
            case "2":
                addCourseUI();
                return true;
            case "3":
                enrollUI();
                return true;
            case "4":
                dropUI();
                return true;
            case "5":
                listStudents();
                return true;
            case "6":
                listCourses();
                return true;
            case "0":
                return false;
            default:
                println("Invalid choice. Please try again.");
                return true;
        }
    }

    private void addStudentUI() {
        try {
            String id = readInput("Banner ID: ").trim();
            String name = readInput("Name: ").trim();
            String email = readInput("Email: ").trim();
            
            service.addStudent(id, name, email);
            println("Student added successfully.");
        } catch (EnrollmentException e) {
            println("Error: " + e.getMessage());
        }
    }

    private void addCourseUI() {
        try {
            String code = readInput("Course Code: ").trim();
            String title = readInput("Title: ").trim();
            String capStr = readInput("Capacity: ").trim();
            
            int capacity = Integer.parseInt(capStr);
            service.addCourse(code, title, capacity);
            println("Course added successfully.");
        } catch (NumberFormatException e) {
            println("Error: Capacity must be a valid number.");
        } catch (EnrollmentException e) {
            println("Error: " + e.getMessage());
        }
    }

    private void enrollUI() {
        try {
            String studentId = readInput("Student ID: ").trim();
            String courseCode = readInput("Course Code: ").trim();
            
            EnrollmentResult result = service.enrollStudent(studentId, courseCode);
            println(result.getMessage());
        } catch (EnrollmentException e) {
            println("Error: " + e.getMessage());
        }
    }

    private void dropUI() {
        try {
            String studentId = readInput("Student ID: ").trim();
            String courseCode = readInput("Course Code: ").trim();
            
            DropResult result = service.dropStudent(studentId, courseCode);
            println(result.getMessage());
        } catch (EnrollmentException e) {
            println("Error: " + e.getMessage());
        }
    }

    private void listStudents() {
        Collection<Student> students = service.getAllStudents();
        println("\nStudents:");
        if (students.isEmpty()) {
            println("  (none)");
        } else {
            for (Student s : students) {
                println("  - " + s);
            }
        }
    }

    private void listCourses() {
        try {
            Collection<Course> courses = service.getAllCourses();
            println("\nCourses:");
            if (courses.isEmpty()) {
                println("  (none)");
            } else {
                for (Course c : courses) {
                    CourseEnrollmentInfo info = service.getCourseEnrollmentInfo(c.getCode());
                    println("  - " + info.getDisplayString());
                }
            }
        } catch (EnrollmentException e) {
            println("Error: " + e.getMessage());
        }
    }

    private String readInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private void println(String message) {
        System.out.println(message);
    }
}