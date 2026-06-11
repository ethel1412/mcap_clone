// ================= CSRF + AXIOS SETUP =================
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

if (csrfToken && csrfHeader) {
    axios.defaults.headers.common[csrfHeader] = csrfToken;
}
axios.defaults.withCredentials = true;

// ================= GLOBAL STATE =================
let admissionWindowId = null;
let programmeLevel = null;
let streamId = null;

let selectedProgrammeId = null;
let selectedProgrammeName = null;

const pageAlertBox = document.getElementById('alert-box');
const modalAlertBox = document.getElementById('criteria-modal-alert');

// ---- Tie-breaker state (fixed fields, reorder only) ----
// [{ field, label, direction, priority }]
let tiebreakerConfig = [];

const FIELD_LABELS = {
    ENTRANCE_SCORE: 'Entrance Exam',
    CLASS12_PERCENT: 'Class XII %',
    UG_PERCENT: 'UG % (for PG)',
    COMMUNITY_CATEGORY: 'Community Category (Caste)',
    DATE_OF_BIRTH: 'Date of Birth'
};

// ================= SUBJECT SELECTION STATE =================
let cuetMeritSubjects = [];     // array of string
let nonCuetMeritSubjects = [];  // array of string

// ================= UTILITIES =================
function showPageAlert(message, type) {
    if (!pageAlertBox) return;

    pageAlertBox.replaceChildren();

    const div = document.createElement("div");
    div.className = `alert alert-${type} alert-dismissible fade show`;

    const icon = document.createElement("i");
    icon.className = `bi bi-${type === 'success' ? 'check-circle' : 'exclamation-triangle'} me-2`;

    const text = document.createTextNode(message);

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "btn-close";
    btn.setAttribute("data-bs-dismiss", "alert");

    div.append(icon, text, btn);
    pageAlertBox.appendChild(div);
}

function showModalAlert(message, type) {
    if (!modalAlertBox) return;

    if (!message) {
        modalAlertBox.replaceChildren();
        return;
    }

    modalAlertBox.replaceChildren();

    const div = document.createElement("div");
    div.className = `alert alert-${type} alert-dismissible fade show`;
    div.setAttribute("role", "alert");

    const text = document.createTextNode(message);

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "btn-close";
    btn.setAttribute("data-bs-dismiss", "alert");

    div.append(text, btn);
    modalAlertBox.appendChild(div);
}

function safeArray(v) {
    return Array.isArray(v) ? v : [];
}

// ================= SUBJECT CHECKBOX HELPERS =================
function resetSubjectSelections() {
    cuetMeritSubjects = [];
    nonCuetMeritSubjects = [];

    const cuetBox = document.getElementById('cuet-subjects-box');
    const nonCuetBox = document.getElementById('noncuet-subjects-box');
	
	if (cuetBox) {
	    cuetBox.replaceChildren();
	    const div = document.createElement("div");
	    div.className = "text-muted small";
	    div.textContent = "Loading...";
	    cuetBox.appendChild(div);
	}
	
	if (nonCuetBox) {
	    nonCuetBox.replaceChildren();
	    const div = document.createElement("div");
	    div.className = "text-muted small";
	    div.textContent = "Loading...";
	    nonCuetBox.appendChild(div);
	}
}

function renderSubjectCheckboxes(containerEl, subjects, selectedValues, checkboxClass, idPrefix) {
    if (!containerEl) return;

    const selected = new Set(safeArray(selectedValues).map(String));
    const list = safeArray(subjects).map(String);

    // clear container
    containerEl.replaceChildren();

    if (!list.length) {
        const div = document.createElement("div");
        div.className = "text-muted small";
        div.textContent = "No subjects available.";
        containerEl.appendChild(div);
        return;
    }

    list.forEach((subj, idx) => {
        const id = `${idPrefix}-${idx}`;
        const isChecked = selected.has(subj);

        const wrapper = document.createElement("div");
        wrapper.className = "form-check";

        const input = document.createElement("input");
        input.className = `form-check-input ${checkboxClass}`;
        input.type = "checkbox";
        input.value = subj;
        input.id = id;
        if (isChecked) input.checked = true;

        const label = document.createElement("label");
        label.className = "form-check-label";
        label.htmlFor = id;
        label.textContent = subj;

        wrapper.appendChild(input);
        wrapper.appendChild(label);

        containerEl.appendChild(wrapper);
    });
}

function readCheckedSubjects(containerEl, checkboxClass) {
    if (!containerEl) return [];
    return [...containerEl.querySelectorAll(`input.${checkboxClass}:checked`)].map(x => x.value);
}

function clearSubjectChecks(containerEl, checkboxClass) {
    if (!containerEl) return;
    containerEl.querySelectorAll(`input.${checkboxClass}:checked`).forEach(cb => cb.checked = false);
}

function escapeHtml(str) {
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function escapeHtmlAttr(str) {
    // for value="" attribute
    return escapeHtml(str);
}

// ================= INITIALIZATION =================
document.addEventListener('DOMContentLoaded', () => {
    const awEl = document.getElementById('admissionWindowId');
    const plEl = document.getElementById('programmeLevel');
    const stEl = document.getElementById('streamId');

    admissionWindowId = awEl ? parseInt(awEl.value, 10) : null;
    programmeLevel = plEl ? plEl.value : null;
    streamId = stEl ? parseInt(stEl.value, 10) : null;

    wireConfigureButtons();

    // Clear buttons inside modal (optional, present in updated HTML)
    const btnCuetClear = document.getElementById('btn-cuet-clear');
    if (btnCuetClear) {
        btnCuetClear.addEventListener('click', () => {
            clearSubjectChecks(document.getElementById('cuet-subjects-box'), 'cuet-chk');
        });
    }
    const btnNonCuetClear = document.getElementById('btn-noncuet-clear');
    if (btnNonCuetClear) {
        btnNonCuetClear.addEventListener('click', () => {
            clearSubjectChecks(document.getElementById('noncuet-subjects-box'), 'noncuet-chk');
        });
    }

    const saveBtn = document.getElementById('btn-save-criteria');
    if (saveBtn) {
        saveBtn.addEventListener('click', onSaveCriteria);
    }

    const startBtn = document.getElementById('btn-start-admission');
    if (startBtn) {
        startBtn.addEventListener('click', onStartAdmissionProcess);
    }

    initMeritListStatus();
});

// ================= CONFIGURE BUTTONS =================
function wireConfigureButtons() {
    document.querySelectorAll('.btn-configure').forEach(btn => {
        btn.addEventListener('click', () => {
            selectedProgrammeId = parseInt(btn.dataset.programmeId, 10);
            selectedProgrammeName = btn.dataset.programmeName;

            document.getElementById('crit-programmeId').value = selectedProgrammeId;
            document.getElementById('crit-programmeName').value = selectedProgrammeName;

            document.getElementById('crit-prog-label').textContent = selectedProgrammeName;

            const statusText = document.getElementById('criteria-status-text');
            if (statusText) {
                statusText.textContent = 'Loading existing criteria (if any)...';
            }

            loadCriteriaForSelected();
        });
    });
}

// ================= TIE-BREAKERS: DEFAULT + REORDER =================
function initDefaultTieBreakers() {
    let base;

    if (programmeLevel === 'UG') {
        base = [
            { field: 'ENTRANCE_SCORE',     direction: 'DESC' },
            { field: 'CLASS12_PERCENT',    direction: 'DESC' },
            { field: 'COMMUNITY_CATEGORY', direction: 'ASC'  },
            { field: 'DATE_OF_BIRTH',      direction: 'ASC'  }
        ];
    } else { // PG
        base = [
            { field: 'ENTRANCE_SCORE',     direction: 'DESC' },
            { field: 'UG_PERCENT',         direction: 'DESC' },
            { field: 'COMMUNITY_CATEGORY', direction: 'ASC'  },
            { field: 'DATE_OF_BIRTH',      direction: 'ASC'  }
        ];
    }

    tiebreakerConfig = base.map((c, i) => ({
        field: c.field,
        label: FIELD_LABELS[c.field] || c.field,
        direction: c.direction,
        priority: i + 1
    }));
}

// ================= SUBJECT LIST LOADERS (CHECKBOX UI) =================
function loadAllowedCuetSubjects() {
    const box = document.getElementById('cuet-subjects-box');
    if (!box || !admissionWindowId || !selectedProgrammeId) return Promise.resolve([]);

    const url = `/admission-criteria/data/allowed-cuet-subjects?admissionWindowId=${admissionWindowId}&programmeId=${selectedProgrammeId}`;

    box.replaceChildren();
	const div = document.createElement("div");
	div.className = "text-muted small";
	div.textContent = "Loading...";
	box.appendChild(div);

    return axios.get(url)
        .then(res => {
            const list = safeArray(res.data).map(String);
            renderSubjectCheckboxes(box, list, cuetMeritSubjects, 'cuet-chk', 'cuet');
            return list;
        })
        .catch(() => {
            renderSubjectCheckboxes(box, [], [], 'cuet-chk', 'cuet');
            return [];
        });
}

function loadNonCuetSubjects() {
    const box = document.getElementById('noncuet-subjects-box');
    if (!box || !admissionWindowId || !selectedProgrammeId) return Promise.resolve([]);

    const url = `/admission-criteria/data/allowed-non-cuet-subjects`
        + `?admissionWindowId=${admissionWindowId}`
        + `&programmeId=${selectedProgrammeId}`;

    box.replaceChildren();
	
	const div = document.createElement("div");
	div.className = "text-muted small";
	div.textContent = "Loading...";
	
	box.appendChild(div);

    return axios.get(url)
        .then(res => {
            const list = safeArray(res.data).map(String);
            renderSubjectCheckboxes(box, list, nonCuetMeritSubjects, 'noncuet-chk', 'noncuet');
            return list;
        })
        .catch(() => {
            renderSubjectCheckboxes(box, [], [], 'noncuet-chk', 'noncuet');
            return [];
        });
}

// ================= LOAD CRITERIA =================
function loadCriteriaForSelected() {
    if (!selectedProgrammeId || !admissionWindowId) return;

    showModalAlert('', '');
    resetSubjectSelections();
    initDefaultTieBreakers();

    const url = programmeLevel === 'UG'
        ? `/admission-criteria/data/ug?admissionWindowId=${admissionWindowId}&programmeId=${selectedProgrammeId}`
        : `/admission-criteria/data/pg?admissionWindowId=${admissionWindowId}&programmeId=${selectedProgrammeId}`;

    const criteriaReq = axios.get(url).catch(err => {
        if (err.response && err.response.status === 404) return { data: null };
        throw err;
    });

    Promise.all([
        criteriaReq
    ])
        .then(([criteriaRes]) => {
            const data = criteriaRes ? criteriaRes.data : null;

            // Load existing subject selections first (so loaders can pre-check)
            if (data) {
                cuetMeritSubjects = safeArray(data.cuetMeritSubjects);
                nonCuetMeritSubjects = safeArray(data.nonCuetMeritSubjects);
            } else {
                cuetMeritSubjects = [];
                nonCuetMeritSubjects = [];
            }

            // Now load subject lists and render with checked state
            return Promise.all([
                loadAllowedCuetSubjects(),
                loadNonCuetSubjects()
            ]).then(() => data);
        })
        .then((data) => {
            if (!data) {
                onNoCriteria();
                syncTieBreakerHiddenField();
                renderTieBreakerTable();
                return;
            }

            // If backend sends priorities, reorder the fixed list
            if (Array.isArray(data.tiebreakerConfig) && data.tiebreakerConfig.length) {
                const byField = Object.fromEntries(
                    tiebreakerConfig.map(c => [c.field, c])
                );
                const ordered = [];

                data.tiebreakerConfig
                    .slice()
                    .sort((a, b) => (a.priority || 0) - (b.priority || 0))
                    .forEach(c => {
                        const base = byField[c.field];
                        if (base) {
                            ordered.push({
                                ...base,
                                direction: c.direction || base.direction,
                                priority: ordered.length + 1
                            });
                            delete byField[c.field];
                        }
                    });

                Object.values(byField).forEach(c => {
                    ordered.push({
                        ...c,
                        priority: ordered.length + 1
                    });
                });

                tiebreakerConfig = ordered;
            }

            syncTieBreakerHiddenField();
            renderTieBreakerTable();

            const statusText = document.getElementById('criteria-status-text');
            if (statusText) {
                statusText.textContent = 'Existing criteria loaded. Drag to change tie-breaker order, then Save.';
            }
        })
        .catch(err => {
            onNoCriteria();
            syncTieBreakerHiddenField();
            renderTieBreakerTable();

            const statusText = document.getElementById('criteria-status-text');
            if (statusText) statusText.textContent = 'Failed to load criteria.';
            showModalAlert('Failed to load criteria.', 'danger');
            console.error(err);
        });
}

function onNoCriteria() {
    const statusText = document.getElementById('criteria-status-text');
    if (statusText) {
        statusText.textContent = 'No criteria configured yet. Select subjects (optional), adjust tie-breaker order and click Save.';
    }
}

// ================= TIE-BREAKER TABLE (NO ADD/REMOVE) =================
function renderTieBreakerTable() {
    const tbody = document.querySelector('#tiebreaker-table tbody');
    if (!tbody) return;

    tbody.replaceChildren();

    if (!tiebreakerConfig.length) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 4;
        td.className = 'text-center text-muted';
        td.textContent = 'No fields available.';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    tiebreakerConfig.forEach((item, index) => {
        const tr = document.createElement('tr');
        tr.setAttribute('draggable', 'true');
        tr.dataset.index = index.toString();

        // Handle column
        const tdHandle = document.createElement('td');
        tdHandle.className = 'text-center text-muted';

        const icon = document.createElement('i');
        icon.className = 'bi bi-grip-vertical';
        tdHandle.appendChild(icon);

        tr.appendChild(tdHandle);

        // Index column
        const tdIndex = document.createElement('td');
        tdIndex.className = 'text-center';
        tdIndex.textContent = index + 1;
        tr.appendChild(tdIndex);

        // Field column
        const tdField = document.createElement('td');
        tdField.textContent = item.label || item.field;

        if (item.field === 'COMMUNITY_CATEGORY') {
            const sub = document.createElement('div');
            sub.className = 'small text-muted';
            sub.textContent = 'Sub-priority: ST (1) > SC (2) > OBC (3) > EWS (4) > General (5)';
            tdField.appendChild(sub);
        }

        tr.appendChild(tdField);

        // Order column
        const tdOrder = document.createElement('td');
        tdOrder.className = 'text-center';
        tdOrder.textContent = item.priority != null ? item.priority : (index + 1);
        tr.appendChild(tdOrder);

        tbody.appendChild(tr);
    });

    enableTieBreakerDragAndDrop();
}

function syncTieBreakerHiddenField() {
    const hidden = document.getElementById('tiebreaker-config-json');
    if (!hidden) return;
    hidden.value = JSON.stringify(
        tiebreakerConfig.map(c => ({
            field: c.field,
            direction: c.direction,
            priority: c.priority
        }))
    );
}

// ---- Drag & drop for tie-breaker rows ----
function enableTieBreakerDragAndDrop() {
    const tbody = document.querySelector('#tiebreaker-table tbody');
    if (!tbody) return;

    let draggedRow = null;

    tbody.onmousedown = null;
    tbody.onmouseup = null;

    tbody.addEventListener('dragstart', (e) => {
        const tr = e.target.closest('tr');
        if (!tr || !tr.hasAttribute('draggable')) return;
        draggedRow = tr;
        tr.classList.add('dragging');
    });

    tbody.addEventListener('dragend', (e) => {
        const tr = e.target.closest('tr');
        if (tr) tr.classList.remove('dragging');
        draggedRow = null;
        syncTieBreakerFromDom();
    });

    tbody.addEventListener('dragover', (e) => {
        e.preventDefault();
        if (!draggedRow) return;

        const afterElement = getDragAfterRow(tbody, e.clientY);
        if (afterElement == null) {
            tbody.appendChild(draggedRow);
        } else {
            tbody.insertBefore(draggedRow, afterElement);
        }
    });
}

function getDragAfterRow(container, y) {
    const rows = [...container.querySelectorAll('tr[draggable="true"]:not(.dragging)')];
    return rows.reduce(
        (closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = y - box.top - box.height / 2;
            if (offset < 0 && offset > closest.offset) {
                return { offset, element: child };
            } else {
                return closest;
            }
        },
        { offset: Number.NEGATIVE_INFINITY, element: null }
    ).element;
}

function syncTieBreakerFromDom() {
    const tbody = document.querySelector('#tiebreaker-table tbody');
    if (!tbody) return;

    const newOrder = [];
    tbody.querySelectorAll('tr[draggable="true"]').forEach((tr) => {
        const oldIndex = parseInt(tr.dataset.index, 10);
        if (!isNaN(oldIndex) && tiebreakerConfig[oldIndex]) {
            newOrder.push(tiebreakerConfig[oldIndex]);
        }
    });

    tiebreakerConfig = newOrder.map((c, i) => ({
        ...c,
        priority: i + 1
    }));

    syncTieBreakerHiddenField();
    renderTieBreakerTable();
}

// ================= SAVE CRITERIA =================
function onSaveCriteria() {
    if (!selectedProgrammeId || !admissionWindowId) {
        showModalAlert('Missing admission window or programme.', 'danger');
        return;
    }

    syncTieBreakerHiddenField();

    let tiebreakers = [];
    try {
        const raw = document.getElementById('tiebreaker-config-json').value || '[]';
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed)) tiebreakers = parsed;
    } catch (e) {
        console.warn('Invalid tiebreaker-config-json', e);
    }

    // subject selections from checkbox UI
    cuetMeritSubjects = readCheckedSubjects(document.getElementById('cuet-subjects-box'), 'cuet-chk');
    nonCuetMeritSubjects = readCheckedSubjects(document.getElementById('noncuet-subjects-box'), 'noncuet-chk');

    const payload = {
        admissionWindowId: admissionWindowId,
        programmeLevel: programmeLevel,
        streamId: streamId,
        programmeId: selectedProgrammeId,
        cuetMeritSubjects: cuetMeritSubjects,
        nonCuetMeritSubjects: nonCuetMeritSubjects,
        tiebreakerConfig: tiebreakers
    };

    const saveBtn = document.getElementById('btn-save-criteria');
    saveBtn.setAttribute('disabled', 'disabled');

    axios.post('/admission-criteria/data/save', payload)
        .then(res => {
            const body = res.data || {};
            showModalAlert(body.message || 'Criteria saved successfully.', 'success');

            const statusText = document.getElementById('criteria-status-text');
            if (statusText) statusText.textContent = 'Criteria saved successfully.';

            setTimeout(() => window.location.reload(), 700);
        })
        .catch(err => {
            const body = err.response && err.response.data ? err.response.data : {};
            const msg = body.message || 'Error saving criteria.';
            showModalAlert(msg, 'danger');
        })
        .finally(() => {
            saveBtn.removeAttribute('disabled');
        });
}

// ================= START ADMISSION / RUN ALLOCATION =================
function updateStartAdmissionButton() {
    const table = document.getElementById('programmeCriteriaTable');
    const startBtn = document.getElementById('btn-start-admission');
    if (!table || !startBtn) return;

    const rows = table.querySelectorAll('tbody tr[data-programme-id]');
    if (rows.length === 0) {
        startBtn.disabled = true;
        return;
    }

    let allHaveMerit = true;

    rows.forEach(row => {
        const badge = row.querySelector('.merit-status-cell .badge');
        if (!badge) {
            allHaveMerit = false;
            return;
        }
        const text = badge.textContent.trim();
        if (text !== 'Merit list exists') {
            allHaveMerit = false;
        }
    });

    startBtn.disabled = !allHaveMerit;
}

// ================= MERIT LIST STATUS PER PROGRAMME =================
function initMeritListStatus() {
    const table = document.getElementById('programmeCriteriaTable');
    if (!table || !admissionWindowId || !programmeLevel) return;

    const rows = table.querySelectorAll('tbody tr[data-programme-id]');
    rows.forEach(row => {
        const programmeId = parseInt(row.dataset.programmeId, 10);
        if (!programmeId) return;

        const cell = row.querySelector('.merit-status-cell');
        if (!cell) return;

        const badge = cell.querySelector('.badge');
        if (badge) {
            badge.className = 'badge bg-secondary';
            badge.textContent = 'Checking...';
        }

        const checkUrl = programmeLevel === 'UG'
            ? `/merit-list/data/check/ug?admissionWindowId=${admissionWindowId}&programmeId=${programmeId}`
            : `/merit-list/data/check/pg?admissionWindowId=${admissionWindowId}&programmeId=${programmeId}`;

        axios.get(checkUrl)
            .then(res => {
                const data = res.data || {};
                const hasMeritList = !!data.hasMeritList;
                const canGenerate = !!data.canGenerate;

                if (badge) {
                    if (hasMeritList) {
                        badge.className = 'badge bg-success';
                        badge.replaceChildren();						
						const icon = document.createElement("i");
						icon.className = "bi bi-trophy me-1";						
						badge.appendChild(icon);
						badge.appendChild(document.createTextNode("Merit list exists"));
                    } else if (canGenerate) {
                        badge.className = 'badge bg-warning text-dark';
                        badge.replaceChildren();					
						const icon1 = document.createElement("i");
						icon1.className = "bi bi-exclamation-circle me-1";
						
						badge.appendChild(icon1);
						badge.appendChild(document.createTextNode("Not generated"));
                    } else {
                        badge.className = 'badge bg-secondary';
                        badge.replaceChildren();
						const icon2 = document.createElement("i");
						icon2.className = "bi bi-dash-circle me-1";
						
						badge.appendChild(icon2);
						badge.appendChild(document.createTextNode("No eligible applicants"));
                    }
                }
                updateStartAdmissionButton();
            })
            .catch(() => {
                if (badge) {
                    badge.className = 'badge bg-danger';
                    badge.replaceChildren();
					
					const icon3 = document.createElement("i");
					icon3.className = "bi bi-exclamation-triangle me-1";
					
					badge.appendChild(icon3);
					badge.appendChild(document.createTextNode("Error"));
                }
            });
    });
}

function onStartAdmissionProcess() {
    if (!admissionWindowId) {
        showPageAlert('Missing admission window id.', 'danger');
        return;
    }

    const startBtn = document.getElementById('btn-start-admission');
    if (startBtn) {
        startBtn.disabled = true;
        startBtn.replaceChildren();
		const spinner = document.createElement("span");
		spinner.className = "spinner-border spinner-border-sm me-1";
		
		startBtn.appendChild(spinner);
		startBtn.appendChild(document.createTextNode("Processing..."));
    }

    axios.post(`/seat-allotment-data/window/${admissionWindowId}/run`)
        .then(res => {
            const data = res.data || {};
            renderAllocationSummary(data);
            showPageAlert('Seat allocation summary generated.', 'success');

            if (startBtn) {
                startBtn.disabled = true;
                startBtn.textContent = 'Admission Process Completed';
                startBtn.classList.remove('btn-primary');
                startBtn.classList.add('btn-secondary');
            }

            transformMeritButtonsToAllotment();
        })
        .catch(err => {
            const msg = (err.response && err.response.data && err.response.data.message)
                ? err.response.data.message
                : 'Error running seat allocation.';
            showPageAlert(msg, 'danger');

            if (startBtn) {
                startBtn.disabled = false;
                startBtn.textContent = 'Start Admission Process';
                startBtn.classList.remove('btn-secondary');
                startBtn.classList.add('btn-primary');
            }
        });
}

function renderAllocationSummary(summary) {
    if (!summary) return;

    const summaryBox = document.getElementById('allocation-summary');
    if (summaryBox) {
      summaryBox.replaceChildren();

		const card = document.createElement("div");
		card.className = "card mt-3";
		
		// Header
		const header = document.createElement("div");
		header.className = "card-header";
		header.textContent = `Seat Allocation Summary (Window #${summary.admissionId || admissionWindowId})`;
		card.appendChild(header);
		
		// Body
		const body = document.createElement("div");
		body.className = "card-body";
		
		const row = document.createElement("div");
		row.className = "row text-center";
		
		// helper to build each column
		function createCol(value, label, extraClass = "") {
		    const col = document.createElement("div");
		    col.className = "col-md-3";
		
		    const valDiv = document.createElement("div");
		    valDiv.className = `fw-bold h5 mb-0 ${extraClass}`;
		    valDiv.textContent = value;
		
		    const labelDiv = document.createElement("div");
		    labelDiv.className = "text-muted small";
		    labelDiv.textContent = label;
		
		    col.appendChild(valDiv);
		    col.appendChild(labelDiv);
		
		    return col;
		}
		
		// Append columns (same logic)
		row.appendChild(createCol(summary.totalProgrammes || 0, "Programmes"));
		row.appendChild(createCol(summary.totalSeats || 0, "Total Seats"));
		row.appendChild(createCol(summary.totalAllotted || 0, "Allotted", "text-success"));
		row.appendChild(createCol(summary.totalUnfilled || 0, "Unfilled", "text-warning"));
		
		body.appendChild(row);
		card.appendChild(body);
		
		summaryBox.appendChild(card);
    }

    const tableBody = document.getElementById('allocation-programme-tbody');
if (!tableBody || !Array.isArray(summary.programmeSummaries)) return;

tableBody.replaceChildren();

summary.programmeSummaries.forEach((p, idx) => {
    const tr = document.createElement('tr');

    const td1 = document.createElement('td');
    td1.className = "text-center";
    td1.textContent = idx + 1;

    const td2 = document.createElement('td');
    td2.textContent = p.programmeName || '';

    const td3 = document.createElement('td');
    td3.textContent = p.instituteName || '';

    const td4 = document.createElement('td');
    td4.className = "text-end";
    td4.textContent = p.totalSeats || 0;

    const td5 = document.createElement('td');
    td5.className = "text-end";
    td5.textContent = p.reservedSeats || 0;

    const td6 = document.createElement('td');
    td6.className = "text-end";
    td6.textContent = p.openSeats || 0;

    const td7 = document.createElement('td');
    td7.className = "text-end text-success";
    td7.textContent = p.allottedCount || 0;

    const td8 = document.createElement('td');
    td8.className = "text-end text-warning";
    td8.textContent = p.unfilledSeats || 0;

    tr.append(td1, td2, td3, td4, td5, td6, td7, td8);
    tableBody.appendChild(tr);
});
}

// ================= MERIT → VIEW ALLOTMENT BTN TRANSFORM =============
function transformMeritButtonsToAllotment() {
    const awId = admissionWindowId;
    if (!awId) return;

    const rows = document.querySelectorAll('#programmeCriteriaTable tbody tr[data-programme-id]');
    rows.forEach(row => {
        const programmeId = parseInt(row.dataset.programmeId, 10);
        if (!programmeId) return;

        const actionCell = row.querySelector('td:last-child');
        if (!actionCell) return;

        actionCell.replaceChildren();
		
		const link = document.createElement("a");
		link.className = "btn btn-sm btn-outline-success";
		link.href = `/seat-allotment/page/window/${awId}/programme/${programmeId}`;
		link.textContent = "View Allotment";
		
		actionCell.appendChild(link);
    });
}
