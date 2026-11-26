# Contexto del proyecto — Student Performance Predictor

> Documento local para reanudar sesiones con poco contexto.
> Última actualización: 2026-11-19.
> Rama: `dev-raul`. Último commit: `9f1a801`.
> Estado: HU-05 implementada y verificada; cambios sin commit.

## 1. Objetivo y stack

Ejemplo didáctico de arquitectura por capas y microservicios:

- Java sirve el frontend y expone la API de estudiantes/asignaturas.
- Python calcula predicciones ficticias de notas.
- Python genera informes académicos en CSV y JSON.
- MySQL persiste estudiantes, asignaturas y matrículas.
- Java integra el servicio de informes mediante el patrón Adapter.

| Componente | Tecnología | Puerto | Rol |
| --- | --- | --- | --- |
| Frontend + API Java | `HttpServer`, Gson, JDBC | `8080` | UI, CRUD y orquestación |
| Predicción | FastAPI | `8000` | `POST /predict` |
| Informes | FastAPI | `8001` | `POST /reports/{format}` |
| MySQL | MySQL 8.4 | `3306` | Persistencia |

Flujo: navegador → Java `:8080` → predicción/reportes Python → respuesta.

## 2. Archivos clave

- `README.md`: documentación completa, contrato REST y ejecución.
- `compose.yaml`: MySQL, predicción, informes y Java.
- `requests.http`: ejemplos manuales de todos los endpoints.
- `java-api/src/main/java/edu/example/studentperformance/`:
  - `Main.java`: composición de dependencias y rutas HTTP.
  - `StudentController.java`: CRUD, predicciones y `GET /students/{id}/report`.
  - `StudentService.java`, `StudentRepository.java`: lógica y persistencia.
  - `Database.java`: creación de tablas y migración idempotente.
  - `Enrollment.java`, `Student.java`, `Subject.java`: dominio.
  - Adapter de informes: `ReportGenerator`, `PythonReportAdapter`, `ReportClient`,
    `ReportService`, `AcademicReport`, `ReportRecord`, `ReportResult`, `ReportFormat`.
- `python-api/app/`: predicción (`main.py`, `predictor/predictor_subject.py`).
- `report-api/`: microservicio independiente de informes.
  - `app/main.py`: endpoint y `GET /health`.
  - `app/models.py`: modelos Pydantic.
  - `app/report_builder.py`: generación CSV/JSON.
  - `tests/test_report_builder.py`: tests unitarios.
- Frontend: `java-api/src/main/resources/static/` (`app.js`, `subject.js`, HTML y CSS).

## 3. Modelo de datos

- `Student`: `id`, `name`, `studyHours`, `enrollments`.
- `Enrollment`: `subjectId`, `studyHours`, `course`, `academicYear`, `grade`.
- `Subject`: `id`, `name`, `difficulty`.

Tablas MySQL: `students`, `subjects`, `enrollments`.
La tabla `enrollments` tiene claves foráneas con `ON DELETE CASCADE`.
`Database.ensureColumn()` añade `course`, `academic_year` y `grade` si la tabla ya existía.

Dificultad canónica en BD, Java, Python y API: `EASY`, `MEDIUM`, `HARD`.
La UI muestra etiquetas en español (`Fácil`, `Media`, `Difícil`) sin cambiar los valores.

Fórmula de predicción:

```text
max(0, min(10, study_hours * 1.5 * factor))
factor: EASY=1.2, MEDIUM=1.0, HARD=0.8
```

## 4. Patrón Adapter (informes)

- `ReportGenerator`: interfaz estable que consume el backend.
- `PythonReportAdapter`: implementa la interfaz y transforma Java → Python.
- `ReportClient`: único punto que conoce la URL y el JSON de Python.
- `ReportService`: construye `AcademicReport` desde `Student` y `Subject`.
- `StudentController` depende de `ReportService`, no de `ReportClient`.

Mapeo Adapter:

| Java (`AcademicReport`/`ReportRecord`) | Python (`ReportRequest`/`ReportRecord`) |
| --- | --- |
| `studentId` | `student` |
| `studentName` | `name` |
| `academicYear` | `academic_year` |
| `subject` | `subject` |
| `course` | `course` |
| `academicYear` | `academic_year` (registro) |
| `grade` | `score` |

## 5. Contratos HTTP

Java:

- `GET /students`, `GET /students/{id}`, `POST /students`, `PUT /students/{id}`, `DELETE /students/{id}`.
- `GET /students/{id}/prediction`.
- `GET /students/{id}/subjects/{subjectId}/prediction`.
- `GET /students/{id}/report?format=csv|json` → descarga con `Content-Disposition`.
- `GET /api/subjects`, `POST /api/subjects` (`difficulty`: `EASY|MEDIUM|HARD`).
- `GET /subjects` → vista web.
- Errores: `400` entrada inválida, `404` no encontrado, `405` método no permitido,
  `503` microservicio Python no disponible.

Predicción (`:8000`):

```json
POST /predict
{"study_hours": 5, "difficulty": "MEDIUM"}
→ {"predicted_grade": 7.5}
```

Informes (`:8001`):

```json
POST /reports/csv|json
{
  "student": 1,
  "name": "Ana",
  "academic_year": "2025-2026",
  "records": [
    {"subject": "Algoritmos", "course": "1º", "academic_year": "2025-2026", "score": 8.5}
  ]
}
→ {"filename": "report_1.csv", "media_type": "...", "content": "..."}
```

## 6. Ejecución

Docker (recomendado):

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f java-api
docker compose down
```

URLs: <http://localhost:8080>, <http://localhost:8000/docs>, <http://localhost:8001/docs>.

Local:

```bash
cd python-api && python -m uvicorn app.main:app --reload --port 8000
cd report-api && python -m uvicorn app.main:app --reload --port 8001
cd java-api && mvn compile exec:java
```

Variables relevantes en Docker: `PYTHON_SERVICE_URL`, `REPORT_SERVICE_URL`,
`DB_URL`, `DB_USER`, `DB_PASSWORD`. Por defecto en local:
`http://localhost:8000`, `http://localhost:8001`,
`jdbc:mysql://localhost:3306/studentdb`, `app`, `app`.

## 7. Verificación realizada

- `docker compose up --build -d`: los 4 servicios arrancan; Java espera a MySQL y a reportes.
- Compilación Java correcta con Maven y Java 21.
- `cd report-api && python -m unittest discover -s tests -v`: 2 tests OK.
- `POST /predict` con 5 horas y `MEDIUM`: `{"predicted_grade":7.5}`.
- Predicción por asignatura con 12 horas y `MEDIUM`: `10.0` (limitada por el máximo).
- Informes CSV y JSON por Java: `200`, `Content-Disposition` correcto y contenido generado por Python.
- Formato inválido: `400`. Servicio de informes caído: `503`; predicciones siguen funcionando.
- Datos de prueba actuales: asignatura `Algoritmos` (id 1), estudiantes `Ana` (id 2)
  y `Luis` (id 9). Existía previamente el estudiante `raul` (id 1).

## 8. Particularidades del entorno

- `AGENTS.md` y `CONTEXT.md` están en `.gitignore`: son locales, no se comparten por Git.
- Maven no está en `PATH`. En este equipo:
  `JAVA_HOME="/c/Program Files/Apache NetBeans/jdk"` y
  `/c/Program Files/Apache NetBeans/java/maven/bin/mvn`.
- No hay `node` disponible; el JavaScript se revisa manualmente.
- El volumen de MySQL puede conservar tablas antiguas; `ensureColumn()` las migra.
- El healthcheck de `report-service` usa `urllib` desde Python.

## 9. Pendientes

- Hacer commit de HU-05 (solo si se solicita).
- Opcional: limpiar los datos de prueba (`Algoritmos`, `Ana`, `Luis`).
- Opcional: añadir tests Java para el Adapter y el endpoint de informes.
