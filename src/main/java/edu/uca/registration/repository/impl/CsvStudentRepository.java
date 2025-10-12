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

import edu.uca.registration.model.Student;
import edu.uca.registration.repository.StudentRepository;
import edu.uca.registration.util.Logger;

/**
 * CSV based implementation of StudentRepository.
 */
public class CsvStudentRepository implements StudentRepository {
    private final String filePath;
    private final Map<String, Student> students;
    private final Logger logger;

    public CsvStudentRepository(String filePath, Logger logger) {
        this.filePath = filePath;
        this.students = new LinkedHashMap<>();
        this.logger = logger;
        loadFromFile();
    }

    @Override
    public void save(Student student) {
        students.put(student.getId(), student);
        flush();
    }

    @Override
    public Optional<Student> findById(String id) {
        return Optional.ofNullable(students.get(id));
    }

    @Override
    public boolean exists(String id) {
        return students.containsKey(id);
    }

    @Override
    public Collection<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    @Override
    public boolean delete(String id) {
        boolean existed = students.remove(id) != null;
        if (existed) {
            flush();
        }
        return existed;
    }

    @Override
    public int count() {
        return students.size();
    }

    @Override
    public void flush() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Student s : students.values()) {
                pw.println(String.format("%s,%s,%s", 
                    s.getId(), s.getName(), s.getEmail()));
            }
            logger.debug("Saved " + students.size() + " students to " + filePath);
        } catch (IOException e) {
            logger.error("Failed to save students: " + e.getMessage());
            throw new RuntimeException("Failed to persist students", e);
        }
    }

    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("Student file not found, starting with empty repository");
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
                        Student student = new Student(parts[0], parts[1], parts[2]);
                        students.put(student.getId(), student);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Skipping invalid student at line " + lineNum + ": " + e.getMessage());
                    }
                }
            }
            logger.info("Loaded " + students.size() + " students from " + filePath);
        } catch (IOException e) {
            logger.error("Failed to load students: " + e.getMessage());
        }
    }
}