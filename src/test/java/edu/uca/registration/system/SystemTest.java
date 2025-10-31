package edu.uca.registration.system;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Enrollment;
import edu.uca.registration.model.Student;
import edu.uca.registration.repository.CourseRepository;
import edu.uca.registration.repository.EnrollmentRepository;
import edu.uca.registration.repository.StudentRepository;
import edu.uca.registration.service.EnrollmentException;
import edu.uca.registration.service.RegistrationService;
import edu.uca.registration.util.Logger;

/**
 * System tests validating complete workflows.
 * Test ID Prefix: ST (System Test)
 */
@DisplayName("System Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemTest {

    private RegistrationService service;
    private File tempDir;
    
    // Temporary file paths for testing
    private static final String TEST_STUDENTS_FILE = "test_students.csv";
    private static final String TEST_COURSES_FILE = "test_courses.csv";
    private static final String TEST_ENROLLMENTS_FILE = "test_enrollments.csv";

    @BeforeEach
    void setUp() {
        // Clean up any existing test files
        cleanupTestFiles();
        
        // Create service with in-memory repositories for isolation
        Logger logger = new Logger("SystemTest", Logger.LogLevel.ERROR);
        
        StudentRepository studentRepo = new InMemoryStudentRepository();
        CourseRepository courseRepo = new InMemoryCourseRepository();
        EnrollmentRepository enrollmentRepo = new InMemoryEnrollmentRepository();
        
        service = new RegistrationService(studentRepo, courseRepo, enrollmentRepo, logger);
    }

    @AfterEach
    void tearDown() {
        cleanupTestFiles();
    }

    private void cleanupTestFiles() {
        new File(TEST_STUDENTS_FILE).delete();
        new File(TEST_COURSES_FILE).delete();
        new File(TEST_ENROLLMENTS_FILE).delete();
    }

    // ==================== System Test: Complete Registration Workflow ====================

    @Test
    @Order(1)
    @DisplayName("ST-01: Complete workflow - Add students, courses, enroll, list")
    void testCompleteRegistrationWorkflow() throws EnrollmentException {
        // Step 1: Add students
        service.addStudent("B001", "Alice", "alice@uca.edu");
        service.addStudent("B002", "Bob", "bob@uca.edu");
        service.addStudent("B003", "Carol", "carol@uca.edu");
        
        // Verify students added
        assertEquals(3, service.getAllStudents().size());
        assertTrue(service.findStudent("B001").isPresent());
        
        // Step 2: Add courses
        service.addCourse("CSCI4490", "Software Engineering", 2);
        service.addCourse("MATH1496", "Calculus I", 50);
        
        // Verify courses added
        assertEquals(2, service.getAllCourses().size());
        assertTrue(service.findCourse("CSCI4490").isPresent());
        
        // Step 3: Enroll students
        var result1 = service.enrollStudent("B001", "CSCI4490");
        assertFalse(result1.isWaitlisted()); // Should be enrolled
        
        var result2 = service.enrollStudent("B002", "CSCI4490");
        assertFalse(result2.isWaitlisted()); // Should be enrolled (capacity 2)
        
        var result3 = service.enrollStudent("B003", "CSCI4490");
        assertTrue(result3.isWaitlisted()); // Should be waitlisted (full)
        
        // Step 4: Verify enrollment info
        var info = service.getCourseEnrollmentInfo("CSCI4490");
        assertEquals(2, info.getEnrolledCount());
        assertEquals(1, info.getWaitlistCount());
        
        // Step 5: Enroll in different course
        var result4 = service.enrollStudent("B001", "MATH1496");
        assertFalse(result4.isWaitlisted());
        
        // System test passed - complete workflow works correctly
    }

    @Test
    @Order(2)
    @DisplayName("ST-02: Waitlist promotion workflow")
    void testWaitlistPromotionWorkflow() throws EnrollmentException {
        // Setup: Create course with capacity 2
        service.addStudent("B001", "Alice", "alice@uca.edu");
        service.addStudent("B002", "Bob", "bob@uca.edu");
        service.addStudent("B003", "Carol", "carol@uca.edu");
        service.addStudent("B004", "David", "david@uca.edu");
        service.addCourse("CSCI4490", "Software Engineering", 2);
        
        // Step 1: Fill course and create waitlist
        service.enrollStudent("B001", "CSCI4490"); // Enrolled
        service.enrollStudent("B002", "CSCI4490"); // Enrolled
        service.enrollStudent("B003", "CSCI4490"); // Waitlisted
        service.enrollStudent("B004", "CSCI4490"); // Waitlisted
        
        // Verify initial state
        var info1 = service.getCourseEnrollmentInfo("CSCI4490");
        assertEquals(2, info1.getEnrolledCount());
        assertEquals(2, info1.getWaitlistCount());
        
        // Step 2: Drop first enrolled student
        var dropResult1 = service.dropStudent("B001", "CSCI4490");
        assertTrue(dropResult1.getPromotedStudentId().isPresent());
        assertEquals("B003", dropResult1.getPromotedStudentId().get()); // FIFO
        
        // Verify B003 promoted
        var info2 = service.getCourseEnrollmentInfo("CSCI4490");
        assertEquals(2, info2.getEnrolledCount());
        assertEquals(1, info2.getWaitlistCount());
        
        // Step 3: Drop another student
        var dropResult2 = service.dropStudent("B002", "CSCI4490");
        assertTrue(dropResult2.getPromotedStudentId().isPresent());
        assertEquals("B004", dropResult2.getPromotedStudentId().get()); // FIFO
        
        // Verify B004 promoted
        var info3 = service.getCourseEnrollmentInfo("CSCI4490");
        assertEquals(2, info3.getEnrolledCount());
        assertEquals(0, info3.getWaitlistCount());
    }

    @Test
    @Order(3)
    @DisplayName("ST-03: Multiple course enrollments per student")
    void testMultipleCourseEnrollments() throws EnrollmentException {
        // Setup
        service.addStudent("B001", "Alice", "alice@uca.edu");
        service.addCourse("CSCI4490", "Software Engineering", 30);
        service.addCourse("MATH1496", "Calculus I", 30);
        service.addCourse("ENGL1010", "English Composition", 30);
        
        // Enroll in multiple courses
        service.enrollStudent("B001", "CSCI4490");
        service.enrollStudent("B001", "MATH1496");
        service.enrollStudent("B001", "ENGL1010");
        
        // Verify enrollments
        var csci = service.getCourseEnrollmentInfo("CSCI4490");
        var math = service.getCourseEnrollmentInfo("MATH1496");
        var engl = service.getCourseEnrollmentInfo("ENGL1010");
        
        assertEquals(1, csci.getEnrolledCount());
        assertEquals(1, math.getEnrolledCount());
        assertEquals(1, engl.getEnrolledCount());
        
        // Drop one course
        service.dropStudent("B001", "MATH1496");
        var mathAfterDrop = service.getCourseEnrollmentInfo("MATH1496");
        assertEquals(0, mathAfterDrop.getEnrolledCount());
        
        // Verify other enrollments intact
        var csciAfterDrop = service.getCourseEnrollmentInfo("CSCI4490");
        assertEquals(1, csciAfterDrop.getEnrolledCount());
    }

    @Test
    @Order(4)
    @DisplayName("ST-04: Error handling throughout workflow")
    void testErrorHandlingInWorkflow() throws EnrollmentException {
        // Setup
        service.addStudent("B001", "Alice", "alice@uca.edu");
        service.addCourse("CSCI4490", "Software Engineering", 30);
        
        // Test 1: Duplicate student
        assertThrows(EnrollmentException.class,
            () -> service.addStudent("B001", "Bob", "bob@uca.edu"));
        
        // Test 2: Duplicate course
        assertThrows(EnrollmentException.class,
            () -> service.addCourse("CSCI4490", "Different Title", 50));
        
        // Test 3: Enroll non-existent student
        assertThrows(EnrollmentException.class,
            () -> service.enrollStudent("B999", "CSCI4490"));
        
        // Test 4: Enroll in non-existent course
        assertThrows(EnrollmentException.class,
            () -> service.enrollStudent("B001", "INVALID999"));
        
        // Test 5: Drop non-enrolled student
        assertThrows(EnrollmentException.class,
            () -> service.dropStudent("B001", "CSCI4490"));
        
        // Test 6: Enroll then try to enroll again
        service.enrollStudent("B001", "CSCI4490");
        assertThrows(EnrollmentException.class,
            () -> service.enrollStudent("B001", "CSCI4490"));
        
        // System remains consistent despite errors
        assertEquals(1, service.getAllStudents().size());
        assertEquals(1, service.getAllCourses().size());
    }

    @Test
    @Order(5)
    @DisplayName("ST-05: Large scale enrollment test")
    void testLargeScaleEnrollment() throws EnrollmentException {
        // Create large course
        service.addCourse("CSCI1101", "Intro to CS", 100);
        
        // Add many students
        for (int i = 1; i <= 150; i++) {
            String id = String.format("B%03d", i);
            String email = String.format("student%d@uca.edu", i);
            service.addStudent(id, "Student" + i, email);
        }
        
        // Enroll all students
        int enrolledCount = 0;
        int waitlistedCount = 0;
        
        for (int i = 1; i <= 150; i++) {
            String id = String.format("B%03d", i);
            var result = service.enrollStudent(id, "CSCI1101");
            if (result.isWaitlisted()) {
                waitlistedCount++;
            } else {
                enrolledCount++;
            }
        }
        
        // Verify counts
        assertEquals(100, enrolledCount);
        assertEquals(50, waitlistedCount);
        
        var info = service.getCourseEnrollmentInfo("CSCI1101");
        assertEquals(100, info.getEnrolledCount());
        assertEquals(50, info.getWaitlistCount());
    }

    @Test
    @Order(6)
    @DisplayName("ST-06: Boundary conditions test")
    void testBoundaryConditions() throws EnrollmentException {
        // Test minimum capacity course
        service.addCourse("MIN001", "Min Course", 1);
        service.addStudent("B001", "Alice", "alice@uca.edu");
        service.addStudent("B002", "Bob", "bob@uca.edu");
        
        service.enrollStudent("B001", "MIN001");
        var result = service.enrollStudent("B002", "MIN001");
        assertTrue(result.isWaitlisted());
        
        var info = service.getCourseEnrollmentInfo("MIN001");
        assertEquals(1, info.getEnrolledCount());
        assertEquals(1, info.getWaitlistCount());
        
        // Test maximum capacity course
        service.addCourse("MAX001", "Max Course", 500);
        var infoMax = service.getCourseEnrollmentInfo("MAX001");
        assertEquals(500, infoMax.getCourse().getCapacity());
    }

    // ==================== In-Memory Repository Implementations for Testing ====================
    
    static class InMemoryStudentRepository implements StudentRepository {
        private final Map<String, Student> students = new LinkedHashMap<>();
        
        public void save(Student student) { students.put(student.getId(), student); }
        public Optional<Student> findById(String id) { return Optional.ofNullable(students.get(id)); }
        public boolean exists(String id) { return students.containsKey(id); }
        public Collection<Student> findAll() { return new ArrayList<>(students.values()); }
        public boolean delete(String id) { return students.remove(id) != null; }
        public int count() { return students.size(); }
        public void flush() { }
    }
    
    static class InMemoryCourseRepository implements CourseRepository {
        private final Map<String, Course> courses = new LinkedHashMap<>();
        
        public void save(Course course) { courses.put(course.getCode(), course); }
        public Optional<Course> findByCode(String code) { return Optional.ofNullable(courses.get(code)); }
        public boolean exists(String code) { return courses.containsKey(code); }
        public Collection<Course> findAll() { return new ArrayList<>(courses.values()); }
        public boolean delete(String code) { return courses.remove(code) != null; }
        public int count() { return courses.size(); }
        public void flush() { }
    }
    
    static class InMemoryEnrollmentRepository implements EnrollmentRepository {
        private final List<Enrollment> enrollments = new ArrayList<>();
        
        public void save(Enrollment enrollment) {
            delete(enrollment.getStudentId(), enrollment.getCourseCode());
            enrollments.add(enrollment);
        }
        
        public List<Enrollment> findByCourseCode(String courseCode) {
            return enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode))
                .toList();
        }
        
        public List<Enrollment> findByStudentId(String studentId) {
            return enrollments.stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .toList();
        }
        
        public List<Enrollment> findByCourseAndStatus(String courseCode, Enrollment.EnrollmentStatus status) {
            return enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode) && e.getStatus() == status)
                .toList();
        }
        
        public boolean exists(String studentId, String courseCode) {
            return enrollments.stream()
                .anyMatch(e -> e.getStudentId().equals(studentId) && e.getCourseCode().equals(courseCode));
        }
        
        public boolean delete(String studentId, String courseCode) {
            return enrollments.removeIf(e -> 
                e.getStudentId().equals(studentId) && e.getCourseCode().equals(courseCode));
        }
        
        public int countByCourseAndStatus(String courseCode, Enrollment.EnrollmentStatus status) {
            return (int) enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode) && e.getStatus() == status)
                .count();
        }
        
        public void flush() { }
    }
}