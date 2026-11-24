const API_URL = "/api/courses";

const form = document.getElementById("course-form");
const formTitle = document.getElementById("form-title");
const idInput = document.getElementById("course-id");
const nameInput = document.getElementById("course-name");
const startDateInput = document.getElementById("course-start-date");
const endDateInput = document.getElementById("course-end-date");
const submitBtn = document.getElementById("submit-btn");
const cancelEditBtn = document.getElementById("cancel-edit");
const courseList = document.getElementById("course-list");
const message = document.getElementById("message");

form.addEventListener("submit", saveCourse);
cancelEditBtn.addEventListener("click", resetForm);

loadCourses();

async function loadCourses() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            showMessage("Could not load academic courses.", true);
            return;
        }
        renderCourses(await response.json());
    } catch (error) {
        showMessage("Could not load academic courses.", true);
    }
}

async function saveCourse(event) {
    event.preventDefault();

    const payload = {
        name: nameInput.value.trim(),
        startDate: startDateInput.value,
        endDate: endDateInput.value
    };

    if (!payload.name || !payload.startDate || !payload.endDate) {
        showMessage("Name, start date and end date are required.", true);
        return;
    }

    if (payload.endDate <= payload.startDate) {
        showMessage("End date must be after start date.", true);
        return;
    }

    const editingId = idInput.value;
    const isEditing = editingId !== "";
    const url = isEditing ? `${API_URL}/${editingId}` : API_URL;
    const method = isEditing ? "PUT" : "POST";

    try {
        const response = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            await showApiError(response);
            return;
        }
        resetForm();
        showMessage(isEditing ? "Academic course updated." : "Academic course saved.", false);
        await loadCourses();
    } catch (error) {
        showMessage("Could not save the academic course.", true);
    }
}

function startEdit(course) {
    idInput.value = course.id;
    nameInput.value = course.name;
    startDateInput.value = course.startDate;
    endDateInput.value = course.endDate;

    formTitle.textContent = "Edit academic course";
    submitBtn.textContent = "Update course";
    cancelEditBtn.classList.remove("hidden");
    showMessage("", false);
    nameInput.focus();
}

function resetForm() {
    form.reset();
    idInput.value = "";
    formTitle.textContent = "Add academic course";
    submitBtn.textContent = "Save course";
    cancelEditBtn.classList.add("hidden");
}

async function deleteCourse(course) {
    if (!confirm(`Delete academic course "${course.name}"?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${course.id}`, {
            method: "DELETE"
        });
        if (!response.ok) {
            await showApiError(response);
            return;
        }
        if (idInput.value === String(course.id)) {
            resetForm();
        }
        showMessage("Academic course deleted.", false);
        await loadCourses();
    } catch (error) {
        showMessage("Could not delete the academic course.", true);
    }
}

function renderCourses(courses) {
    courseList.replaceChildren();

    if (courses.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No academic courses registered yet.";
        courseList.appendChild(empty);
        return;
    }

    for (const course of courses) {
        const card = document.createElement("article");
        card.className = "student-card";

        const title = document.createElement("strong");
        title.textContent = course.name;

        const dates = document.createElement("p");
        dates.textContent = `From ${course.startDate} to ${course.endDate}`;

        const actions = document.createElement("div");
        actions.className = "form-actions";

        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.className = "secondary";
        editBtn.textContent = "Edit";
        editBtn.addEventListener("click", () => startEdit(course));

        const deleteBtn = document.createElement("button");
        deleteBtn.type = "button";
        deleteBtn.className = "secondary";
        deleteBtn.textContent = "Delete";
        deleteBtn.addEventListener("click", () => deleteCourse(course));

        actions.appendChild(editBtn);
        actions.appendChild(deleteBtn);

        card.appendChild(title);
        card.appendChild(dates);
        card.appendChild(actions);
        courseList.appendChild(card);
    }
}

async function showApiError(response) {
    try {
        const data = await response.json();
        showMessage(data.error || `Server error (${response.status}).`, true);
    } catch (error) {
        showMessage(`Server error (${response.status}).`, true);
    }
}

function showMessage(text, isError) {
    message.textContent = text;
    message.classList.toggle("error", isError);
}
