package edu.uca.registration;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    // ---- Global state (intentionally messy for refactor) ----
    static Map<String, Student> students = new LinkedHashMap<>();
    static Map<String, Course> courses = new LinkedHashMap<>();
    static List<String> auditLog = new ArrayList<>();

    // ---- CSV "persistence" files ----
    static final String STUDENTS_CSV = "students.csv";
    static final String COURSES_CSV = "courses.csv";
    static final String ENROLLMENTS_CSV = "enrollments.csv";

    public static void main(String[] args) {
        boolean demo = args.length > 0 && "--demo".equalsIgnoreCase(args[0]);
        if (demo) {
            seedDemoData();
            audit("SEED demo data");
        } else {
            loadAll();
        }

        println("=== UCA Course Registration (Baseline) ===");
        println("NOTE: This code is intentionally messy. You'll refactor it.");
        menuLoop();
        saveAll();
        println("Goodbye!");
    }

    private static void menuLoop() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            println("\nMenu:");
            println("1) Add student");
            println("2) Add course");
            println("3) Enroll student in course");
            println("4) Drop student from course");
            println("5) List students");
            println("6) List courses");
            println("0) Exit");
            print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addStudentUI(sc); break;
                case "2": addCourseUI(sc); break;
                case "3": enrollUI(sc); break;
                case "4": dropUI(sc); break;
                case "5": listStudents(); break;
                case "6": listCourses(); break;
                case "0": return;
                default: println("Invalid"); break;
            }
        }
    }

    private static void addStudentUI(Scanner sc) {
        print("Banner ID: ");
        String id = sc.nextLine().trim();
        print("Name: ");
        String name = sc.nextLine().trim();
        print("Email: ");
        String email = sc.nextLine().trim();
        Student s = new Student(id, name, email);
        students.put(id, s);
        audit("ADD_STUDENT " + id);
    }

    private static void addCourseUI(Scanner sc) {
        print("Course Code: ");
        String code = sc.nextLine().trim();
        print("Title: ");
        String title = sc.nextLine().trim();
        print("Capacity: ");
        int cap = Integer.parseInt(sc.nextLine().trim());
        Course c = new Course(code, title, cap);
        courses.put(code, c);
        audit("ADD_COURSE " + code);
    }

    private static void enrollUI(Scanner sc) {
        print("Student ID: ");
        String sid = sc.nextLine().trim();
        print("Course Code: ");
        String cc = sc.nextLine().trim();
        Course c = courses.get(cc);
        if (c == null) { println("No such course"); return; }
        if (c.roster.contains(sid)) { println("Already enrolled"); return; }
        if (c.waitlist.contains(sid)) { println("Already waitlisted"); return; }

        if (c.roster.size() >= c.capacity) {
            c.waitlist.add(sid);
            audit("WAITLIST " + sid + "->" + cc);
            println("Course full. Added to WAITLIST.");
        } else {
            c.roster.add(sid);
            audit("ENROLL " + sid + "->" + cc);
            println("Enrolled.");
        }
    }

    private static void dropUI(Scanner sc) {
        print("Student ID: ");
        String sid = sc.nextLine().trim();
        print("Course Code: ");
        String cc = sc.nextLine().trim();
        Course c = courses.get(cc);
        if (c == null) { println("No such course"); return; }

        if (c.roster.remove(sid)) {
            audit("DROP " + sid + " from " + cc);
            // Promote first waitlisted (FIFO)
            if (!c.waitlist.isEmpty()) {
                String promote = c.waitlist.remove(0);
                c.roster.add(promote);
                audit("PROMOTE " + promote + "->" + cc);
                println("Promoted " + promote + " from waitlist.");
            } else {
                println("Dropped.");
            }
        } else if (c.waitlist.remove(sid)) {
            audit("WAITLIST_REMOVE " + sid + " " + cc);
            println("Removed from waitlist.");
        } else {
            println("Not enrolled or waitlisted.");
        }
    }

    private static void listStudents() {
        println("Students:");
        for (Student s : students.values()) println(" - " + s);
    }

    private static void listCourses() {
        println("Courses:");
        for (Course c : courses.values())
            println(" - " + c.code + " " + c.title + " cap=" + c.capacity
                    + " enrolled=" + c.roster.size() + " wait=" + c.waitlist.size());
    }

    // -------------------- Persistence --------------------
    private static void loadAll() { loadStudents(); loadCourses(); loadEnrollments(); }

    private static void saveAll() { saveStudents(); saveCourses(); saveEnrollments(); }

    private static void loadStudents() {
        File f = new File(STUDENTS_CSV);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 3) {
                    students.put(p[0], new Student(p[0], p[1], p[2]));
                }
            }
            audit("LOAD students=" + students.size());
        } catch (Exception e) {
            println("Failed load students: " + e.getMessage());
        }
    }

    private static void saveStudents() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(STUDENTS_CSV))) {
            for (Student s : students.values()) {
                pw.println(s.id + "," + s.name + "," + s.email);
            }
        } catch (Exception e) {
            println("Failed save students: " + e.getMessage());
        }
    }

    private static void loadCourses() {
        File f = new File(COURSES_CSV);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 3) {
                    try {
                        int cap = Integer.parseInt(p[2]);
                        courses.put(p[0], new Course(p[0], p[1], cap));
                    } catch (NumberFormatException ignored) {}
                }
            }
            audit("LOAD courses=" + courses.size());
        } catch (Exception e) {
            println("Failed load courses: " + e.getMessage());
        }
    }

    private static void saveCourses() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(COURSES_CSV))) {
            for (Course c : courses.values()) {
                pw.println(c.code + "," + c.title + "," + c.capacity);
            }
        } catch (Exception e) {
            println("Failed save courses: " + e.getMessage());
        }
    }

    private static void loadEnrollments() {
        File f = new File(ENROLLMENTS_CSV);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Format: courseCode|studentId|ENROLLED or WAITLIST
                String[] p = line.split("\\|", -1);
                if (p.length >= 3) {
                    String code = p[0], sid = p[1], status = p[2];
                    Course c = courses.get(code);
                    if (c == null) continue;
                    if ("ENROLLED".equalsIgnoreCase(status)) {
                        if (!c.roster.contains(sid)) c.roster.add(sid);
                    } else if ("WAITLIST".equalsIgnoreCase(status)) {
                        if (!c.waitlist.contains(sid)) c.waitlist.add(sid);
                    }
                }
            }
            audit("LOAD enrollments");
        } catch (Exception e) {
            println("Failed load enrollments: " + e.getMessage());
        }
    }

    private static void saveEnrollments() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ENROLLMENTS_CSV))) {
            for (Course c : courses.values()) {
                for (String sid : c.roster) pw.println(c.code + "|" + sid + "|ENROLLED");
                for (String sid : c.waitlist) pw.println(c.code + "|" + sid + "|WAITLIST");
            }
        } catch (Exception e) {
            println("Failed save enrollments: " + e.getMessage());
        }
    }

    // -------------------- Demo data --------------------
    private static void seedDemoData() {
        students.put("B001", new Student("B001", "Alice", "alice@uca.edu"));
        students.put("B002", new Student("B002", "Brian", "brian@uca.edu"));
        courses.put("CSCI4490", new Course("CSCI4490", "Software Engineering", 2));
        courses.put("MATH1496", new Course("MATH1496", "Calculus I", 50));
    }

    // -------------------- Tiny domain types --------------------
    static class Student {
        String id, name, email;
        Student(String id, String name, String email) { this.id=id; this.name=name; this.email=email; }
        public String toString() { return id + " " + name + " <" + email + ">"; }
    }
    static class Course {
        String code, title; int capacity;
        List<String> roster = new ArrayList<>(), waitlist = new ArrayList<>();
        Course(String code, String title, int capacity) { this.code=code; this.title=title; this.capacity=capacity; }
    }

    // -------------------- Utils --------------------
    private static void print(String s){ System.out.print(s); }
    private static void println(String s){ System.out.println(s); }
    private static void audit(String ev){ auditLog.add(LocalDateTime.now() + " | " + ev); }
}
