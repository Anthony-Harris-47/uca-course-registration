package edu.uca.registration.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Enrollment;
import edu.uca.registration.model.Enrollment.EnrollmentStatus;
import edu.uca.registration.model.Student;
import edu.uca.registration.repository.CourseRepository;
import edu.uca.registration.repository.EnrollmentRepository;
import edu.uca.registration.repository.StudentRepository;
import edu.uca.registration.util.Logger;

/**
 * Unit and Component tests for RegistrationService.
 * Test ID Prefix: UT-SVC (Unit Test - Service), CT-SVC (Component Test - Service)
 */
@DisplayName("Registration Service Tests")
class RegistrationServiceTest {

    @Mock
    private StudentRepository studentRepo;
    
    @Mock
    private CourseRepository courseRepo;
    
    @Mock
    private EnrollmentRepository enrollmentRepo;
    
    @Mock
    private Logger logger;
    
    private RegistrationService service;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        service = new RegistrationService(studentRepo, courseRepo, enrollmentRepo, logger);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ==================== Unit Tests - Add Student ====================

    @Test
    @DisplayName("UT-SVC-01: Add student with valid data succeeds")
    void testAddStudentWithValidData() throws EnrollmentException {
        // Arrange
        when(studentRepo.exists("B001")).thenReturn(false);
        
        // Act
        service.addStudent("B001", "Alice", "alice@uca.edu");
        
        // Assert
        verify(studentRepo, times(1)).exists("B001");
        verify(studentRepo, times(1)).save(any(Student.class));
        verify(logger, times(1)).info(contains("ADD_STUDENT"));
    }

    @Test
    @DisplayName("UT-SVC-02: Add duplicate student throws exception")
    void testAddDuplicateStudent() {
        // Arrange
        when(studentRepo.exists("B001")).thenReturn(true);
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.addStudent("B001", "Alice", "alice@uca.edu")
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(studentRepo, never()).save(any());
    }

    @Test
    @DisplayName("UT-SVC-03: Add student with invalid email throws exception")
    void testAddStudentWithInvalidEmail() {
        // Arrange
        when(studentRepo.exists("B001")).thenReturn(false);
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.addStudent("B001", "Alice", "not-an-email")
        );
        assertTrue(exception.getMessage().contains("Invalid"));
        verify(studentRepo, never()).save(any());
    }

    // ==================== Unit Tests - Add Course ====================

    @Test
    @DisplayName("UT-SVC-04: Add course with valid data succeeds")
    void testAddCourseWithValidData() throws EnrollmentException {
        // Arrange
        when(courseRepo.exists("CSCI4490")).thenReturn(false);
        
        // Act
        service.addCourse("CSCI4490", "Software Engineering", 30);
        
        // Assert
        verify(courseRepo, times(1)).exists("CSCI4490");
        verify(courseRepo, times(1)).save(any(Course.class));
        verify(logger, times(1)).info(contains("ADD_COURSE"));
    }

    @Test
    @DisplayName("UT-SVC-05: Add duplicate course throws exception")
    void testAddDuplicateCourse() {
        // Arrange
        when(courseRepo.exists("CSCI4490")).thenReturn(true);
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.addCourse("CSCI4490", "Software Engineering", 30)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(courseRepo, never()).save(any());
    }

    @Test
    @DisplayName("UT-SVC-06: Add course with invalid capacity throws exception")
    void testAddCourseWithInvalidCapacity() {
        // Arrange
        when(courseRepo.exists("CSCI4490")).thenReturn(false);
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.addCourse("CSCI4490", "Software Engineering", 0)
        );
        assertTrue(exception.getMessage().contains("Invalid"));
        verify(courseRepo, never()).save(any());
    }

    // ==================== Component Tests - Enrollment ====================

    @Test
    @DisplayName("CT-SVC-01: Enroll student in available course succeeds")
    void testEnrollStudentInAvailableCourse() throws EnrollmentException {
        // Arrange
        when(studentRepo.exists("B001")).thenReturn(true);
        when(courseRepo.findByCode("CSCI4490"))
            .thenReturn(Optional.of(new Course("CSCI4490", "Software Engineering", 30)));
        when(enrollmentRepo.exists("B001", "CSCI4490")).thenReturn(false);
        when(enrollmentRepo.countByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(5);
        
        // Act
        var result = service.enrollStudent("B001", "CSCI4490");
        
        // Assert
        assertFalse(result.isWaitlisted());
        assertEquals("Enrolled.", result.getMessage());
        verify(enrollmentRepo, times(1)).save(any(Enrollment.class));
        verify(logger, times(1)).info(contains("ENROLL"));
    }

    @Test
    @DisplayName("CT-SVC-02: Enroll student in full course adds to waitlist")
    void testEnrollStudentInFullCourse() throws EnrollmentException {
        // Arrange
        when(studentRepo.exists("B001")).thenReturn(true);
        when(courseRepo.findByCode("CSCI4490"))
            .thenReturn(Optional.of(new Course("CSCI4490", "Software Engineering", 2)));
        when(enrollmentRepo.exists("B001", "CSCI4490")).thenReturn(false);
        when(enrollmentRepo.countByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(2); // Course is full
        
        // Act
        var result = service.enrollStudent("B001", "CSCI4490");
        
        // Assert
        assertTrue(result.isWaitlisted());
        assertTrue(result.getMessage().contains("WAITLIST"));
        verify(enrollmentRepo, times(1)).save(argThat(e -> 
            e.getStatus() == EnrollmentStatus.WAITLISTED));
        verify(logger, times(1)).info(contains("WAITLIST"));
    }

    @Test
    @DisplayName("CT-SVC-03: Enroll non-existent student throws exception")
    void testEnrollNonExistentStudent() {
        // Arrange
        when(studentRepo.exists("B999")).thenReturn(false);
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.enrollStudent("B999", "CSCI4490")
        );
        assertTrue(exception.getMessage().contains("not found"));
        verify(enrollmentRepo, never()).save(any());
    }

    @Test
    @DisplayName("CT-SVC-04: Enroll in non-existent course throws exception")
    void testEnrollInNonExistentCourse() {
        // Arrange
        when(studentRepo.exists("B001")).thenReturn(true);
        when(courseRepo.findByCode("INVALID999")).thenReturn(Optional.empty());
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.enrollStudent("B001", "INVALID999")
        );
        assertTrue(exception.getMessage().contains("not found"));
        verify(enrollmentRepo, never()).save(any());
    }

    @Test
    @DisplayName("CT-SVC-05: Enroll already enrolled student throws exception")
    void testEnrollAlreadyEnrolledStudent() {
        // Arrange
        when(studentRepo.exists("B001")).thenReturn(true);
        when(courseRepo.findByCode("CSCI4490"))
            .thenReturn(Optional.of(new Course("CSCI4490", "Software Engineering", 30)));
        when(enrollmentRepo.exists("B001", "CSCI4490")).thenReturn(true);
        
        List<Enrollment> enrollments = List.of(
            new Enrollment("B001", "CSCI4490", EnrollmentStatus.ENROLLED)
        );
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(enrollments);
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.enrollStudent("B001", "CSCI4490")
        );
        assertTrue(exception.getMessage().contains("already enrolled"));
        verify(enrollmentRepo, never()).save(any());
    }

    // ==================== Component Tests - Drop with Promotion ====================

    @Test
    @DisplayName("CT-SVC-06: Drop student without waitlist succeeds")
    void testDropStudentWithoutWaitlist() throws EnrollmentException {
        // Arrange
        when(courseRepo.findByCode("CSCI4490"))
            .thenReturn(Optional.of(new Course("CSCI4490", "Software Engineering", 30)));
        
        List<Enrollment> enrolled = List.of(
            new Enrollment("B001", "CSCI4490", EnrollmentStatus.ENROLLED)
        );
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(enrolled);
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.WAITLISTED))
            .thenReturn(Collections.emptyList());
        when(enrollmentRepo.delete("B001", "CSCI4490")).thenReturn(true);
        
        // Act
        var result = service.dropStudent("B001", "CSCI4490");
        
        // Assert
        assertEquals("Dropped.", result.getMessage());
        assertFalse(result.getPromotedStudentId().isPresent());
        verify(enrollmentRepo, times(1)).delete("B001", "CSCI4490");
        verify(logger, times(1)).info(contains("DROP"));
    }

    @Test
    @DisplayName("CT-SVC-07: Drop student promotes first waitlisted (FIFO)")
    void testDropStudentPromotesFromWaitlist() throws EnrollmentException {
        // Arrange
        when(courseRepo.findByCode("CSCI4490"))
            .thenReturn(Optional.of(new Course("CSCI4490", "Software Engineering", 2)));
        
        List<Enrollment> enrolled = List.of(
            new Enrollment("B001", "CSCI4490", EnrollmentStatus.ENROLLED)
        );
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(enrolled);
        
        List<Enrollment> waitlist = List.of(
            new Enrollment("B003", "CSCI4490", EnrollmentStatus.WAITLISTED),
            new Enrollment("B004", "CSCI4490", EnrollmentStatus.WAITLISTED)
        );
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.WAITLISTED))
            .thenReturn(waitlist);
        when(enrollmentRepo.delete("B001", "CSCI4490")).thenReturn(true);
        when(enrollmentRepo.delete("B003", "CSCI4490")).thenReturn(true);
        
        // Act
        var result = service.dropStudent("B001", "CSCI4490");
        
        // Assert
        assertTrue(result.getMessage().contains("Promoted"));
        assertTrue(result.getPromotedStudentId().isPresent());
        assertEquals("B003", result.getPromotedStudentId().get());
        verify(enrollmentRepo, times(1)).delete("B001", "CSCI4490");
        verify(enrollmentRepo, times(1)).delete("B003", "CSCI4490");
        verify(enrollmentRepo, times(1)).save(argThat(e -> 
            e.getStudentId().equals("B003") && 
            e.getStatus() == EnrollmentStatus.ENROLLED));
        verify(logger, times(1)).info(contains("PROMOTE"));
    }

    @Test
    @DisplayName("CT-SVC-08: Drop non-enrolled student throws exception")
    void testDropNonEnrolledStudent() {
        // Arrange
        when(courseRepo.findByCode("CSCI4490"))
            .thenReturn(Optional.of(new Course("CSCI4490", "Software Engineering", 30)));
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(Collections.emptyList());
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.WAITLISTED))
            .thenReturn(Collections.emptyList());
        
        // Act & Assert
        EnrollmentException exception = assertThrows(
            EnrollmentException.class,
            () -> service.dropStudent("B001", "CSCI4490")
        );
        assertTrue(exception.getMessage().contains("not enrolled"));
    }

    @Test
    @DisplayName("CT-SVC-09: Drop waitlisted student succeeds")
    void testDropWaitlistedStudent() throws EnrollmentException {
        // Arrange
        when(courseRepo.findByCode("CSCI4490"))
            .thenReturn(Optional.of(new Course("CSCI4490", "Software Engineering", 2)));
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(Collections.emptyList());
        
        List<Enrollment> waitlist = List.of(
            new Enrollment("B001", "CSCI4490", EnrollmentStatus.WAITLISTED)
        );
        when(enrollmentRepo.findByCourseAndStatus("CSCI4490", EnrollmentStatus.WAITLISTED))
            .thenReturn(waitlist);
        when(enrollmentRepo.delete("B001", "CSCI4490")).thenReturn(true);
        
        // Act
        var result = service.dropStudent("B001", "CSCI4490");
        
        // Assert
        assertTrue(result.getMessage().contains("Removed from waitlist"));
        assertFalse(result.getPromotedStudentId().isPresent());
        verify(enrollmentRepo, times(1)).delete("B001", "CSCI4490");
    }

    // ==================== Component Tests - Query Operations ====================

    @Test
    @DisplayName("CT-SVC-10: Get course enrollment info succeeds")
    void testGetCourseEnrollmentInfo() throws EnrollmentException {
        // Arrange
        Course course = new Course("CSCI4490", "Software Engineering", 30);
        when(courseRepo.findByCode("CSCI4490")).thenReturn(Optional.of(course));
        when(enrollmentRepo.countByCourseAndStatus("CSCI4490", EnrollmentStatus.ENROLLED))
            .thenReturn(25);
        when(enrollmentRepo.countByCourseAndStatus("CSCI4490", EnrollmentStatus.WAITLISTED))
            .thenReturn(5);
        
        // Act
        var info = service.getCourseEnrollmentInfo("CSCI4490");
        
        // Assert
        assertNotNull(info);
        assertEquals(course, info.getCourse());
        assertEquals(25, info.getEnrolledCount());
        assertEquals(5, info.getWaitlistCount());
        assertTrue(info.getDisplayString().contains("CSCI4490"));
        assertTrue(info.getDisplayString().contains("25"));
        assertTrue(info.getDisplayString().contains("5"));
    }

    @Test
    @DisplayName("CT-SVC-11: Get info for non-existent course throws exception")
    void testGetCourseEnrollmentInfoForNonExistentCourse() {
        // Arrange
        when(courseRepo.findByCode("INVALID999")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EnrollmentException.class,
            () -> service.getCourseEnrollmentInfo("INVALID999"));
    }
}