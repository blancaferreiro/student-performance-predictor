# Student Performance Predictor

Aplicación académica para gestionar estudiantes, asignaturas, cursos académicos y matrículas, y consultar predicciones didácticas de notas.

> **Aviso:** las notas predichas son ficticias y se utilizan únicamente con fines docentes. No proceden de un modelo entrenado ni deben utilizarse para evaluar el rendimiento real de una persona.

## 1. Arquitectura

La aplicación está compuesta por tres servicios:

```text
Navegador (HTML, CSS y JavaScript)
              |
              | HTTP :8080
              v
       API Java (backend)
          |          |
          |          +------> MySQL 8.4 (persistencia)
          |
          | HTTP :8000
          v
    API Python (FastAPI)
```

- **Frontend:** HTML, CSS y JavaScript servido por la API Java.
- **Backend Java:** API HTTP, controladores, servicios, repositorios y reglas de negocio.
- **MySQL:** almacena estudiantes, asignaturas, cursos y matrículas.
- **Python/FastAPI:** servicio independiente que calcula la predicción didáctica.
- **Docker Compose:** inicia los tres servicios y configura su comunicación.

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

La primera ejecución puede tardar unos minutos mientras Docker descarga imágenes y construye los servicios.

Comprobar el estado:

```bash
docker compose ps
```

La base de datos MySQL debe aparecer como `healthy`; los servicios Java y Python deben estar iniciados.

Abrir la aplicación en el navegador:

- Aplicación web: <http://localhost:8080>
- Gestión de asignaturas: <http://localhost:8080/subjects>
- Gestión de cursos: <http://localhost:8080/courses>
- Documentación de FastAPI: <http://localhost:8000/docs>

## 4. Detener y volver a iniciar

Detener los contenedores sin borrar los datos de MySQL:

```bash
docker compose down
```

Volver a iniciar:

```bash
docker compose up -d
```

Reconstruir las imágenes después de modificar código:

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

La aplicación crea las tablas al arrancar si todavía no existen. Esta inicialización crea tablas nuevas; no es un sistema de migraciones para transformar automáticamente una base de datos antigua con otro esquema. Para una instalación limpia, se puede iniciar con un volumen MySQL nuevo.

## 8. Solución de problemas

**No se abre `localhost:8080`:**

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

**MySQL no está listo:** esperar a que `docker compose ps` indique `healthy` para el servicio `mysql`. La API Java espera a que la base de datos esté saludable antes de arrancar.

## 9. Estructura del proyecto

```text
student-performance-predictor-dev-aitor/
├── compose.yaml
├── README.md
├── requests.http
├── flujo.png
├── java-api/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/edu/example/studentperformance/
│       └── resources/static/
└── python-api/
    ├── Dockerfile
    ├── requirements.txt
    └── app/
```

## 10. Trabajo en equipo

Cada compañero puede descomprimir el proyecto, abrir la carpeta raíz y ejecutar Docker Compose siguiendo el apartado 3. Para compartir cambios de código, utilizar la rama Git acordada por el equipo y realizar commits antes de integrar cambios.
