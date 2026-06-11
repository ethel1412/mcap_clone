// Global variables
let programmeOfferedId = null;
let programmeName = '';
let departmentName = '';
let semesterToDelete = null;
let currentSemesterForSubjects = null;

document.addEventListener('DOMContentLoaded', function () {
    initializePage();
    setupEventListeners();
    if (programmeOfferedId) {
    	loadProgrammeSemesters();
	}
});

function initializePage() {
    const urlParams = new URLSearchParams(window.location.search);
    programmeOfferedId = urlParams.get('programmeOfferedId');
    programmeName = urlParams.get('programmeName');
    departmentName = urlParams.get('departmentName');

    const input = document.getElementById('programmeOfferedId');
    if (input) input.value = programmeOfferedId;
}

function setupEventListeners() {
    document.getElementById('addSemesterForm')?.addEventListener('submit', handleAddSemester);
    document.getElementById('addSubjectsForm')?.addEventListener('submit', handleAddSubjects);
    document.getElementById('confirmDeleteSemesterBtn')?.addEventListener('click', handleDeleteSemester);
}

// ================= BUTTON HELPERS =================
function setButtonLoading(btn, text) {
    if (!btn._originalContent) {
        btn._originalContent = Array.from(btn.childNodes).map(n => n.cloneNode(true));
    }

    btn.disabled = true;
    btn.replaceChildren();

    const icon = document.createElement("i");
    icon.className = "fas fa-spinner fa-spin me-2";

    btn.append(icon, document.createTextNode(text));
}

function restoreButton(btn) {
    if (btn._originalContent) {
        btn.replaceChildren(...btn._originalContent);
    }
    btn.disabled = false;
}

// ================= LOAD SEMESTERS =================
async function loadProgrammeSemesters() {
    if (!programmeOfferedId) return; // ✅ STOP early

    try {
        const res = await axios.get(`/semesters/data/by-programme/${programmeOfferedId}`);
        renderSemesters(res.data || []);
    } catch (e) {
        console.error(e);
    }
}

function renderSemesters(semesters) {
    const container = document.getElementById('semestersContainer');
    container.replaceChildren();

    semesters.forEach(s => {
        const card = createSemesterCardElement(s);
        container.appendChild(card);
        setTimeout(() => loadSemesterSubjects(s.semesterId), 50);
    });
}

function createSemesterCardElement(s) {
    const titleText = s.semesterName || `Semester ${s.semesterNumber}`;

    const card = document.createElement("div");
    card.className = "semester-card";

    const title = document.createElement("h5");
    title.textContent = titleText;

    const addBtn = document.createElement("button");
    addBtn.textContent = "+";
    addBtn.addEventListener("click", () => {
        showAddSubjectsModal(s.semesterId, titleText);
    });

    const delBtn = document.createElement("button");
    delBtn.textContent = "🗑";
    delBtn.addEventListener("click", () => {
        showDeleteSemesterModal(s.semesterId, titleText);
    });

    const table = document.createElement("table");
    const tbody = document.createElement("tbody");
    tbody.id = `subjects-${s.semesterId}`;
    table.appendChild(tbody);

    card.append(title, addBtn, delBtn, table);

    return card;
}

// ================= SUBJECTS =================
async function loadSemesterSubjects(id) {
    try {
        const res = await axios.get(`/subject-assignments/data/by-semester/${id}`);
        renderSemesterSubjects(id, res.data || []);
    } catch (e) {
        console.error(e);
    }
}

function renderSemesterSubjects(id, subjects) {
    const tbody = document.getElementById(`subjects-${id}`);
    tbody.replaceChildren();

    if (!subjects.length) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.textContent = "No subjects";
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    subjects.forEach((s, i) => {
        const tr = document.createElement("tr");

        const td1 = document.createElement("td");
        td1.textContent = i + 1;

        const td2 = document.createElement("td");
        td2.textContent = s.subjectName;

        const td3 = document.createElement("td");

        const btn = document.createElement("button");
        btn.textContent = "Delete";
        btn.addEventListener("click", () => {
            confirmRemoveSubject(s.assignmentId, s.subjectName, id, btn);
        });

        td3.appendChild(btn);

        tr.append(td1, td2, td3);
        tbody.appendChild(tr);
    });
}

// ================= REMOVE SUBJECT =================
async function confirmRemoveSubject(id, name, semesterId, btn) {
    if (!confirm(`Remove "${name}"?`)) return;

    try {
        setButtonLoading(btn, "");

        await axios.delete(`/subject-assignments/data/${id}`);

        showToast('success', 'Removed');
        await loadSemesterSubjects(semesterId);

    } catch (e) {
        console.error(e);
        showToast('error', 'Failed');
        restoreButton(btn);
    }
}

// ================= ADD SEMESTER =================
async function handleAddSemester(e) {
    e.preventDefault();

    const form = e.target;
    const btn = document.getElementById('submitSemesterBtn');

    if (!form.checkValidity()) return;

    try {
        setButtonLoading(btn, "Adding...");

        const data = new FormData(form);

        await axios.post('/semesters/data', {
            programmeOfferedId: parseInt(data.get('programmeOfferedId')),
            semesterNumber: parseInt(data.get('semesterNumber'))
        });

        showToast('success', 'Added');

    } catch (e) {
        console.error(e);
    } finally {
        restoreButton(btn);
    }
}

// ================= ADD SUBJECTS =================
async function handleAddSubjects(e) {
    e.preventDefault();

    const btn = document.getElementById('submitSubjectsBtn');

    try {
        setButtonLoading(btn, "Adding...");

        await axios.post('/subject-assignments/data', {});

        showToast('success', 'Added');

    } catch (e) {
        console.error(e);
    } finally {
        restoreButton(btn);
    }
}

// ================= DELETE SEMESTER =================
async function handleDeleteSemester() {
    if (!semesterToDelete) return;

    const btn = document.getElementById('confirmDeleteSemesterBtn');

    try {
        setButtonLoading(btn, "Deleting...");

        await axios.delete(`/semesters/data/${semesterToDelete}`);

        showToast('success', 'Deleted');

    } catch (e) {
        console.error(e);
    } finally {
        restoreButton(btn);
        semesterToDelete = null;
    }
}

// ================= UTIL =================
function showToast(type, msg) {
    console.log(type, msg);
}

// expose
window.showAddSubjectsModal = () => {};
window.showDeleteSemesterModal = (id) => { semesterToDelete = id; };
window.confirmRemoveSubject = confirmRemoveSubject;