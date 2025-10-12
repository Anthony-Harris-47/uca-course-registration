package edu.uca.registration.repository.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.uca.registration.model.Enrollment;
import edu.uca.registration.model.Enrollment.EnrollmentStatus;
import edu.uca.registration.repository.EnrollmentRepository;
import edu.uca.registration.util.Logger;

/**
 * CSV based implementation of EnrollmentRepository.
 * Maintains FIFO ordering for waitlists.
 */
public class CsvEnrollmentRepository implements EnrollmentRepository {
    private final String filePath;
    private final List<Enrollment> enrollments;
    private final Logger logger;

    public CsvEnrollmentRepository(String filePath, Logger logger) {
        this.filePath = filePath;
        this.enrollments = new ArrayList<>();
        this.logger = logger;
        loadFromFile();
    }

    @Override
    public void save(Enrollment enrollment) {
        // Remove existing enrollment for same student course pair
        delete(enrollment.getStudentId(), enrollment.getCourseCode());
        enrollments.add(enrollment);
        flush();
    }

    @Override
    public List<Enrollment> findByCourseCode(String courseCode) {
        return enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<Enrollment> findByStudentId(String studentId) {
        return enrollments.stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Enrollment> findByCourseAndStatus(String courseCode, EnrollmentStatus status) {
        return enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode) && e.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String studentId, String courseCode) {
        return enrollments.stream()
                .anyMatch(e -> e.getStudentId().equals(studentId) 
                            && e.getCourseCode().equals(courseCode));
    }

    @Override
    public boolean delete(String studentId, String courseCode) {
        boolean removed = enrollments.removeIf(e -> 
            e.getStudentId().equals(studentId) && e.getCourseCode().equals(courseCode));
        if (removed) {
            flush();
        }
        return removed;
    }

    @Override
    public int countByCourseAndStatus(String courseCode, EnrollmentStatus status) {
        return (int) enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode) && e.getStatus() == status)
                .count();
    }

    @Override
    public void flush() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Enrollment e : enrollments) {
                pw.println(String.format("%s|%s|%s", 
                    e.getCourseCode(), e.getStudentId(), e.getStatus()));
            }
            logger.debug("Saved " + enrollments.size() + " enrollments to " + filePath);
        } catch (IOException ex) {
            logger.error("Failed to save enrollments: " + ex.getMessage());
            throw new RuntimeException("Failed to persist enrollments", ex);
        }
    }

    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("Enrollment file not found, starting with empty repository");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    try {
                        String courseCode = parts[0];
                        String studentId = parts[1];
                        EnrollmentStatus status = EnrollmentStatus.valueOf(parts[2].toUpperCase());
                        enrollments.add(new Enrollment(studentId, courseCode, status));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Skipping invalid enrollment at line " + lineNum + ": " + e.getMessage());
                    }
                }
            }
            logger.info("Loaded " + enrollments.size() + " enrollments from " + filePath);
        } catch (IOException e) {
            logger.error("Failed to load enrollments: " + e.getMessage());
        }
    }
}