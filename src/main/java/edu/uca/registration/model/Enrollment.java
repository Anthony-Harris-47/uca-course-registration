package edu.uca.registration.model;

import java.util.Objects;


//Represents an enrollment or waitlist entry.

public class Enrollment {
    private final String studentId;
    private final String courseCode;
    private final EnrollmentStatus status;

    public enum EnrollmentStatus {
        ENROLLED,
        WAITLISTED
    }

    public Enrollment(String studentId, String courseCode, EnrollmentStatus status) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty");
        }
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        this.studentId = studentId.trim();
        this.courseCode = courseCode.trim();
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public boolean isEnrolled() {
        return status == EnrollmentStatus.ENROLLED;
    }

    public boolean isWaitlisted() {
        return status == EnrollmentStatus.WAITLISTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(studentId, that.studentId) &&
               Objects.equals(courseCode, that.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseCode);
    }

    @Override
    public String toString() {
        return studentId + " -> " + courseCode + " [" + status + "]";
    }
}