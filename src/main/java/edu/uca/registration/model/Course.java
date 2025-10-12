package edu.uca.registration.model;

import java.util.Objects;

//This represents a course in the registration system.
//Immutable domain object with validation.

public class Course {
    private final String code;
    private final String title;
    private final int capacity;

    public Course(String code, String title, int capacity) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty");
        }
        if (capacity < 1 || capacity > 500) {
            throw new IllegalArgumentException("Capacity must be between 1 and 500");
        }
        
        this.code = code.trim();
        this.title = title.trim();
        this.capacity = capacity;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(code, course.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " - " + title;
    }
}