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
let selectedAdmissionCode = null;
let selectedWindowData = null;
let allWindowsData = [];
let ALL_STEPS = [];
let startPicker, endPicker;

// --- DOM Elements ---
const windowsLoading = document.getElementById('windows-loading');
const windowsGrid = document.getElementById('windows-grid');
const noWindows = document.getElementById('no-windows');
const windowSelectionSection = document.getElementById('window-selection-section');
const scheduleCreationSection = document.getElementById('schedule-creation-section');
const alertBox = document.getElementById('alert-box');
const form = document.getElementById('createScheduleForm');
const scheduleFormCard = document.getElementById('schedule-form-card');

document.addEventListener('DOMContentLoaded', async () => {

	windowSelectionSection.classList.remove('d-none');
    scheduleCreationSection.classList.add('d-none');

    initFlatpickr();
    setupEventListeners();

    await loadStepTemplates();
    loadAdmissionWindows();
});

function initFlatpickr() {
    const config = {
        enableTime: true,
        dateFormat: "Y-m-d\\TH:i",
        altInput: true,
        altFormat: "F j, Y - h:i K",
        time_24hr: false,
        static: true
    };

    startPicker = flatpickr("#startDate", config);
    endPicker = flatpickr("#endDate", config);

    document.getElementById('startDate').addEventListener('change', (e) => {
        if (e.target.value) {
            endPicker.set('minDate', e.target.value);
        }
    });
}

async function loadStepTemplates() {
    try {
        const response = await axios.get('/step-template-data/templates');
        ALL_STEPS = response.data.map(template => ({
            order: template.stepOrder,
            name: template.stepName,
            category: template.category,
            route: template.admissionRoute,
            phase: template.phaseNumber,
            role: template.defaultActorRole
        }));
    } catch (error) {
        console.error('Error loading step templates:', error);
        showToast('Failed to load Master Roadmap templates', 'error');
        ALL_STEPS = [];
    }
}

async function loadAdmissionWindows() {
	windowSelectionSection.classList.remove('d-none');
    scheduleCreationSection.classList.add('d-none');

    try {
        const response = await axios.get('/schedule-data/upcoming-windows');
        allWindowsData = response.data;

        windowsLoading.classList.add('d-none');

        if (allWindowsData.length === 0) {
            noWindows.classList.remove('d-none');
            windowsGrid.classList.add('d-none');
        } else {
            noWindows.classList.add('d-none');
            renderWindowCards();
            windowsGrid.classList.remove('d-none');
        }
    } catch (error) {
        console.error('Error loading admission windows:', error);
        windowsLoading.classList.add('d-none');
        showToast('Failed to load admission windows.', 'error');
    }
}

async function renderWindowCards() {
    windowsGrid.innerHTML = '';

    for (const windowData of allWindowsData) {
        const progress = await getWindowProgress(windowData.admissionCode);

        const totalMasterSteps = ALL_STEPS.length;
        const isComplete = progress.completedSteps >= totalMasterSteps && totalMasterSteps > 0;

        const card = document.createElement('div');
        card.className = 'col-md-6 col-lg-4';

        card.innerHTML = `
            <div class="window-card shadow-sm" data-admission-code="${windowData.admissionCode}">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <span class="window-badge badge-upcoming">Upcoming Window</span>
                    ${isComplete
                        ? `<span class="badge bg-success"><i class="bi bi-check-circle-fill me-1"></i>Completed</span>`
                        : progress.completedSteps > 0
                            ? `<span class="badge bg-info text-dark">${progress.completedSteps} / ${totalMasterSteps} Steps</span>`
                            : `<span class="badge bg-secondary">Not Started</span>`
                    }
                </div>

                <h5 class="window-card-title text-primary">${escapeHTML(windowData.windowName)}</h5>

                <div class="window-dates border">
                    <div class="date-item">
                        <i class="bi bi-calendar-check text-success"></i>
                        <span><strong>Opens:</strong> <br>${formatDateTime(windowData.startDate)}</span>
                    </div>
                    <div class="date-item mt-2">
                        <i class="bi bi-calendar-x text-danger"></i>
                        <span><strong>Closes:</strong> <br>${formatDateTime(windowData.endDate)}</span>
                    </div>
                </div>

                ${isComplete ? `
                    <a href="/schedule-management/view?admissionCode=${windowData.admissionCode}" class="btn btn-outline-success select-window-btn rounded-pill">
                        <i class="bi bi-eye me-2"></i>View Live Schedule
                    </a>
                ` : `
                    <button class="btn btn-primary select-window-btn rounded-pill shadow-sm"
					        data-admission-code="${windowData.admissionCode}">
					    <i class="bi bi-gear-fill me-2"></i>Configure Timeline
					</button>
                `}
            </div>
        `;

        const button = card.querySelector('button.select-window-btn');
        if (button) {
            button.addEventListener('click', () => {
                const code = button.getAttribute('data-admission-code');
                selectWindow(code);
            });
        }
        windowsGrid.appendChild(card);
    }
}

async function getWindowProgress(admissionCode) {
    try {
        const response = await axios.get(`/schedule-data/next-step/${admissionCode}`);
        return response.data;
    } catch (error) {
        return { completedSteps: 0, totalSteps: ALL_STEPS.length, allStepsCompleted: false };
    }
}

async function selectWindow(admissionCode) {
    if (ALL_STEPS.length === 0) {
        showToast('The Master Roadmap is empty. Please define templates first.', 'error');
        return;
    }

    selectedAdmissionCode = admissionCode;
    selectedWindowData = allWindowsData.find(w => w.admissionCode === admissionCode);

    const progress = await getWindowProgress(admissionCode);

   	if (progress.completedSteps >= ALL_STEPS.length && ALL_STEPS.length > 0) {
	    showToast('All schedule steps have been completed for this window!', 'success');
	    const safeAdmissionCode = encodeURIComponent(String(admissionCode));
	    setTimeout(() => window.location.href = `/schedule-management/view?admissionCode=${safeAdmissionCode}`, 1500);
	    return;
	}
    document.getElementById('selected-window-name').textContent = selectedWindowData.windowName;
    document.getElementById('selected-window-dates').textContent =
        `${formatDateTime(selectedWindowData.startDate)}  —  ${formatDateTime(selectedWindowData.endDate)}`;

    windowSelectionSection.classList.add('d-none');
    scheduleCreationSection.classList.remove('d-none');

    loadNextStep();
}

async function loadNextStep() {
    try {
        const schedulesResponse = await axios.get(`/schedule-data/schedules/${selectedAdmissionCode}`);
        const completedSchedules = schedulesResponse.data;

        const totalMasterSteps = ALL_STEPS.length;
        const completedCount = completedSchedules.length;

        if (completedCount >= totalMasterSteps) {
            document.getElementById('completion-message').classList.remove('d-none');
            document.getElementById('schedule-form-card').classList.add('d-none');
            updateProgressBar(totalMasterSteps, totalMasterSteps);
            renderStepsRoadmap(completedSchedules, totalMasterSteps + 1);
            return;
        }

        const activeTemplate = ALL_STEPS[completedCount];

        document.getElementById('admissionCode').value = selectedAdmissionCode;
        document.getElementById('stepOrder').value = activeTemplate.order;
        document.getElementById('stepName').value = activeTemplate.name;
        document.getElementById('stepNameDisplay').textContent = activeTemplate.name;

        updateProgressBar(completedCount, totalMasterSteps);
        renderStepsRoadmap(completedSchedules, activeTemplate.order);

        applyDateConstraints(activeTemplate);

    } catch (error) {
        console.error('Error loading next step:', error);
        showToast('Failed to load next step data.', 'error');
    }
}

function applyDateConstraints(activeTemplate) {
    const constraintInfo = document.getElementById('date-constraint-info');
    const constraintText = document.getElementById('date-constraint-text');

    let minStart = null;
    let maxEnd = null;
    let ruleText = `<ul class="mb-0 mt-1 ps-3">
        <li><strong>Overlap Allowed:</strong> Steps can overlap with other steps in this phase.</li>`;

    if (activeTemplate.category === 'PRE_ADMISSION') {
        maxEnd = selectedWindowData.startDate;
        ruleText += `<li><strong>Pre-Admission Limit:</strong> Must conclude <strong>before</strong> the Student Window opens on <span class="badge bg-white text-dark border border-secondary">${formatDateTime(selectedWindowData.startDate)}</span>.</li>`;

    } else if (activeTemplate.category === 'COUNSELLING' || activeTemplate.category === 'CORRECTION') {
        minStart = selectedWindowData.endDate;
        const phaseLabel = activeTemplate.category === 'CORRECTION' ? 'Correction' : 'Counselling';
        ruleText += `<li><strong>${phaseLabel} Limit:</strong> Cannot begin until <strong>after</strong> the Student Window closes on <span class="badge bg-white text-dark border border-secondary">${formatDateTime(selectedWindowData.endDate)}</span>.</li>`;
    }

    ruleText += '</ul>';

    startPicker.clear();
    endPicker.clear();

    if (minStart) {
        startPicker.set('minDate', minStart);
        endPicker.set('minDate', minStart);
    } else {
        startPicker.set('minDate', null);
        endPicker.set('minDate', null);
    }

    if (maxEnd) {
        startPicker.set('maxDate', maxEnd);
        endPicker.set('maxDate', maxEnd);
    } else {
        startPicker.set('maxDate', null);
        endPicker.set('maxDate', null);
    }

    constraintText.textContent = '';
	const strong = document.createElement('strong');
	strong.textContent = 'Timeline Requirements:';
	constraintText.appendChild(strong);

	const wrapper = document.createElement('div');
	wrapper.innerHTML = ruleText;
	constraintText.appendChild(wrapper);
    constraintInfo.classList.remove('d-none');
}

function renderStepsRoadmap(completedSchedules, currentStepOrder) {
    const roadmap = document.getElementById('steps-roadmap');
    roadmap.innerHTML = '';

    let currentCategory = null;
    let currentPhaseKey = null;
    let hasDrawnAppWindow = false;

    ALL_STEPS.forEach(step => {
        if (!hasDrawnAppWindow && (step.category === 'CORRECTION' || step.category === 'COUNSELLING')) {
            roadmap.insertAdjacentHTML('beforeend', `
                <div class="step-item locked anchor-item anchor-item-custom">
                    <div class="step-header shadow-sm bg-slate-700">
                        <i class="bi bi-calendar-check step-icon d-block"></i>
                    </div>
                    <div class="p-3 rounded shadow-sm bg-dashed-slate">
                        <h6 class="mb-2 fw-bold text-secondary"><i class="bi bi-pin-angle-fill me-2"></i>Student Application Window</h6>
                        <div class="text-dark small fw-semibold">
                            <i class="bi bi-play-circle text-success me-1"></i> Opens: ${formatDateTime(selectedWindowData.startDate)}<br>
                            <i class="bi bi-stop-circle text-danger me-1"></i> Closes: ${formatDateTime(selectedWindowData.endDate)}
                        </div>
                    </div>
                </div>
            `);
            hasDrawnAppWindow = true;
        }

        if (step.category !== currentCategory) {
            currentCategory = step.category;

            let catName = '';
            if (step.category === 'PRE_ADMISSION') catName = 'Pre-Admission Phase';
            else if (step.category === 'CORRECTION') catName = 'Application Correction Phase';
            else if (step.category === 'COUNSELLING') catName = 'Counselling Phase';

            roadmap.insertAdjacentHTML('beforeend', `<div class="roadmap-header"><span>${escapeHTML(catName)}</span></div>`);
        }

        const phaseKey = step.route + '-' + step.phase;
        if (step.phase && phaseKey !== currentPhaseKey) {
            currentPhaseKey = phaseKey;
            const badgeColor = step.route === 'CUET' ? 'bg-success' : 'bg-info text-dark';
            roadmap.insertAdjacentHTML('beforeend', `
                <div class="roadmap-header mt-3">
                    <span class="${badgeColor} text-white border-0">${escapeHTML(step.route)} - Phase ${step.phase}</span>
                </div>
            `);
        }

        const isCompleted = completedSchedules.some(s => s.stepOrder === step.order);
        const isActive = step.order === currentStepOrder;

        let statusClass = isCompleted ? 'completed' : (isActive ? 'active' : 'locked');
        let statusText = isCompleted ? 'Completed' : (isActive ? 'Awaiting Dates' : 'Locked');
        let statusIcon = isCompleted ? 'check-lg' : (isActive ? 'pencil-fill' : 'lock-fill');

        const completedSchedule = completedSchedules.find(s => s.stepOrder === step.order);

        const stepCard = document.createElement('div');
        stepCard.className = `step-item ${statusClass}`;
        stepCard.innerHTML = `
            <div class="step-header shadow-sm">
                <span class="step-number">${step.order}</span>
                <i class="bi bi-${statusIcon} step-icon"></i>
            </div>
            <div class="step-title">${escapeHTML(step.name)}</div>
            <span class="step-status ${statusClass}">${statusText}</span>

            ${completedSchedule ? `
                <div class="d-block mt-1">
                    <div class="step-dates shadow-sm">
                        <div><i class="bi bi-calendar-check text-success"></i> ${formatDateTime(completedSchedule.startDate)}</div>
                        <div><i class="bi bi-calendar-x text-danger"></i> ${formatDateTime(completedSchedule.endDate)}</div>
                    </div>
                </div>
            ` : ''}

            ${isActive ? `
                <div class="mt-2">
                    <button type="button" class="step-create-btn shadow-sm">
                        <i class="bi bi-arrow-down-circle me-1"></i> Set Dates Below
                    </button>
                </div>
            ` : ''}
        `;
       const stepBtn = stepCard.querySelector('.step-create-btn');
		if (stepBtn) {
		    stepBtn.addEventListener('click', scrollToForm);
		}
        roadmap.appendChild(stepCard);
    });
}

function scrollToForm() {
    scheduleFormCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
    scheduleFormCard.classList.add('form-highlight');
    setTimeout(() => scheduleFormCard.classList.remove('form-highlight'), 1500);
}

function updateProgressBar(completed, total) {
    const percentage = total === 0 ? 0 : (completed / total) * 100;
    document.getElementById('progress-bar').style.width = `${percentage}%`;
    document.getElementById('progress-text').textContent = `${completed} of ${total} required steps mapped`;
}

function setupEventListeners() {
    document.getElementById('change-window-btn').addEventListener('click', () => {
        scheduleCreationSection.classList.add('d-none');
        windowSelectionSection.classList.remove('d-none');
        form.reset();
        startPicker.clear();
        endPicker.clear();
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!document.getElementById('startDate').value || !document.getElementById('endDate').value) {
            showToast('Please select both Start and End dates.', 'error');
            return;
        }

        await createScheduleStep();
    });
}

async function createScheduleStep() {
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

    const stepOrderVal = parseInt(document.getElementById('stepOrder').value);
    const activeTemplate = ALL_STEPS.find(s => s.order === stepOrderVal);

    const formData = {
        admissionCode: document.getElementById('admissionCode').value,
        stepOrder: stepOrderVal,
        stepName: document.getElementById('stepName').value,
        description: document.getElementById('description').value,
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        admissionRoute: activeTemplate ? activeTemplate.route : null,
        phaseNumber: activeTemplate ? activeTemplate.phase : null
    };

    try {
        await axios.post('/schedule-data/schedules', formData);

        form.reset();
        startPicker.clear();
        endPicker.clear();
        form.classList.remove('was-validated');

        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => {
            showToast('Schedule step configured successfully!', 'success');
            loadNextStep();
        }, 500);

    } catch (error) {
        console.error('Error creating schedule:', error);
        const errorMsg = error.response?.data?.error || 'Failed to map dates to schedule step.';
        showToast(errorMsg, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-save me-2"></i>Save Step & Continue';
    }
}

function showToast(message, type) {
    const toastId = `toast-${Date.now()}`;
    let container = document.querySelector('.toast-container-top');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container-top position-fixed top-0 end-0 p-3 z-1080';
        document.body.appendChild(container);
    }

    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0 shadow" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body fw-bold">
                    <i class="bi bi-${type === 'success' ? 'check-circle-fill' : 'exclamation-triangle-fill'} me-2"></i>
                    ${escapeHTML(message)}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);
    const toastElement = document.getElementById(toastId);
    const bsToast = new bootstrap.Toast(toastElement, { autohide: true, delay: 4000 });

    bsToast.show();
    toastElement.addEventListener('hidden.bs.toast', () => toastElement.remove());
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit', hour12: true
    });
}