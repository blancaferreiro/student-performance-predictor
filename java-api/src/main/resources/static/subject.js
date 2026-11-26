// Ruta de la API REST de asignaturas (HU-01). Cámbiala aquí si el backend usa otra.
const API_URL = "/api/subjects";

const form = document.getElementById("subject-form");
const nameInput = document.getElementById("subject-name");
const difficultyInput = document.getElementById("subject-difficulty");
const subjectList = document.getElementById("subject-list");
const message = document.getElementById("message");

// Texto visible para cada valor que entiende el backend.
const DIFFICULTY_LABELS = { EASY: "Fácil", MEDIUM: "Media", HARD: "Difícil" };

form.addEventListener("submit", saveSubject);
loadSubjects();

// GET /api/subjects -> lista de asignaturas
async function loadSubjects() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            showMessage("Could not load subjects.", true);
            return;
        }
        renderSubjects(await response.json());
    } catch (error) {
        showMessage("Could not load subjects.", true);
    }
}

// POST /api/subjects con {name, difficulty}
async function saveSubject(event) {
    event.preventDefault(); // evita que el navegador recargue la página

    const subject = {
        name: nameInput.value.trim(),
        difficulty: difficultyInput.value
    };

    // trim() puede dejar el nombre vacío aunque "required" se haya cumplido (solo espacios).
    if (subject.name === "" || subject.difficulty === "") {
        showMessage("Name and difficulty are required.", true);
        return;
    }

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(subject)
        });
        if (!response.ok) {
            await showApiError(response);
            return;
        }
        form.reset();
        showMessage("Subject saved.", false);
        await loadSubjects();
    } catch (error) {
        showMessage("Could not save the subject.", true);
    }
}

function renderSubjects(subjects) {
    subjectList.replaceChildren();

    if (subjects.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No subjects yet.";
        subjectList.appendChild(empty);
        return;
    }

    for (const subject of subjects) {
        const card = document.createElement("article");
        card.className = "student-card"; // reutiliza el estilo existente

        const name = document.createElement("strong");
        name.textContent = subject.name;

        const difficulty = document.createElement("p");
        difficulty.textContent =
            "Difficulty: " + (DIFFICULTY_LABELS[subject.difficulty] || subject.difficulty);

        card.appendChild(name);
        card.appendChild(difficulty);
        subjectList.appendChild(card);
    }
}

async function showApiError(response) {
    try {
        const data = await response.json();
        showMessage(data.error, true);
    } catch (error) {
        showMessage("Server error (" + response.status + ").", true);
    }
}

function showMessage(text, isError) {
    message.textContent = text;
    message.classList.toggle("error", isError);
}