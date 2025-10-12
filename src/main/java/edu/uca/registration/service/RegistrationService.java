package edu.uca.registration.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import edu.uca.registration.model.Course;
import edu.uca.registration.model.Enrollment;
import edu.uca.registration.model.Enrollment.EnrollmentStatus;
import edu.uca.registration.model.Student;
import edu.uca.registration.repository.CourseRepository;
import edu.uca.registration.repository.EnrollmentRepository;
import edu.uca.registration.repository.StudentRepository;
import edu.uca.registration.util.Logger;

/**
 * Service layer containing all business logic for course registration.
 * Enforces capacity limits, waitlist management, and FIFO promotion.
 */
public class RegistrationService {
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final Logger logger;

    public RegistrationService(StudentRepository studentRepo, 
                              CourseRepository courseRepo,
                              EnrollmentRepository enrollmentRepo,
                              Logger logger) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.logger = logger;
    }

    //Student Operations
    
    public void addStudent(String id, String name, String email) throws EnrollmentException {
        if (studentRepo.exists(id)) {
            throw new EnrollmentException("Student with ID " + id + " already exists");
        }
        
        try {
            Student student = new Student(id, name, email);
            studentRepo.save(student);
            logger.info("ADD_STUDENT " + id);
        } catch (IllegalArgumentException e) {
            throw new EnrollmentException("Invalid student data: " + e.getMessage(), e);
        }
    }

    public Optional<Student> findStudent(String id) {
        return studentRepo.findById(id);
    }

    public Collection<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    //Course Operations
    
    public void addCourse(String code, String title, int capacity) throws EnrollmentException {
        if (courseRepo.exists(code)) {
            throw new EnrollmentException("Course with code " + code + " already exists");
        }
        
        try {
            Course course = new Course(code, title, capacity);
            courseRepo.save(course);
            logger.info("ADD_COURSE " + code);
        } catch (IllegalArgumentException e) {
            throw new EnrollmentException("Invalid course data: " + e.getMessage(), e);
        }
    }

    public Optional<Course> findCourse(String code) {
        return courseRepo.findByCode(code);
    }

    public Collection<Course> getAllCourses() {
        return courseRepo.findAll();
    }

    //Enrollment Operations
    
    public EnrollmentResult enrollStudent(String studentId, String courseCode) throws EnrollmentException {
        
        if (!studentRepo.exists(studentId)) {
            throw new EnrollmentException("Student " + studentId + " not found");
        }

        Optional<Course> courseOpt = courseRepo.findByCode(courseCode);
        if (courseOpt.isEmpty()) {
            throw new EnrollmentException("Course " + courseCode + " not found");
        }
        Course course = courseOpt.get();

        // Check if already enrolled
        if (enrollmentRepo.exists(studentId, courseCode)) {
            List<Enrollment> existing = enrollmentRepo.findByCourseAndStatus(courseCode, EnrollmentStatus.ENROLLED);
            boolean enrolled = existing.stream().anyMatch(e -> e.getStudentId().equals(studentId));
            if (enrolled) {
                throw new EnrollmentException("Student already enrolled in " + courseCode);
            } else {
                throw new EnrollmentException("Student already on waitlist for " + courseCode);
            }
        }

        // Check capacity
        int enrolledCount = enrollmentRepo.countByCourseAndStatus(courseCode, EnrollmentStatus.ENROLLED);
        
        if (enrolledCount >= course.getCapacity()) {
            // Add to waitlist
            Enrollment enrollment = new Enrollment(studentId, courseCode, EnrollmentStatus.WAITLISTED);
            enrollmentRepo.save(enrollment);
            logger.info("WAITLIST " + studentId + " -> " + courseCode);
            return EnrollmentResult.waitlisted();
        } else {
            // Enroll directly
            Enrollment enrollment = new Enrollment(studentId, courseCode, EnrollmentStatus.ENROLLED);
            enrollmentRepo.save(enrollment);
            logger.info("ENROLL " + studentId + " -> " + courseCode);
            return EnrollmentResult.enrolled();
        }
    }

    public DropResult dropStudent(String studentId, String courseCode) throws EnrollmentException {
        // Validate course exists
        if (!courseRepo.findByCode(courseCode).isPresent()) {
            throw new EnrollmentException("Course " + courseCode + " not found");
        }

        // Check if enrolled
        List<Enrollment> enrolled = enrollmentRepo.findByCourseAndStatus(courseCode, EnrollmentStatus.ENROLLED);
        boolean wasEnrolled = enrolled.stream().anyMatch(e -> e.getStudentId().equals(studentId));

        if (wasEnrolled) {
            enrollmentRepo.delete(studentId, courseCode);
            logger.info("DROP " + studentId + " from " + courseCode);

            // Promote first waitlisted student
            List<Enrollment> waitlist = enrollmentRepo.findByCourseAndStatus(courseCode, EnrollmentStatus.WAITLISTED);
            if (!waitlist.isEmpty()) {
                Enrollment firstWaitlisted = waitlist.get(0);
                String promotedId = firstWaitlisted.getStudentId();
                
                enrollmentRepo.delete(promotedId, courseCode);
                Enrollment promoted = new Enrollment(promotedId, courseCode, EnrollmentStatus.ENROLLED);
                enrollmentRepo.save(promoted);
                
                logger.info("PROMOTE " + promotedId + " -> " + courseCode);
                return DropResult.droppedWithPromotion(promotedId);
            }
            return DropResult.dropped();
        }

        // Check if on waitlist
        List<Enrollment> waitlist = enrollmentRepo.findByCourseAndStatus(courseCode, EnrollmentStatus.WAITLISTED);
        boolean wasWaitlisted = waitlist.stream().anyMatch(e -> e.getStudentId().equals(studentId));

        if (wasWaitlisted) {
            enrollmentRepo.delete(studentId, courseCode);
            logger.info("WAITLIST_REMOVE " + studentId + " " + courseCode);
            return DropResult.removedFromWaitlist();
        }

        throw new EnrollmentException("Student not enrolled or waitlisted in " + courseCode);
    }

    //Query Operations
    
    public CourseEnrollmentInfo getCourseEnrollmentInfo(String courseCode) throws EnrollmentException {
        Optional<Course> courseOpt = courseRepo.findByCode(courseCode);
        if (courseOpt.isEmpty()) {
            throw new EnrollmentException("Course " + courseCode + " not found");
        }
        
        Course course = courseOpt.get();
        int enrolled = enrollmentRepo.countByCourseAndStatus(courseCode, EnrollmentStatus.ENROLLED);
        int waitlisted = enrollmentRepo.countByCourseAndStatus(courseCode, EnrollmentStatus.WAITLISTED);
        
        return new CourseEnrollmentInfo(course, enrolled, waitlisted);
    }

    //Result Classes
    
    public static class EnrollmentResult {
        private final boolean waitlisted;

        private EnrollmentResult(boolean waitlisted) {
            this.waitlisted = waitlisted;
        }

        public static EnrollmentResult enrolled() {
            return new EnrollmentResult(false);
        }

        public static EnrollmentResult waitlisted() {
            return new EnrollmentResult(true);
        }

        public boolean isWaitlisted() {
            return waitlisted;
        }

        public String getMessage() {
            return waitlisted ? "Course full. Added to WAITLIST." : "Enrolled.";
        }
    }

    public static class DropResult {
        private final String message;
        private final String promotedStudentId;

        private DropResult(String message, String promotedStudentId) {
            this.message = message;
            this.promotedStudentId = promotedStudentId;
        }

        public static DropResult dropped() {
            return new DropResult("Dropped.", null);
        }

        public static DropResult droppedWithPromotion(String promotedId) {
            return new DropResult("Dropped. Promoted " + promotedId + " from waitlist.", promotedId);
        }

        public static DropResult removedFromWaitlist() {
            return new DropResult("Removed from waitlist.", null);
        }

        public String getMessage() {
            return message;
        }

        public Optional<String> getPromotedStudentId() {
            return Optional.ofNullable(promotedStudentId);
        }
    }

    public static class CourseEnrollmentInfo {
        private final Course course;
        private final int enrolledCount;
        private final int waitlistCount;

        public CourseEnrollmentInfo(Course course, int enrolledCount, int waitlistCount) {
            this.course = course;
            this.enrolledCount = enrolledCount;
            this.waitlistCount = waitlistCount;
        }

        public Course getCourse() {
            return course;
        }

        public int getEnrolledCount() {
            return enrolledCount;
        }

        public int getWaitlistCount() {
            return waitlistCount;
        }

        public String getDisplayString() {
            return String.format("%s %s cap=%d enrolled=%d wait=%d",
                    course.getCode(), course.getTitle(), course.getCapacity(),
                    enrolledCount, waitlistCount);
        }
    }
}