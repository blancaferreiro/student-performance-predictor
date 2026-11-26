const form = document.getElementById("student-form");
const studentIdInput = document.getElementById("student-id");
const nameInput = document.getElementById("student-name");
const studyHoursInput = document.getElementById("study-hours");
const formTitle = document.getElementById("form-title");
const cancelEditButton = document.getElementById("cancel-edit");
const addEnrollmentButton = document.getElementById("add-enrollment");
const studentList = document.getElementById("student-list");
const message = document.getElementById("message");
const enrollmentContainer = document.getElementById("subject-checkboxes");

let currentStudents = [];
let currentSubjects = [];
let currentCourses = [];
let currentEnrollments = [];
const DIFFICULTY_LABELS = { EASY: "Fácil", MEDIUM: "Media", HARD: "Difícil" };

form.addEventListener("submit", saveStudent);
cancelEditButton.addEventListener("click", resetForm);
addEnrollmentButton.addEventListener("click", addEnrollment);

async function initializePage() {
    await loadSubjects();
    await loadCourses();
    await loadStudents();
    renderEnrollmentRows();
}

initializePage();

async function loadSubjects() {
    try {
        const response = await fetch("/api/subjects");

        if (!response.ok) {
            throw new Error("Could not load subjects");
        }

        currentSubjects = await response.json();

    } catch (error) {
        showMessage("Could not load subjects.", true);
    }
}

async function loadCourses() {
    try {
        const response = await fetch("/api/courses");

        if (!response.ok) {
            throw new Error("Could not load courses");
        }

        currentCourses = await response.json();

    } catch (error) {
        showMessage("Could not load academic courses.", true);
    }
}

function addEnrollment() {
    if (currentSubjects.length === 0) {
        showMessage("Create a subject before adding an enrollment.", true);
        return;
    }

    if (currentCourses.length === 0) {
        showMessage("Create an academic course before adding an enrollment.", true);
        return;
    }

    currentEnrollments.push({
        subjectId: currentSubjects[0].id,
        courseId: currentCourses[0].id,
        studyHours: "",
        grade: ""
    });

    renderEnrollmentRows();
}

function renderEnrollmentRows() {
    enrollmentContainer.replaceChildren();

    if (currentEnrollments.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No enrollments added yet.";
        enrollmentContainer.appendChild(empty);
        return;
    }

    currentEnrollments.forEach((enrollment, index) => {
        const row = document.createElement("div");
        row.className = "enrollment-item";

        const content = document.createElement("div");
        content.style.display = "flex";
        content.style.flexDirection = "column";
        content.style.gap = "0.5rem";
        content.style.width = "100%";

        const title = document.createElement("strong");
        title.textContent = "Enrollment " + (index + 1);

        const subjectLabel = document.createElement("label");
        subjectLabel.textContent = "Subject";

        const subjectSelect = document.createElement("select");

        currentSubjects.forEach(subject => {
            const option = document.createElement("option");
            option.value = subject.id;
            option.textContent =
                subject.name + " (" +
                (DIFFICULTY_LABELS[subject.difficulty] || subject.difficulty) +
                ")";
            subjectSelect.appendChild(option);
        });

        subjectSelect.value = enrollment.subjectId;

        subjectSelect.addEventListener("change", () => {
            currentEnrollments[index].subjectId = Number(subjectSelect.value);
        });

        const courseLabel = document.createElement("label");
        courseLabel.textContent = "Academic course";

        const courseSelect = document.createElement("select");

        currentCourses.forEach(course => {
            const option = document.createElement("option");
            option.value = course.id;
            option.textContent =
                course.name +
                (course.startDate && course.endDate
                    ? " (" + course.startDate + " - " + course.endDate + ")"
                    : "");
            courseSelect.appendChild(option);
        });

        courseSelect.value = enrollment.courseId;

        courseSelect.addEventListener("change", () => {
            currentEnrollments[index].courseId = Number(courseSelect.value);
        });

        const hoursLabel = document.createElement("label");
        hoursLabel.textContent = "Study hours";

        const hoursInput = createEnrollmentInput("number", "Study hours");
        hoursInput.min = "0";
        hoursInput.step = "0.5";
        hoursInput.value = enrollment.studyHours;

        hoursInput.addEventListener("input", () => {
            currentEnrollments[index].studyHours = hoursInput.value;
        });

        const gradeLabel = document.createElement("label");
        gradeLabel.textContent = "Grade (0-10)";

        const gradeInput = createEnrollmentInput("number", "Grade (0-10)");
        gradeInput.min = "0";
        gradeInput.max = "10";
        gradeInput.step = "0.1";
        gradeInput.value = enrollment.grade;

        gradeInput.addEventListener("input", () => {
            currentEnrollments[index].grade = gradeInput.value;
        });

        const removeButton = createButton("Remove enrollment", () => {
            currentEnrollments.splice(index, 1);
            renderEnrollmentRows();
        });

        content.appendChild(title);
        content.appendChild(subjectLabel);
        content.appendChild(subjectSelect);
        content.appendChild(courseLabel);
        content.appendChild(courseSelect);
        content.appendChild(hoursLabel);
        content.appendChild(hoursInput);
        content.appendChild(gradeLabel);
        content.appendChild(gradeInput);
        content.appendChild(removeButton);

        row.appendChild(content);
        enrollmentContainer.appendChild(row);
    });
}

function createEnrollmentInput(type, placeholder) {
    const input = document.createElement("input");
    input.type = type;
    input.placeholder = placeholder;
    return input;
}

async function loadStudents() {
    try {
        const response = await fetch("/students");

        if (!response.ok) {
            throw new Error("Could not load students");
        }

        currentStudents = await response.json();
        renderStudents(currentStudents);

    } catch (error) {
        showMessage("Could not load students.", true);
    }
}

async function saveStudent(event) {
    event.preventDefault();

    const id = studentIdInput.value;
    const enrollments = [];

    for (const enrollment of currentEnrollments) {
        const hours = Number(enrollment.studyHours);

        if (
            enrollment.studyHours === "" ||
            !Number.isFinite(hours) ||
            hours < 0
        ) {
            showMessage("Enter valid study hours for every enrollment.", true);
            return;
        }

        let grade = null;

        if (enrollment.grade !== "" && enrollment.grade != null) {
            grade = Number(enrollment.grade);

            if (
                !Number.isFinite(grade) ||
                grade < 0 ||
                grade > 10
            ) {
                showMessage("Grades must be between 0 and 10.", true);
                return;
            }
        }

        enrollments.push({
            subjectId: Number(enrollment.subjectId),
            courseId: Number(enrollment.courseId),
            studyHours: hours,
            grade: grade
        });
    }

    const student = {
        name: nameInput.value.trim(),
        studyHours: Number(studyHoursInput.value),
        enrollments: enrollments
    };

    if (student.name === "") {
        showMessage("Enter the student's name.", true);
        return;
    }

    if (
        !Number.isFinite(student.studyHours) ||
        student.studyHours < 0
    ) {
        showMessage("Enter valid general study hours.", true);
        return;
    }

    const url = id === "" ? "/students" : "/students/" + id;
    const method = id === "" ? "POST" : "PUT";

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(student)
        });

        if (!response.ok) {
            await showApiError(response);
            return;
        }

        resetForm();
        showMessage("Student saved successfully.", false);
        await loadStudents();

    } catch (error) {
        showMessage("Could not save the student.", true);
    }
}

function renderStudents(students) {
    studentList.replaceChildren();

    if (students.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "No students yet.";
        studentList.appendChild(empty);
        return;
    }

    students.forEach(student => {
        const card = document.createElement("article");
        card.className = "student-card";

        const name = document.createElement("strong");
        name.textContent = student.name;

        const hours = document.createElement("p");
        hours.textContent = "Study hours: " + student.studyHours;

        card.appendChild(name);
        card.appendChild(hours);

        const enrollmentsTitle = document.createElement("h4");
        enrollmentsTitle.textContent = "Enrollments";
        card.appendChild(enrollmentsTitle);

        if (!student.enrollments || student.enrollments.length === 0) {
            const empty = document.createElement("p");
            empty.textContent = "No enrollments.";
            card.appendChild(empty);

        } else {
            student.enrollments.forEach(enrollment => {
                const subject = currentSubjects.find(
                    item => Number(item.id) === Number(enrollment.subjectId)
                );

                const course = currentCourses.find(
                    item => Number(item.id) === Number(enrollment.courseId)
                );

                const subjectName = subject
                    ? subject.name
                    : "Subject " + enrollment.subjectId;

                const details = [subjectName + ": " + enrollment.studyHours + " hours"];

                if (course) {
                    details.push("Course: " + course.name);
                } else if (enrollment.course) {
                    details.push("Course: " + enrollment.course);
                }

                if (enrollment.grade != null) {
                    details.push("Grade: " + enrollment.grade);
                }

                const enrollmentInfo = document.createElement("p");
                enrollmentInfo.textContent = details.join(" · ");

                const prediction = document.createElement("p");
                prediction.className = "prediction";
                prediction.id = getPredictionElementId(
                    student.id,
                    enrollment.subjectId,
                    enrollment.courseId
                );

                const predictButton = document.createElement("button");
                predictButton.type = "button";
                predictButton.textContent = "Predict grade";

                predictButton.addEventListener("click", () => {
                    requestSubjectPrediction(
                        student.id,
                        enrollment.subjectId,
                        enrollment.courseId
                    );
                });

                card.appendChild(enrollmentInfo);
                card.appendChild(predictButton);
                card.appendChild(prediction);
            });
        }

        const actions = document.createElement("div");
        actions.className = "student-actions";

        actions.appendChild(
            createButton("Edit", () => startEdit(student.id))
        );

        actions.appendChild(
            createButton("Delete", () => deleteStudent(student.id))
        );

        actions.appendChild(createButton(
            "Predict general grade",
            () => requestPrediction(student.id)
        ));

        card.appendChild(actions);

        const generalPrediction = document.createElement("p");
        generalPrediction.className = "prediction";
        generalPrediction.id = "prediction-general-" + student.id;

        card.appendChild(generalPrediction);

        const reports = document.createElement("div");
        reports.className = "report-actions";
        reports.appendChild(createReportLink(
            "Report CSV",
            "/students/" + student.id + "/report?format=csv"
        ));
        reports.appendChild(createReportLink(
            "Report JSON",
            "/students/" + student.id + "/report?format=json"
        ));
        card.appendChild(reports);

        studentList.appendChild(card);
    });
}

function getPredictionElementId(studentId, subjectId, courseId) {
    return "prediction-" + studentId + "-" + subjectId + "-" + courseId;
}

function createButton(label, clickHandler) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.addEventListener("click", clickHandler);
    return button;
}

function createReportLink(label, href) {
    const link = document.createElement("a");
    link.className = "report-link";
    link.href = href;
    link.textContent = label;
    link.setAttribute("download", "");
    return link;
}

function startEdit(studentId) {
    const student = currentStudents.find(
        item => Number(item.id) === Number(studentId)
    );

    if (!student) {
        showMessage("Student not found.", true);
        return;
    }

    studentIdInput.value = student.id;
    nameInput.value = student.name;
    studyHoursInput.value = student.studyHours;

    currentEnrollments = (student.enrollments || []).map(enrollment => ({
        subjectId: Number(enrollment.subjectId),
        courseId: Number(enrollment.courseId || (currentCourses[0] ? currentCourses[0].id : 0)),
        studyHours: Number(enrollment.studyHours),
        grade: enrollment.grade == null ? "" : enrollment.grade
    }));

    renderEnrollmentRows();

    formTitle.textContent = "Edit student";
    cancelEditButton.classList.remove("hidden");
    nameInput.focus();
}

async function deleteStudent(studentId) {
    if (!confirm("Are you sure you want to delete this student?")) {
        return;
    }

    try {
        const response = await fetch("/students/" + studentId, {
            method: "DELETE"
        });

        if (!response.ok) {
            await showApiError(response);
            return;
        }

        showMessage("Student deleted.", false);
        await loadStudents();

    } catch (error) {
        showMessage("Could not delete the student.", true);
    }
}

async function requestPrediction(studentId) {
    try {
        const response = await fetch(
            "/students/" + studentId + "/prediction"
        );

        if (!response.ok) {
            await showApiError(response);
            return;
        }

        const prediction = await response.json();

        const result = document.getElementById(
            "prediction-general-" + studentId
        );

        if (result) {
            result.textContent =
                "Predicted grade: " + prediction.predictedGrade;
        }

    } catch (error) {
        showMessage("Could not request a prediction.", true);
    }
}

async function requestSubjectPrediction(
    studentId,
    subjectId,
    courseId
) {
    const result = document.getElementById(
        getPredictionElementId(studentId, subjectId, courseId)
    );

    result.textContent = "Requesting prediction...";

    try {
        const response = await fetch(
            "/students/" + studentId +
            "/subjects/" + subjectId +
            "/courses/" + courseId +
            "/prediction"
        );

        if (!response.ok) {
            const errorData = await response.json();
            result.textContent = errorData.error || "Prediction failed.";
            return;
        }

        const prediction = await response.json();

        result.textContent =
            "Predicted grade: " + prediction.predictedGrade;

    } catch (error) {
        result.textContent = "Could not request a prediction.";
    }
}

function resetForm() {
    form.reset();

    studentIdInput.value = "";
    currentEnrollments = [];

    renderEnrollmentRows();

    formTitle.textContent = "Add student";
    cancelEditButton.classList.add("hidden");
}

async function showApiError(response) {
    let errorMessage = "Request failed.";

    try {
        const errorData = await response.json();
        errorMessage = errorData.error || errorMessage;

    } catch (error) {
    }

    showMessage(errorMessage, true);
}

function showMessage(text, isError) {
    message.textContent = text;

    if (isError) {
        message.classList.add("error");
    } else {
        message.classList.remove("error");
    }
}
