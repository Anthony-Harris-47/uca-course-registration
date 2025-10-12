package edu.uca.registration.repository;

import java.util.List;

import edu.uca.registration.model.Enrollment;
import edu.uca.registration.model.Enrollment.EnrollmentStatus;

//Repository interface for Enrollment persistence operations.

public interface EnrollmentRepository {
    
    /**
     * Save an enrollment.
     * @param enrollment the enrollment to save
     */
    void save(Enrollment enrollment);
    
    /**
     * Find all enrollments for a course.
     * @param courseCode the course code
     * @return list of enrollments for the course
     */
    List<Enrollment> findByCourseCode(String courseCode);
    
    /**
     * Find all enrollments for a student.
     * @param studentId the student ID
     * @return list of enrollments for the student
     */
    List<Enrollment> findByStudentId(String studentId);
    
    /**
     * Find enrollments by course and status.
     * @param courseCode the course code
     * @param status the enrollment status
     * @return list of matching enrollments (ordered by creation for FIFO)
     */
    List<Enrollment> findByCourseAndStatus(String courseCode, EnrollmentStatus status);
    
    /**
     * Check if an enrollment exists.
     * @param studentId the student ID
     * @param courseCode the course code
     * @return true if enrollment exists
     */
    boolean exists(String studentId, String courseCode);
    
    /**
     * Delete an enrollment.
     * @param studentId the student ID
     * @param courseCode the course code
     * @return true if deleted
     */
    boolean delete(String studentId, String courseCode);
    
    /**
     * Get count of enrollments for a course by status.
     * @param courseCode the course code
     * @param status the enrollment status
     * @return count of enrollments
     */
    int countByCourseAndStatus(String courseCode, EnrollmentStatus status);
    
    //Persist all changes to storage.
    
    void flush();
}