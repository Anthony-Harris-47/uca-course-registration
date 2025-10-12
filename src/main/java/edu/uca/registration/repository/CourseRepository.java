package edu.uca.registration.repository;

import java.util.Collection;
import java.util.Optional;

import edu.uca.registration.model.Course;

//Repository interface for Course persistence operations.

public interface CourseRepository {
    
    /**
     * Save a course to the repository.
     * @param course the course to save
     */
    void save(Course course);
    
    /**
     * Find a course by code.
     * @param code the course code
     * @return Optional containing the course if found
     */
    Optional<Course> findByCode(String code);
    
    /**
     * Check if a course exists.
     * @param code the course code
     * @return true if course exists
     */
    boolean exists(String code);
    
    /**
     * Get all courses.
     * @return collection of all courses
     */
    Collection<Course> findAll();
    
    /**
     * Delete a course by code.
     * @param code the course code
     * @return true if deleted, false if not found
     */
    boolean delete(String code);
    
    /**
     * Get the total number of courses.
     * @return course count
     */
    int count();
    
    //Persist all changes to storage.
    
    void flush();
}