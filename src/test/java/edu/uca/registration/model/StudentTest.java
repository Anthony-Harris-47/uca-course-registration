package edu.uca.registration.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Student model class.
 * Test ID Prefix: UT-STU (Unit Test - Student)
 */
@DisplayName("Student Model Unit Tests")
class StudentTest {

    @Test
    @DisplayName("UT-STU-01: Create student with valid data")
    void testCreateStudentWithValidData() {
        // Arrange & Act
        Student student = new Student("B001", "Alice", "alice@uca.edu");
        
        // Assert
        assertNotNull(student);
        assertEquals("B001", student.getId());
        assertEquals("Alice", student.getName());
        assertEquals("alice@uca.edu", student.getEmail());
    }

    @Test
    @DisplayName("UT-STU-02: Student with null ID throws exception")
    void testCreateStudentWithNullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Student(null, "Alice", "alice@uca.edu")
        );
        assertTrue(exception.getMessage().contains("ID cannot be empty"));
    }

    @Test
    @DisplayName("UT-STU-03: Student with empty ID throws exception")
    void testCreateStudentWithEmptyId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Student("", "Alice", "alice@uca.edu")
        );
        assertTrue(exception.getMessage().contains("ID cannot be empty"));
    }

    @Test
    @DisplayName("UT-STU-04: Student with whitespace ID throws exception")
    void testCreateStudentWithWhitespaceId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Student("   ", "Alice", "alice@uca.edu")
        );
        assertTrue(exception.getMessage().contains("ID cannot be empty"));
    }

    @Test
    @DisplayName("UT-STU-05: Student with null name throws exception")
    void testCreateStudentWithNullName() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Student("B001", null, "alice@uca.edu")
        );
        assertTrue(exception.getMessage().contains("name cannot be empty"));
    }

    @Test
    @DisplayName("UT-STU-06: Student with invalid email throws exception")
    void testCreateStudentWithInvalidEmail() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Student("B001", "Alice", "not-an-email")
        );
        assertTrue(exception.getMessage().contains("Invalid email"));
    }

    @Test
    @DisplayName("UT-STU-07: Student trims whitespace from inputs")
    void testStudentTrimsWhitespace() {
        // Arrange & Act
        Student student = new Student("  B001  ", "  Alice  ", "alice@uca.edu");
        
        // Assert
        assertEquals("B001", student.getId());
        assertEquals("Alice", student.getName());
        assertEquals("alice@uca.edu", student.getEmail());
    }

    @Test
    @DisplayName("UT-STU-08: Students with same ID are equal")
    void testStudentsWithSameIdAreEqual() {
        // Arrange
        Student student1 = new Student("B001", "Alice", "alice@uca.edu");
        Student student2 = new Student("B001", "Bob", "bob@uca.edu");
        
        // Act & Assert
        assertEquals(student1, student2);
        assertEquals(student1.hashCode(), student2.hashCode());
    }

    @Test
    @DisplayName("UT-STU-09: Students with different IDs are not equal")
    void testStudentsWithDifferentIdsAreNotEqual() {
        // Arrange
        Student student1 = new Student("B001", "Alice", "alice@uca.edu");
        Student student2 = new Student("B002", "Alice", "alice@uca.edu");
        
        // Act & Assert
        assertNotEquals(student1, student2);
    }

    @Test
    @DisplayName("UT-STU-10: toString returns formatted string")
    void testToString() {
        // Arrange
        Student student = new Student("B001", "Alice", "alice@uca.edu");
        
        // Act
        String result = student.toString();
        
        // Assert
        assertTrue(result.contains("B001"));
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("alice@uca.edu"));
    }

    @Test
    @DisplayName("UT-STU-11: Various valid email formats accepted")
    void testValidEmailFormats() {
        // These should all be valid
        assertDoesNotThrow(() -> new Student("B001", "Test", "user@domain.com"));
        assertDoesNotThrow(() -> new Student("B002", "Test", "user.name@domain.com"));
        assertDoesNotThrow(() -> new Student("B003", "Test", "user+tag@domain.co.uk"));
        assertDoesNotThrow(() -> new Student("B004", "Test", "user_name@sub.domain.org"));
    }

    @Test
    @DisplayName("UT-STU-12: Invalid email formats rejected")
    void testInvalidEmailFormats() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Student("B001", "Test", "notanemail"));
        assertThrows(IllegalArgumentException.class, 
            () -> new Student("B002", "Test", "@domain.com"));
        assertThrows(IllegalArgumentException.class, 
            () -> new Student("B003", "Test", "user@"));
        assertThrows(IllegalArgumentException.class, 
            () -> new Student("B004", "Test", "user domain.com"));
    }
}