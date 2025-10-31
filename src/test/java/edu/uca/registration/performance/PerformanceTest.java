package edu.uca.registration.performance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
 * Performance and non-functional tests.
 * Test ID Prefix: PT (Performance Test)
 */
@DisplayName("Performance Tests")
class PerformanceTest {

    private RegistrationService service;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = new Logger("PerformanceTest", Logger.LogLevel.ERROR);
        
        StudentRepository studentRepo = new InMemoryStudentRepository();
        CourseRepository courseRepo = new InMemoryCourseRepository();
        EnrollmentRepository enrollmentRepo = new InMemoryEnrollmentRepository();
        
        service = new RegistrationService(studentRepo, courseRepo, enrollmentRepo, logger);
    }

    @Test
    @DisplayName("PT-01: Add 1000 students performance test")
    void testAddManyStudentsPerformance() {
        long startTime = System.currentTimeMillis();
        
        // Add 1000 students
        for (int i = 1; i <= 1000; i++) {
            try {
                String id = String.format("B%04d", i);
                String email = String.format("student%d@uca.edu", i);
                service.addStudent(id, "Student" + i, email);
            } catch (EnrollmentException e) {
                fail("Should not throw exception: " + e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify all students added
        assertEquals(1000, service.getAllStudents().size());
        
        // Performance assertion - should complete in under 5 seconds
        assertTrue(duration < 5000, 
            String.format("Adding 1000 students took %dms (expected < 5000ms)", duration));
        
        System.out.println("PT-01: Added 1000 students in " + duration + "ms");
    }

    @Test
    @DisplayName("PT-02: Enroll 500 students in course performance test")
    void testEnrollManyStudentsPerformance() throws EnrollmentException {
        // Setup: Add students and course
        for (int i = 1; i <= 500; i++) {
            String id = String.format("B%04d", i);
            String email = String.format("student%d@uca.edu", i);
            service.addStudent(id, "Student" + i, email);
        }
        service.addCourse("PERF001", "Performance Test Course", 250);
        
        long startTime = System.currentTimeMillis();
        
        // Enroll all students
        for (int i = 1; i <= 500; i++) {
            String id = String.format("B%04d", i);
            service.enrollStudent(id, "PERF001");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify enrollments
        var info = service.getCourseEnrollmentInfo("PERF001");
        assertEquals(250, info.getEnrolledCount());
        assertEquals(250, info.getWaitlistCount());
        
        // Performance assertion - should complete in under 10 seconds
        assertTrue(duration < 10000,
            String.format("Enrolling 500 students took %dms (expected < 10000ms)", duration));
        
        System.out.println("PT-02: Enrolled 500 students in " + duration + "ms");
    }

    @Test
    @DisplayName("PT-03: Waitlist promotion cascade performance")
    void testWaitlistPromotionCascadePerformance() throws EnrollmentException {
        // Setup: Course with capacity 1, enroll 100 students
        service.addCourse("CASCADE001", "Cascade Test", 1);
        
        for (int i = 1; i <= 100; i++) {
            String id = String.format("B%04d", i);
            String email = String.format("student%d@uca.edu", i);
            service.addStudent(id, "Student" + i, email);
            service.enrollStudent(id, "CASCADE001");
        }
        
        // Verify initial state
        var infoBefore = service.getCourseEnrollmentInfo("CASCADE001");
        assertEquals(1, infoBefore.getEnrolledCount());
        assertEquals(99, infoBefore.getWaitlistCount());
        
        long startTime = System.currentTimeMillis();
        
        // Drop enrolled students one by one, triggering promotions
        for (int i = 1; i <= 50; i++) {
            String id = String.format("B%04d", i);
            service.dropStudent(id, "CASCADE001");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify final state
        var infoAfter = service.getCourseEnrollmentInfo("CASCADE001");
        assertEquals(1, infoAfter.getEnrolledCount());
        assertEquals(49, infoAfter.getWaitlistCount());
        
        // Performance assertion
        assertTrue(duration < 5000,
            String.format("50 drops with promotions took %dms (expected < 5000ms)", duration));
        
        System.out.println("PT-03: 50 drops with promotions in " + duration + "ms");
    }

    @Test
    @DisplayName("PT-04: Query performance with large dataset")
    void testQueryPerformanceWithLargeDataset() throws EnrollmentException {
        // Setup: Add many students and courses
        for (int i = 1; i <= 500; i++) {
            String id = String.format("B%04d", i);
            String email = String.format("student%d@uca.edu", i);
            service.addStudent(id, "Student" + i, email);
        }
        
        for (int i = 1; i <= 50; i++) {
            String code = String.format("COURSE%03d", i);
            service.addCourse(code, "Course " + i, 20);
        }
        
        // Enroll students in various courses
        for (int i = 1; i <= 500; i++) {
            String studentId = String.format("B%04d", i);
            String courseCode = String.format("COURSE%03d", (i % 50) + 1);
            service.enrollStudent(studentId, courseCode);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Perform many queries
        for (int i = 1; i <= 50; i++) {
            String courseCode = String.format("COURSE%03d", i);
            service.getCourseEnrollmentInfo(courseCode);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Performance assertion - queries should be fast
        assertTrue(duration < 1000,
            String.format("50 queries took %dms (expected < 1000ms)", duration));
        
        System.out.println("PT-04: 50 enrollment info queries in " + duration + "ms");
    }

    @Test
    @DisplayName("PT-05: Memory efficiency test")
    void testMemoryEfficiency() throws EnrollmentException {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Suggest garbage collection
        
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Add substantial data
        for (int i = 1; i <= 1000; i++) {
            String id = String.format("B%04d", i);
            String email = String.format("student%d@uca.edu", i);
            service.addStudent(id, "Student" + i, email);
        }
        
        for (int i = 1; i <= 100; i++) {
            String code = String.format("COURSE%03d", i);
            service.addCourse(code, "Course " + i, 50);
        }
        
        runtime.gc(); // Suggest garbage collection
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        
        long memoryUsed = (memoryAfter - memoryBefore) / 1024 / 1024; // Convert to MB
        
        System.out.println("PT-05: Memory used for 1000 students + 100 courses: " + 
            memoryUsed + "MB");
        
        // Memory usage should be reasonable (< 50MB for this data)
        assertTrue(memoryUsed < 50, 
            String.format("Memory usage %dMB exceeds threshold of 50MB", memoryUsed));
    }

    @Test
    @DisplayName("PT-06: Concurrent operation simulation")
    void testConcurrentOperationSimulation() throws EnrollmentException {
        // Setup
        service.addCourse("CONCURRENT001", "Concurrent Test", 100);
        
        for (int i = 1; i <= 200; i++) {
            String id = String.format("B%04d", i);
            String email = String.format("student%d@uca.edu", i);
            service.addStudent(id, "Student" + i, email);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Simulate many rapid operations
        for (int i = 1; i <= 200; i++) {
            String id = String.format("B%04d", i);
            service.enrollStudent(id, "CONCURRENT001");
            
            // Every 10th student, check enrollment info
            if (i % 10 == 0) {
                service.getCourseEnrollmentInfo("CONCURRENT001");
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify final state
        var info = service.getCourseEnrollmentInfo("CONCURRENT001");
        assertEquals(100, info.getEnrolledCount());
        assertEquals(100, info.getWaitlistCount());
        
        System.out.println("PT-06: 200 enrollments with queries in " + duration + "ms");
        
        assertTrue(duration < 5000,
            String.format("Concurrent operations took %dms (expected < 5000ms)", duration));
    }

    @Test
    @DisplayName("PT-07: Stress test - Maximum capacity course")
    void testMaximumCapacityCourse() throws EnrollmentException {
        // Create course with maximum capacity
        service.addCourse("MAXCAP001", "Maximum Capacity Course", 500);
        
        // Add 600 students
        for (int i = 1; i <= 600; i++) {
            String id = String.format("B%04d", i);
            String email = String.format("student%d@uca.edu", i);
            service.addStudent(id, "Student" + i, email);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Enroll all students
        for (int i = 1; i <= 600; i++) {
            String id = String.format("B%04d", i);
            service.enrollStudent(id, "MAXCAP001");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Verify
        var info = service.getCourseEnrollmentInfo("MAXCAP001");
        assertEquals(500, info.getEnrolledCount());
        assertEquals(100, info.getWaitlistCount());
        
        System.out.println("PT-07: Filled max capacity course in " + duration + "ms");
        
        assertTrue(duration < 15000,
            String.format("Max capacity enrollment took %dms (expected < 15000ms)", duration));
    }

    // ==================== Helper Classes ====================
    
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
            return enrollments.stream().filter(e -> e.getCourseCode().equals(courseCode)).toList();
        }
        
        public List<Enrollment> findByStudentId(String studentId) {
            return enrollments.stream().filter(e -> e.getStudentId().equals(studentId)).toList();
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