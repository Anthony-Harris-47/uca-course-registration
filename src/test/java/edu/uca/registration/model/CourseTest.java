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
 * Unit tests for Course model class.
 * Test ID Prefix: UT-CRS (Unit Test - Course)
 */
@DisplayName("Course Model Unit Tests")
class CourseTest {

    @Test
    @DisplayName("UT-CRS-01: Create course with valid data")
    void testCreateCourseWithValidData() {
        // Arrange & Act
        Course course = new Course("CSCI4490", "Software Engineering", 30);
        
        // Assert
        assertNotNull(course);
        assertEquals("CSCI4490", course.getCode());
        assertEquals("Software Engineering", course.getTitle());
        assertEquals(30, course.getCapacity());
    }

    @Test
    @DisplayName("UT-CRS-02: Course with null code throws exception")
    void testCreateCourseWithNullCode() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Course(null, "Software Engineering", 30)
        );
        assertTrue(exception.getMessage().contains("code cannot be empty"));
    }

    @Test
    @DisplayName("UT-CRS-03: Course with empty code throws exception")
    void testCreateCourseWithEmptyCode() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> new Course("", "Software Engineering", 30));
    }

    @Test
    @DisplayName("UT-CRS-04: Course with null title throws exception")
    void testCreateCourseWithNullTitle() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> new Course("CSCI4490", null, 30));
    }

    @Test
    @DisplayName("UT-CRS-05: Course with empty title throws exception")
    void testCreateCourseWithEmptyTitle() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> new Course("CSCI4490", "", 30));
    }

    @Test
    @DisplayName("UT-CRS-06: Course with capacity 0 throws exception")
    void testCreateCourseWithZeroCapacity() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Course("CSCI4490", "Software Engineering", 0)
        );
        assertTrue(exception.getMessage().contains("between 1 and 500"));
    }

    @Test
    @DisplayName("UT-CRS-07: Course with negative capacity throws exception")
    void testCreateCourseWithNegativeCapacity() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> new Course("CSCI4490", "Software Engineering", -1));
    }

    @Test
    @DisplayName("UT-CRS-08: Course with capacity over 500 throws exception")
    void testCreateCourseWithExcessiveCapacity() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new Course("CSCI4490", "Software Engineering", 501)
        );
        assertTrue(exception.getMessage().contains("between 1 and 500"));
    }

    @Test
    @DisplayName("UT-CRS-09: Course with capacity 1 is valid")
    void testCreateCourseWithMinimumCapacity() {
        // Arrange & Act
        Course course = new Course("CSCI4490", "Software Engineering", 1);
        
        // Assert
        assertEquals(1, course.getCapacity());
    }

    @Test
    @DisplayName("UT-CRS-10: Course with capacity 500 is valid")
    void testCreateCourseWithMaximumCapacity() {
        // Arrange & Act
        Course course = new Course("CSCI4490", "Software Engineering", 500);
        
        // Assert
        assertEquals(500, course.getCapacity());
    }

    @Test
    @DisplayName("UT-CRS-11: Course trims whitespace from inputs")
    void testCourseTrimsWhitespace() {
        // Arrange & Act
        Course course = new Course("  CSCI4490  ", "  Software Engineering  ", 30);
        
        // Assert
        assertEquals("CSCI4490", course.getCode());
        assertEquals("Software Engineering", course.getTitle());
    }

    @Test
    @DisplayName("UT-CRS-12: Courses with same code are equal")
    void testCoursesWithSameCodeAreEqual() {
        // Arrange
        Course course1 = new Course("CSCI4490", "Software Engineering", 30);
        Course course2 = new Course("CSCI4490", "Different Title", 50);
        
        // Act & Assert
        assertEquals(course1, course2);
        assertEquals(course1.hashCode(), course2.hashCode());
    }

    @Test
    @DisplayName("UT-CRS-13: Courses with different codes are not equal")
    void testCoursesWithDifferentCodesAreNotEqual() {
        // Arrange
        Course course1 = new Course("CSCI4490", "Software Engineering", 30);
        Course course2 = new Course("CSCI3901", "Software Engineering", 30);
        
        // Act & Assert
        assertNotEquals(course1, course2);
    }

    @Test
    @DisplayName("UT-CRS-14: toString returns formatted string")
    void testToString() {
        // Arrange
        Course course = new Course("CSCI4490", "Software Engineering", 30);
        
        // Act
        String result = course.toString();
        
        // Assert
        assertTrue(result.contains("CSCI4490"));
        assertTrue(result.contains("Software Engineering"));
    }

    @Test
    @DisplayName("UT-CRS-15: Various valid capacities accepted")
    void testVariousValidCapacities() {
        // These should all be valid
        assertDoesNotThrow(() -> new Course("C1", "Test", 1));
        assertDoesNotThrow(() -> new Course("C2", "Test", 25));
        assertDoesNotThrow(() -> new Course("C3", "Test", 100));
        assertDoesNotThrow(() -> new Course("C4", "Test", 250));
        assertDoesNotThrow(() -> new Course("C5", "Test", 500));
    }
}