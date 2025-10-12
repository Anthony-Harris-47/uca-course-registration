package edu.uca.registration.repository;

import java.util.Collection;
import java.util.Optional;

import edu.uca.registration.model.Student;


//Repository interface for Student persistence operations.

public interface StudentRepository {
    
    /**
     * Save a student to the repository.
     * @param student the student to save
     */
    void save(Student student);
    
    /**
     * Find a student by ID.
     * @param id the student ID
     * @return Optional containing the student if found
     */
    Optional<Student> findById(String id);
    
    /**
     * Check if a student exists.
     * @param id the student ID
     * @return true if student exists
     */
    boolean exists(String id);
    
    /**
     * Get all students.
     * @return collection of all students
     */
    Collection<Student> findAll();
    
    /**
     * Delete a student by ID.
     * @param id the student ID
     * @return true if deleted, false if not found
     */
    boolean delete(String id);
    
    /**
     * Get the total number of students.
     * @return student count
     */
    int count();
    
    
     //Persist all changes to storage.
    
    void flush();
}