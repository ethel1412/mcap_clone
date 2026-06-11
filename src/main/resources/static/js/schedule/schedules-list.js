/**
 * SECURITY UTILITY: Escapes HTML characters to prevent DOM-based XSS attacks.
 */
function escapeHTML(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// --- CSRF Token & Axios Setup ---
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

if (csrfHeader && csrfToken) {
    axios.defaults.headers.common[csrfHeader] = csrfToken;
}
axios.defaults.withCredentials = true;

// --- Global Variables ---
let allWindowsData = [];
let ALL_STEPS = []; // Master Roadmap Data

// --- DOM Elements ---
const loadingState = document.getElementById('loading-state');
const mainContent = document.getElementById('main-content');
const noDataState = document.getElementById('no-data-state');
const windowsAccordion = document.getElementById('windowsAccordion');
const alertBox = document.getElementById('alert-box');

// Modals
let editModal;

// Initialize
document.addEventListener('DOMContentLoaded', async () => {
    editModal = new bootstrap.Modal(document.getElementById('editScheduleModal'));

    initFlatpickr();
    setupEventListeners();

    await loadStepTemplates();
    await loadAllWindows();
});

// Initialize Flatpickr Date Selectors
function initFlatpickr() {
    const config = {
        enableTime: true,
        dateFormat: "Y-m-d\\TH:i",
        altInput: true,
        altFormat: "F j, Y - h:i K",
        time_24hr: false,
        static: true
    };

    flatpickr(document.getElementById("editStartDate"), {
        ...config,
        onChange: function(selectedDates) {
            const endPicker = document.getElementById("editEndDate")._flatpickr;
            if (endPicker && selectedDates[0]) {
                endPicker.set('minDate', selectedDates[0]);
            }
        }
    });

    flatpickr(document.getElementById("editEndDate"), config);
}

async function loadStepTemplates() {
    try {
        const response = await axios.get('/step-template-data/templates');
        ALL_STEPS = response.data.map(template => ({
            order: template.stepOrder,
            name: template.stepName,
            category: template.category,
            route: template.admissionRoute,
            phase: template.phaseNumber
        }));
    } catch (error) {
        console.error('Error loading Master Roadmap templates:', error);
        showAlert('Failed to load system timeline templates.', 'danger');
        ALL_STEPS = [];
    }
}

async function loadAllWindows() {
    try {
        const response = await axios.get('/schedule-data/active-windows');
        allWindowsData = response.data;

        loadingState.classList.add('d-none');
        mainContent.classList.remove('d-none');

        if (allWindowsData.length === 0) {
            noDataState.classList.remove('d-none');
            windowsAccordion.classList.add('d-none');
        } else {
            noDataState.classList.add('d-none');
            windowsAccordion.classList.remove('d-none');
            renderWindows();
        }
    } catch (error) {
        console.error('Error loading windows:', error);
        loadingState.classList.add('d-none');
        showAlert('Failed to load admission windows.', 'danger');
    }
}

async function renderWindows() {
    windowsAccordion.innerHTML = '';
    const totalMasterSteps = ALL_STEPS.length;

    for (let i = 0; i < allWindowsData.length; i++) {
        const window = allWindowsData[i];
        const schedules = await fetchSchedulesForWindow(window.admissionCode);
        const accordionItem = createWindowAccordion(window, schedules, i, totalMasterSteps);
        windowsAccordion.appendChild(accordionItem);
    }
}

async function fetchSchedulesForWindow(admissionCode) {
    try {
        const response = await axios.get(`/schedule-data/schedules/${admissionCode}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching schedules:', error);
        return [];
    }
}

function createWindowAccordion(window, schedules, index, totalMasterSteps) {
    const div = document.createElement('div');
    div.className = 'accordion-item border-0 mb-3 shadow-sm rounded overflow-hidden';

    const completedCount = schedules.length;
    let badgeColor = completedCount === 0 ? 'bg-secondary' : (completedCount >= totalMasterSteps ? 'bg-success' : 'bg-primary');

    div.innerHTML = `
        <h2 class="accordion-header" id="heading${index}">
            <button class="accordion-button collapsed" type="button"
                    data-bs-toggle="collapse" data-bs-target="#collapse${index}">
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center w-100 me-3">
                    <div>
                        <h5 class="mb-1 fw-bold text-dark">${escapeHTML(window.windowName)}</h5>
                        <div class="window-header-info">
                            <span class="window-info-item text-secondary">
                                <i class="bi bi-calendar-range text-primary"></i>
                                ${formatDateTime(window.startDate)} — ${formatDateTime(window.endDate)}
                            </span>
                        </div>
                    </div>
                    <div class="mt-2 mt-md-0">
                        <span class="badge ${badgeColor} text-white rounded-pill px-3 py-2 shadow-sm fs-09rem">
                            <i class="bi bi-list-check me-1"></i>
                            ${completedCount} / ${totalMasterSteps} Mapped
                        </span>
                    </div>
                </div>
            </button>
        </h2>
        <div id="collapse${index}" class="accordion-collapse collapse"
             data-bs-parent="#windowsAccordion">
            <div class="accordion-body bg-light pt-0">
                ${renderScheduleTimeline(schedules, window)}
            </div>
        </div>
    `;

    return div;
}

function renderScheduleTimeline(schedules, window) {
    let html = '<div class="schedule-timeline">';

    let currentCategory = null;
    let currentPhaseKey = null;
    let hasDrawnAppWindow = false;

    ALL_STEPS.forEach((template) => {

        if (!hasDrawnAppWindow && (template.category === 'CORRECTION' || template.category === 'COUNSELLING')) {
            html += `
                <div class="schedule-card my-2-5rem">
                    <div class="schedule-header-wrapper bg-slate-700">
                        <i class="bi bi-calendar-check"></i>
                    </div>
                    <div class="p-3 rounded shadow-sm bg-dashed-slate">
                        <h6 class="mb-2 fw-bold text-secondary"><i class="bi bi-pin-angle-fill me-2"></i>Student Application Window</h6>
                        <div class="text-dark small fw-semibold">
                            <i class="bi bi-play-circle text-success me-1"></i> Opens: ${formatDateTime(window.startDate)}<br>
                            <i class="bi bi-stop-circle text-danger me-1"></i> Closes: ${formatDateTime(window.endDate)}
                        </div>
                    </div>
                </div>
            `;
            hasDrawnAppWindow = true;
        }

        if (template.category && template.category !== currentCategory) {
            currentCategory = template.category;

            let catName = '';
            if (template.category === 'PRE_ADMISSION') catName = 'Pre-Admission Phase';
            else if (template.category === 'CORRECTION') catName = 'Application Correction Phase';
            else if (template.category === 'COUNSELLING') catName = 'Counselling Phase';

            html += `<div class="roadmap-header"><span>${escapeHTML(catName)}</span></div>`;
        }

        const phaseKey = template.route + '-' + template.phase;
        if (template.phase && phaseKey !== currentPhaseKey) {
            currentPhaseKey = phaseKey;
            const badgeColor = template.route === 'CUET' ? 'bg-success' : 'bg-info text-dark';
            html += `
                <div class="roadmap-header mt-3">
                    <span class="${badgeColor} text-white border-0">${escapeHTML(template.route)} - Phase ${template.phase}</span>
                </div>
            `;
        }

        const completedSchedule = schedules.find(s => s.stepOrder === template.order);
        const isCompleted = !!completedSchedule;

        if (isCompleted) {
            const status = completedSchedule.status || 'upcoming';
            let actionButtons = '';

            if (status === 'expired') {
                actionButtons = `
                    <button class="btn btn-outline-secondary shadow-sm px-3" disabled title="This step has already concluded">
                        <i class="bi bi-lock-fill me-1"></i> Closed
                    </button>
                `;
            } else {
                actionButtons = `
                    <button
					    class="btn btn-outline-primary shadow-sm px-3 adjust-btn"
					    data-schedule-id="${completedSchedule.scheduleId}"
					    data-status="${status}"
					>
					    <i class="bi bi-pencil-fill me-1"></i> Adjust Dates
					</button>
                `;
            }

            let statusBadge = '';
            if(status === 'ongoing') statusBadge = '<span class="badge bg-success mt-1">ONGOING</span>';
            else if(status === 'expired') statusBadge = '<span class="badge bg-secondary mt-1">EXPIRED</span>';
            else statusBadge = '<span class="badge bg-primary mt-1">UPCOMING</span>';

            html += `
                <div class="schedule-card">
                    <div class="schedule-header-wrapper shadow-sm">
                        ${template.order}
                    </div>
                    <div class="schedule-content">
                        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-start mb-3">
                            <div>
                                <div class="schedule-title">${escapeHTML(template.name)}</div>
                                ${statusBadge}
                            </div>
                            <div class="d-flex gap-2 mt-2 mt-md-0">
                                ${actionButtons}
                            </div>
                        </div>

                        <div class="schedule-dates border shadow-sm">
                            <div class="date-item w-50 border-end pe-3">
                                <i class="bi bi-calendar-play text-success fs-5"></i>
                                <div>
                                    <small class="text-uppercase fw-bold text-muted d-block fs-07rem">Starts</small>
                                    <span class="fw-semibold text-dark">${formatDateTime(completedSchedule.startDate)}</span>
                                </div>
                            </div>
                            <div class="date-item w-50 ps-2">
                                <i class="bi bi-calendar-x text-danger fs-5"></i>
                                <div>
                                    <small class="text-uppercase fw-bold text-muted d-block fs-07rem">Ends</small>
                                    <span class="fw-semibold text-dark">${formatDateTime(completedSchedule.endDate)}</span>
                                </div>
                            </div>
                        </div>

                        ${completedSchedule.description ? `
                            <div class="schedule-description mt-3">
                                <i class="bi bi-info-circle-fill text-primary me-2"></i><strong>Note:</strong> ${escapeHTML(completedSchedule.description)}
                            </div>
                        ` : ''}
                    </div>
                </div>
            `;
        } else {
            html += `
                <div class="schedule-card pending">
                    <div class="schedule-header-wrapper">
                        ${template.order}
                    </div>
                    <div class="schedule-content">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <div class="schedule-title mb-0">${escapeHTML(template.name)}</div>
                            <span class="badge bg-secondary rounded-pill">Awaiting Configuration</span>
                        </div>
                        <div class="schedule-dates border-0 bg-transparent p-0 mb-0 mt-3 opacity-50">
                            <div class="date-item w-50 pe-3">
                                <i class="bi bi-calendar text-muted fs-5"></i>
                                <div>
                                    <small class="text-uppercase fw-bold text-muted d-block fs-07rem">Starts</small>
                                    <span class="fw-semibold text-muted">Pending...</span>
                                </div>
                            </div>
                            <div class="date-item w-50 ps-2">
                                <i class="bi bi-calendar text-muted fs-5"></i>
                                <div>
                                    <small class="text-uppercase fw-bold text-muted d-block fs-07rem">Ends</small>
                                    <span class="fw-semibold text-muted">Pending...</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }
    });

    html += '</div>';

    return html;
}

async function openEditModal(scheduleId, status) {
    try {
        const response = await axios.get(`/schedule-data/schedules/detail/${scheduleId}`);
        const schedule = response.data;

        document.getElementById('editScheduleId').value = schedule.scheduleId;
        document.getElementById('editAdmissionCode').value = schedule.admissionCode;
        document.getElementById('editStepOrder').value = schedule.stepOrder;

        document.getElementById('editAdmissionRoute').value = schedule.admissionRoute || '';
        document.getElementById('editPhaseNumber').value = schedule.phaseNumber || '';

        document.getElementById('editStepName').textContent = schedule.stepName;
        document.getElementById('editStepOrderDisplay').textContent = schedule.stepOrder;
        document.getElementById('editDescription').value = schedule.description || '';

        const template = ALL_STEPS.find(t => t.order === schedule.stepOrder);
        const windowData = allWindowsData.find(w => w.admissionCode === schedule.admissionCode);

        const startPicker = document.getElementById("editStartDate")._flatpickr;
        const endPicker = document.getElementById("editEndDate")._flatpickr;

        if (!startPicker || !endPicker) return;

        startPicker.clear();
        endPicker.clear();

        startPicker.set('minDate', null);
        startPicker.set('maxDate', null);
        endPicker.set('minDate', null);
        endPicker.set('maxDate', null);

        if (template && windowData) {
            if (template.category === 'PRE_ADMISSION') {
                startPicker.set('maxDate', windowData.startDate);
                endPicker.set('maxDate', windowData.startDate);
            }
            if (template.category === 'COUNSELLING') {
                startPicker.set('minDate', windowData.endDate);
                endPicker.set('minDate', windowData.endDate);
            }
        }

        const startInputEl = startPicker.altInput || startPicker.input;

        if (status === 'ongoing') {
            startPicker.set('clickOpens', false);
            startInputEl.classList.add('bg-e9ecef', 'cursor-not-allowed');
            startInputEl.title = "Step is active. Start date cannot be changed.";
            endPicker.set('minDate', schedule.startDate);

            showAlert('This step is currently active. You can only extend the End Date.', 'info');
        } else {
            startPicker.set('clickOpens', true);
            startInputEl.classList.remove('bg-e9ecef', 'cursor-not-allowed');
            startInputEl.title = "";
            endPicker.set('minDate', schedule.startDate);
        }

        startPicker.setDate(schedule.startDate, true);
        endPicker.setDate(schedule.endDate, true);

        editModal.show();

    } catch (error) {
        console.error('Error loading schedule detail:', error);
        showAlert('Failed to load schedule details.', 'danger');
    }
}

function setupEventListeners() {
    document.getElementById('editScheduleForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!document.getElementById('editStartDate').value || !document.getElementById('editEndDate').value) {
            showAlert('Please select both Start and End dates.', 'danger');
            return;
        }

        await updateSchedule();
    });

    windowsAccordion.addEventListener('click', function (e) {
        const btn = e.target.closest('.adjust-btn');
        if (!btn) return;

        const scheduleId = btn.getAttribute('data-schedule-id');
        const status = btn.getAttribute('data-status');

        if (!scheduleId) return;

        openEditModal(parseInt(scheduleId), status);
    });
}

async function updateSchedule() {
    const btn = document.getElementById('updateScheduleBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

    const routeVal = document.getElementById('editAdmissionRoute').value;
    const phaseVal = document.getElementById('editPhaseNumber').value;

    const startPicker = document.getElementById("editStartDate")._flatpickr;
    const endPicker = document.getElementById("editEndDate")._flatpickr;

    const startDateObj = startPicker?.selectedDates[0];
    const endDateObj = endPicker?.selectedDates[0];

    if (!startDateObj || !endDateObj) {
        showAlert('Please select valid Start and End dates.', 'danger');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-save me-2"></i>Save Changes';
        return;
    }

    const data = {
        admissionCode: document.getElementById('editAdmissionCode').value,
        stepOrder: parseInt(document.getElementById('editStepOrder').value),
        description: document.getElementById('editDescription').value,
        startDate: startDateObj.toISOString(),
        endDate: endDateObj.toISOString(),
        admissionRoute: routeVal ? routeVal : null,
        phaseNumber: phaseVal ? parseInt(phaseVal) : null
    };

    try {
        const scheduleId = document.getElementById('editScheduleId').value;
        await axios.put(`/schedule-data/schedules/${scheduleId}`, data);
        showAlert('Schedule dates updated successfully!', 'success');
        editModal.hide();
        await loadAllWindows();

    } catch (error) {
        console.error('Error updating schedule:', error);
        const errorMsg = error.response?.data?.error || error.response?.data?.message || 'Failed to update schedule dates.';
        showAlert(errorMsg, 'danger');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-save me-2"></i>Save Changes';
    }
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit', hour12: true
    });
}

function showAlert(message, type) {
    const icon = type === 'success' ? 'check-circle-fill' : (type === 'info' ? 'info-circle-fill' : 'exclamation-triangle-fill');
    alertBox.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-${icon} me-2"></i>${escapeHTML(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;

    setTimeout(() => {
        const alert = alertBox.querySelector('.alert');
        if (alert) {
            alert.classList.remove('show');
            setTimeout(() => alertBox.innerHTML = '', 150);
        }
    }, 5000);
}