package edu.uca.registration.repository.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import edu.uca.registration.model.Course;
import edu.uca.registration.repository.CourseRepository;
import edu.uca.registration.util.Logger;

/**
 * CSV based implementation of CourseRepository.
 */
public class CsvCourseRepository implements CourseRepository {
    private final String filePath;
    private final Map<String, Course> courses;
    private final Logger logger;

    public CsvCourseRepository(String filePath, Logger logger) {
        this.filePath = filePath;
        this.courses = new LinkedHashMap<>();
        this.logger = logger;
        loadFromFile();
    }

    @Override
    public void save(Course course) {
        courses.put(course.getCode(), course);
        flush();
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return Optional.ofNullable(courses.get(code));
    }

    @Override
    public boolean exists(String code) {
        return courses.containsKey(code);
    }

    @Override
    public Collection<Course> findAll() {
        return new ArrayList<>(courses.values());
    }

    @Override
    public boolean delete(String code) {
        boolean existed = courses.remove(code) != null;
        if (existed) {
            flush();
        }
        return existed;
    }

    @Override
    public int count() {
        return courses.size();
    }

    @Override
    public void flush() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Course c : courses.values()) {
                pw.println(String.format("%s,%s,%d", 
                    c.getCode(), c.getTitle(), c.getCapacity()));
            }
            logger.debug("Saved " + courses.size() + " courses to " + filePath);
        } catch (IOException e) {
            logger.error("Failed to save courses: " + e.getMessage());
            throw new RuntimeException("Failed to persist courses", e);
        }
    }

    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("Course file not found, starting with empty repository");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    try {
                        int capacity = Integer.parseInt(parts[2]);
                        Course course = new Course(parts[0], parts[1], capacity);
                        courses.put(course.getCode(), course);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Skipping invalid course at line " + lineNum + ": " + e.getMessage());
                    }
                }
            }
            logger.info("Loaded " + courses.size() + " courses from " + filePath);
        } catch (IOException e) {
            logger.error("Failed to load courses: " + e.getMessage());
        }
    }
}