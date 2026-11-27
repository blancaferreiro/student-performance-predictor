-- Esquema de Student Performance Predictor.
-- MySQL ejecuta este archivo automáticamente la primera vez que se
-- inicializa el contenedor (volumen de datos vacío).

CREATE DATABASE IF NOT EXISTS studentdb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE studentdb;

CREATE TABLE IF NOT EXISTS students (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  study_hours DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS subjects (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  difficulty ENUM('EASY','MEDIUM','HARD') NOT NULL
);

CREATE TABLE IF NOT EXISTS courses (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
  student_id INT NOT NULL,
  subject_id INT NOT NULL,
  course_id INT NOT NULL,
  study_hours DOUBLE NOT NULL CHECK (study_hours >= 0),
  grade DOUBLE NULL CHECK (grade IS NULL OR (grade >= 0 AND grade <= 10)),
  PRIMARY KEY (student_id, subject_id, course_id),
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT
);
