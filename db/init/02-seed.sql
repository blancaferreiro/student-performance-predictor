-- Datos de prueba iniciales.
-- Se cargan una sola vez, tras ejecutarse 01-schema.sql, cuando el
-- volumen de MySQL está vacío.

SET NAMES utf8mb4;
USE studentdb;

INSERT INTO courses (id, name, start_date, end_date) VALUES
  (1, '2025-2026', '2025-09-01', '2026-06-30'),
  (2, '2024-2025', '2024-09-01', '2025-06-30');

INSERT INTO subjects (id, name, difficulty) VALUES
  (1, 'Algoritmos', 'HARD'),
  (2, 'Bases de Datos', 'MEDIUM'),
  (3, 'Programación', 'EASY');

INSERT INTO students (id, name, study_hours) VALUES
  (1, 'Ana', 10),
  (2, 'Luis', 8),
  (3, 'Raúl', 6);

INSERT INTO enrollments (student_id, subject_id, course_id, study_hours, grade) VALUES
  (1, 1, 1, 12, 8.5),
  (1, 3, 1, 8, 7.0),
  (2, 1, 1, 6, NULL),
  (2, 2, 2, 9, 6.5),
  (3, 3, 1, 4, 5.5);
