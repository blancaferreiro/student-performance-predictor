# Student Performance Predictor

Aplicación académica para gestionar estudiantes, asignaturas, cursos académicos y matrículas, y consultar predicciones didácticas de notas.

> **Aviso:** las notas predichas son ficticias y se utilizan únicamente con fines docentes. No proceden de un modelo entrenado ni deben utilizarse para evaluar el rendimiento real de una persona.

## 1. Arquitectura

La aplicación está compuesta por tres servicios:

```text
Navegador (HTML, CSS, JavaScript)
            |
            | HTTP en el puerto 8080
            v
API Java (HttpServer y HashMap)
            |                         |
            | POST /predict           | POST /reports/{format}
            | puerto 8000             | puerto 8001
            v                         v
API de predicción (FastAPI)   API de informes (FastAPI)
```

El servicio de informes es independiente del de predicciones y genera los documentos CSV y JSON. Java lo integra mediante el patrón Adapter, de modo que el controlador y el servicio solo dependen de la interfaz `ReportGenerator`.

El navegador se comunica únicamente con Java. Java también sirve el frontend, por lo que el ejemplo utiliza un único origen en el navegador y no necesita configuración CORS. Gson es la única dependencia de Java; Maven la descarga y proporciona comandos reproducibles para compilar y ejecutar el proyecto, pero Maven no forma parte de la lección.

## 2. Requisitos

Para ejecutar el proyecto de la forma recomendada:

- Docker Desktop instalado y abierto (Mac o Windows), o Docker Engine con Docker Compose.
- Git, si se va a trabajar con el repositorio compartido.
- Un navegador web actualizado.

No es necesario instalar Java, Maven, Python ni MySQL localmente si se utiliza Docker Compose.

## 3. Arrancar el proyecto

1. Descomprimir el ZIP.
2. Abrir la carpeta `student-performance-predictor-dev-aitor` en VS Code o en una terminal.
3. Asegurarse de que Docker Desktop está iniciado.
4. Desde la carpeta raíz, donde está `compose.yaml`, ejecutar:

```bash
docker compose up -d --build
```

En la segunda terminal, inicia el servicio de informes:

```powershell
cd report-api
python -m venv .venv
.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8001
```

En macOS o Linux:

```bash
cd report-api
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8001
```

En la tercera terminal, inicia Java:

Comprobar el estado:

```bash
docker compose ps
```

La base de datos MySQL debe aparecer como `healthy`; los servicios Java y Python deben estar iniciados.

Abrir la aplicación en el navegador:

- Aplicación web: <http://localhost:8080>
- Documentación interactiva de la API de predicción: <http://localhost:8000/docs>
- Documentación interactiva de la API de informes: <http://localhost:8001/docs>

## 4. Detener y volver a iniciar

Detener los contenedores sin borrar los datos de MySQL:

```bash
docker compose down
```

Volver a iniciar:

```bash
docker compose up -d
```

Abre la aplicación en <http://localhost:8080>. La documentación interactiva del servicio de predicción continúa disponible en <http://localhost:8000/docs> y la del servicio de informes en <http://localhost:8001/docs>. Dentro de Docker, Java utiliza los nombres de servicio `python-service` y `report-service` para comunicarse con los microservicios.

```bash
docker compose up -d --build
```

> **Importante sobre los datos:** MySQL utiliza el volumen persistente `mysql-data`. `docker compose down` conserva ese volumen. No ejecutar `docker compose down -v` salvo que se quieran borrar también los datos de la base de datos.

## 5. Funcionalidades de matrículas (HU-02)

Cada matrícula relaciona un estudiante, una asignatura y un curso académico, e incluye:

- Horas de estudio (valor igual o superior a cero).
- Nota opcional, entre 0 y 10.
- Posibilidad de matricular al mismo estudiante en la misma asignatura en cursos académicos diferentes.
- Conservación de las matrículas históricas al actualizar un estudiante sin volver a enviar todas sus matrículas.
- Conservación de una nota existente cuando una actualización envía `grade: null`.
- Actualización de la nota cuando se envía una nueva calificación válida.
- Predicción por matrícula, identificando estudiante, asignatura y curso académico.

La clave primaria de la tabla `enrollments` es la combinación `(student_id, subject_id, course_id)`. Por ello, no se permiten dos matrículas de la misma asignatura para el mismo estudiante dentro del mismo curso, pero sí en cursos diferentes.

## 6. Rutas principales de la API

### Estudiantes

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/students` | Listar estudiantes con sus matrículas |
| GET | `/students/{id}` | Consultar un estudiante |
| POST | `/students` | Crear un estudiante |
| PUT | `/students/{id}` | Actualizar un estudiante y añadir/actualizar matrículas |
| DELETE | `/students/{id}` | Eliminar un estudiante |
| GET | `/students/{id}/prediction` | Predicción general del estudiante |
| GET | `/students/{id}/subjects/{subjectId}/courses/{courseId}/prediction` | Predicción de una matrícula concreta |

Ejemplo de cuerpo JSON para crear un estudiante con una matrícula:

```json
{
  "name": "Ana",
  "studyHours": 5,
  "enrollments": [
    {
      "subjectId": 1,
      "courseId": 1,
      "studyHours": 12,
      "grade": 7.5
    }
  ]
}
```

Para una matrícula todavía sin calificación, enviar `"grade": null`.

### Asignaturas y cursos

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/subjects` | Listar asignaturas |
| POST | `/api/subjects` | Crear asignatura |
| GET | `/api/courses` | Listar cursos académicos |
| POST | `/api/courses` | Crear curso académico |

Las interfaces de gestión están disponibles en `/subjects` y `/courses`.

## 7. Configuración de Docker y base de datos

La configuración de desarrollo se encuentra en `compose.yaml`.

- Java API: puerto `8080`.
- Python API: puerto `8000`.
- MySQL: puerto `3306`.
- Base de datos: `studentdb`.
- Usuario de aplicación: `app`.

Las credenciales incluidas en `compose.yaml` son valores locales de desarrollo. No deben reutilizarse en un despliegue público o de producción.

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8000/predict -ContentType "application/json" -Body '{"study_hours":5,"difficulty":"MEDIUM"}'
```

## 8. Solución de problemas

Para descargar un informe desde Java:

```bash
curl "http://localhost:8080/students/1/report?format=csv" -o report_1.csv
curl "http://localhost:8080/students/1/report?format=json" -o report_1.json
```

El informe incluye los datos del estudiante y sus matrículas con curso, año académico y nota. El contenido final lo genera siempre el microservicio Python de informes.

## Puntos de control didácticos

1. Comprobar que Docker Desktop está iniciado.
2. Ejecutar `docker compose ps` desde la carpeta raíz.
3. Consultar los registros con:

```bash
docker compose logs --tail=100 java-api
docker compose logs --tail=100 mysql
docker compose logs --tail=100 python-service
```

**Se ha modificado código y no aparecen los cambios:**

```bash
docker compose up -d --build
```

Después, recargar el navegador sin caché (Cmd + Shift + R en Mac; Ctrl + Shift + R en Windows/Linux).

| Método | Ruta | Estado correcto | Propósito |
| --- | --- | --- | --- |
| GET | `/students` | `200` | Listar estudiantes |
| GET | `/students/{id}` | `200` | Consultar un estudiante |
| POST | `/students` | `201` | Crear un estudiante |
| PUT | `/students/{id}` | `200` | Actualizar un estudiante |
| DELETE | `/students/{id}` | `204` | Eliminar un estudiante |
| GET | `/students/{id}/prediction` | `200` | Solicitar una predicción ficticia |
| POST | `/predict` de Python | `200` | Calcular una predicción ficticia |
| GET | `/api/subjects` | `200` | Listar asignaturas |
| POST | `/api/subjects` | `201` | Crear una asignatura (`name`, `difficulty`: `EASY`, `MEDIUM` o `HARD`) |
| GET | `/students/{id}/subjects/{subjectId}/prediction` | `200` | Predicción según las horas de la matrícula y la dificultad |
| GET | `/students/{id}/report?format=csv\|json` | `200` | Descargar el informe académico del estudiante |
| POST | `/reports/{format}` de Python | `200` | Generar el contenido de un informe (`csv` o `json`) |
| GET | `/subjects` | `200` | Vista web de gestión de asignaturas |

Java devuelve `400` cuando la entrada no es válida, `404` para un estudiante o una ruta desconocidos, `405` para un método no permitido y `503` cuando el servicio de Python correspondiente no está disponible.

Las matrículas incluyen los campos opcionales `course`, `academicYear` y `grade` (entre 0 y 10). Estos datos alimentan los informes CSV y JSON generados por `report-api`.

## 10. Trabajo en equipo

Cada compañero puede descomprimir el proyecto, abrir la carpeta raíz y ejecutar Docker Compose siguiendo el apartado 3. Para compartir cambios de código, utilizar la rama Git acordada por el equipo y realizar commits antes de integrar cambios.
