// CSRF Token Setup
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// Configure Axios
axios.defaults.headers.common[csrfHeader] = csrfToken;
axios.defaults.withCredentials = true;

// Global Variables
let ALL_STEPS = [];
let windowData = null;

// DOM Elements
const loadingState = document.getElementById('loading-state');
const roadmapContainer = document.getElementById('roadmap-container');
const noSchedules = document.getElementById('no-schedules');
const stepsRoadmap = document.getElementById('steps-roadmap');
const stepsCount = document.getElementById('steps-count');

// Initialize
document.addEventListener('DOMContentLoaded', async () => {
    await loadStepTemplates();
    await loadWindowData();
    await loadSchedules();
});

// Load step templates from database
async function loadStepTemplates() {
    try {
        const response = await axios.get('/step-template-data/templates');
        ALL_STEPS = response.data.map(template => ({
            order: template.stepOrder,
            name: template.stepName,
            round: template.roundNumber
        }));
        console.log('Loaded', ALL_STEPS.length, 'step templates');
    } catch (error) {
        console.error('Error loading step templates:', error);
        ALL_STEPS = [];
    }
}

// Load window data
async function loadWindowData() {
    try {
        const response = await axios.get('/schedule-data/upcoming-windows');
        // CHANGED: Match the frontend object using admissionCode instead of ID
        windowData = response.data.find(w => w.admissionCode === admissionCode);

        if (windowData) {
            document.getElementById('window-name').textContent = windowData.windowName;
            document.getElementById('window-dates').textContent =
                `${formatDate(windowData.startDate)} - ${formatDate(windowData.endDate)}`;
        }
    } catch (error) {
        console.error('Error loading window data:', error);
    }
}

// Load schedules
async function loadSchedules() {
    try {
        // CHANGED: Pass admissionCode instead of admissionId to the backend
        const response = await axios.get(`/schedule-data/schedules/${admissionCode}`);
        const schedules = response.data;

        loadingState.style.display = 'none';

        if (schedules.length === 0) {
            noSchedules.style.display = 'block';
        } else {
            renderRoadmap(schedules);
            roadmapContainer.style.display = 'block';
        }
    } catch (error) {
        console.error('Error loading schedules:', error);
        loadingState.style.display = 'none';
        noSchedules.style.display = 'block';
    }
}

// Render roadmap
function renderRoadmap(schedules) {
    stepsRoadmap.innerHTML = '';
    stepsCount.textContent = `${schedules.length} Step${schedules.length !== 1 ? 's' : ''}`;

    let currentRound = null;

    ALL_STEPS.forEach(step => {
        // Add round separator
        if (step.round && step.round !== currentRound) {
            currentRound = step.round;
            const separator = document.createElement('div');
            separator.className = 'round-separator';
            separator.innerHTML = `
                <div class="round-title">
                    <i class="bi bi-circle-fill me-2"></i> Round ${currentRound} <i class="bi bi-circle-fill ms-2"></i>
                </div>
            `;
            stepsRoadmap.appendChild(separator);
        }

        const schedule = schedules.find(s => s.stepOrder === step.order);

        if (schedule) {
            const stepCard = document.createElement('div');
            stepCard.className = 'step-item';
            stepCard.innerHTML = `
                <div class="step-header">
                    <span class="step-number">${step.order}</span>
                    <i class="bi bi-check-circle-fill step-icon"></i>
                    <div class="step-title">${step.name}</div>
                    <span class="step-status">Completed</span>
                </div>

                ${schedule.description ? `
                    <div class="step-description">
                        <i class="bi bi-info-circle me-2"></i>${schedule.description}
                    </div>
                ` : ''}

                <div class="step-dates">
                    <div>
                        <i class="bi bi-calendar-check"></i>
                        <strong>Start Date:</strong> ${formatDate(schedule.startDate)}
                    </div>
                    <div>
                        <i class="bi bi-calendar-x"></i>
                        <strong>End Date:</strong> ${formatDate(schedule.endDate)}
                    </div>
                </div>
            `;

            stepsRoadmap.appendChild(stepCard);
        }
    });
}

// Utility function
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
}