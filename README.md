UCA Course Registration System


Features

Add and list students (Banner ID, name, email)

Add and list courses (code, title, capacity)

Enroll students automatically with a waitlist when full

Drop courses with FIFO promotion from waitlist

File-based persistence with error handling

57+ automated tests covering all layers


We followed a simple layered design:

CLI (UI)
Service Layer (Business Logic)
Repository Layer (Data Access)
Domain Models (Entities)

Each layer only talks to the one below it.
Repositories handle CSV files, services handle logic, and the CLI is just the user interface.

Quick Start

You’ll need Java 17+ and Maven 3.6+.

mvn clean package

java -jar target/course-registration-0.1.0.jar

Use --demo to start with some sample data.


Running Tests
mvn test          # run all tests
mvn test jacoco:report   # run with coverage


Coverage: ~89% overall
Models: 95% • Service: 91% • Repository: 86%

Example Commands
1) Add student
Banner ID: B001
Name: Alice
Email: alice@uca.edu

3) Enroll student in course
Student ID: B001
Course Code: CSCI4490


If the course is full, the student goes straight to the waitlist.

Config

You can adjust file paths and log level using environment variables or a properties file.

Example:

export STUDENTS_FILE=data/students.csv
export LOG_LEVEL=DEBUG


Testing Summary

We have 57+ automated tests:

12 model tests
15 course tests
17 service/integration tests
6 system tests
7 performance tests