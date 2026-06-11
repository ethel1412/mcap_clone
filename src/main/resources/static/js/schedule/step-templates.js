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

// --- CSRF & Axios Setup ---
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

if (csrfHeader && csrfToken) {
    axios.defaults.headers.common[csrfHeader] = csrfToken;
}
axios.defaults.withCredentials = true;

// --- Global State ---
let allTemplates = [];
let deleteTemplateId = null;
let isEditMode = false;

// --- Modals ---
let customStepModal;
let deleteModal;

// --- DOM Elements ---
const loadingState = document.getElementById('loading-state');
const mainTimelineWrapper = document.getElementById('main-timeline-wrapper');
const preAdmissionZone = document.getElementById('pre-admission-zone');
const correctionZone = document.getElementById('correction-zone'); // ADDED: Correction Zone
const counsellingZone = document.getElementById('counselling-zone');
const preAdmissionGenerator = document.getElementById('pre-admission-generator');
const alertBox = document.getElementById('alert-box');

// Form Elements
const templateForm = document.getElementById('templateForm');
const templateIdInput = document.getElementById('templateId');
const stepOrderInput = document.getElementById('stepOrder');
const stepNameInput = document.getElementById('stepName');
const stepDescInput = document.getElementById('stepDesc');
const actorRoleSelect = document.getElementById('actorRole');
const admissionRouteSelect = document.getElementById('admissionRoute');
const phaseNumberInput = document.getElementById('phaseNumber');
const submitBtn = document.getElementById('submitBtn');

document.addEventListener('DOMContentLoaded', async () => {
    customStepModal = new bootstrap.Modal(document.getElementById('customStepModal'), { backdrop: 'static' });
    deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));

    // Form Event Listeners
    templateForm.addEventListener('submit', handleFormSubmit);
    document.getElementById('confirmDeleteBtn').addEventListener('click', confirmDelete);
    
    //  Add Custom Step button
    document.getElementById('addCustomStepBtn')
        .addEventListener('click', () => openCustomStepModal());

    // Generate Pre Admission
    document.getElementById('generatePreAdmissionBtn')
        .addEventListener('click', generatePreAdmission);

    //  Counselling buttons
    document.querySelectorAll('[data-route]').forEach(btn => {
        btn.addEventListener('click', () => {
            generateCounselling(btn.dataset.route);
        });
    });

    // Delegate dynamic Edit/Delete buttons
    document.body.addEventListener('click', function (e) {

        // EDIT
        if (e.target.closest('.edit-btn')) {
            const btn = e.target.closest('.edit-btn');
            const stepJson = btn.dataset.step;
            openCustomStepModal(true, stepJson);
        }

        // DELETE
        if (e.target.closest('.delete-btn')) {
            const btn = e.target.closest('.delete-btn');
            initDelete(btn.dataset.id, btn.dataset.name);
        }
    });

    // Initial Load
    await loadTimeline();
});

// --- API CALLS: Loading ---
async function loadTimeline() {
    try {
        // CSP FIX: Replaced style.display with classList modifications
        loadingState.classList.remove('d-none');
        mainTimelineWrapper.classList.add('d-none');

        const res = await axios.get('/step-template-data/templates');
        allTemplates = res.data || [];

        renderTimeline();

        // CSP FIX: Replaced style.display with classList modifications
        loadingState.classList.add('d-none');
        mainTimelineWrapper.classList.remove('d-none');
    } catch (err) {
        console.error('Error loading templates', err);
        loadingState.innerHTML = '<p class="text-danger fw-bold"><i class="bi bi-exclamation-triangle me-2"></i>Failed to load timeline.</p>';
    }
}

// --- RENDER LOGIC ---
function renderTimeline() {
    preAdmissionZone.innerHTML = '';
    correctionZone.innerHTML = ''; // ADDED: Clear Correction Zone
    counsellingZone.innerHTML = '';

    const preAdmissionSteps = allTemplates.filter(t => t.category === 'PRE_ADMISSION');
    const correctionSteps = allTemplates.filter(t => t.category === 'CORRECTION'); // ADDED: Filter Correction Steps
    const counsellingSteps = allTemplates.filter(t => t.category === 'COUNSELLING');

    // 1. Render Pre-Admission Zone
    // CSP FIX: Replaced style.display with classList modifications
    if (preAdmissionSteps.length === 0) {
        preAdmissionGenerator.classList.remove('d-none');
    } else {
        preAdmissionGenerator.classList.add('d-none');
        preAdmissionSteps.forEach(step => preAdmissionZone.insertAdjacentHTML('beforeend', createNodeHTML(step)));
    }

    //  Render Correction Zone
    if (correctionSteps.length > 0) {
        correctionSteps.forEach(step => correctionZone.insertAdjacentHTML('beforeend', createNodeHTML(step)));
    }

    // 2. Render Counselling Zone
    if (counsellingSteps.length > 0) {
        counsellingSteps.forEach(step => counsellingZone.insertAdjacentHTML('beforeend', createNodeHTML(step)));
    }
}

// Generate the HTML for a single timeline step
function createNodeHTML(step) {
    const roleClass = `role-${step.defaultActorRole}`;
    const badgeClass = `role-${step.defaultActorRole}-badge`;

    // Generate subtle pill badges for Route and Phase if they exist
    let routeBadge = '';
    if (step.admissionRoute && step.admissionRoute !== 'GENERAL') {
        let routeColor = 'bg-secondary';
        let customClass = '';

        if (step.admissionRoute === 'CUET') routeColor = 'bg-success';
        else if (step.admissionRoute === 'NON_CUET') routeColor = 'bg-info text-dark';
        else if (step.admissionRoute === 'COMBINED') {
            routeColor = 'text-white';
            customClass = 'btn-combined-phase'; // CSP FIX: using our new CSS class instead of inline style
        }

        routeBadge = `<span class="badge ${routeColor} ms-2 rounded-pill ${customClass}">${escapeHTML(step.admissionRoute)}</span>`;
    }
    let phaseBadge = '';
    if (step.phaseNumber) {
        phaseBadge = `<span class="badge bg-secondary ms-1 rounded-pill">Phase ${step.phaseNumber}</span>`;
    }

    // Pass entirely escaped JSON string to edit function to prevent XSS in onClick
    const safeStepJson = escapeHTML(JSON.stringify(step));

    return `
<div class="timeline-node">
    <div class="timeline-badge ${badgeClass}">${step.stepOrder}</div>
    <div class="timeline-content ${roleClass}">
        <div class="d-flex justify-content-between align-items-start">
            <div>
                <h6 class="mb-1 fw-bold text-dark">
                    ${escapeHTML(step.stepName)}
                    ${routeBadge}
                    ${phaseBadge}
                </h6>
                <p class="text-muted small mb-3">${escapeHTML(step.description || 'No description provided.')}</p>
                <span class="badge bg-light text-dark border">
                    <i class="bi bi-person-badge me-1"></i>${escapeHTML(step.defaultActorRole)}
                </span>
            </div>
            <div class="timeline-actions">
                <button type="button"
                    class="btn btn-outline-primary btn-sm shadow-sm edit-btn"
                    data-step='${safeStepJson}'>
                    <i class="bi bi-pencil-fill me-1"></i> Edit
                </button>

                <button type="button"
                    class="btn btn-outline-danger btn-sm shadow-sm delete-btn"
                    data-id="${step.templateId}"
                    data-name="${escapeHTML(step.stepName)}">
                    <i class="bi bi-trash-fill me-1"></i> Delete
                </button>
            </div>
        </div>
    </div>
</div>
`;
}

// --- API CALLS: Smart Generators ---

async function generatePreAdmission() {
    try {
        const btn = preAdmissionGenerator.querySelector('button');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Generating...';

        await axios.post('/step-template-data/auto-generate/pre-admission');
        showAlert('Pre-Admission steps automatically generated!', 'success');
        await loadTimeline();
    } catch (err) {
        showAlert(err.response?.data || 'Failed to generate steps.', 'danger');
    }
}

async function generateCounselling(route) {
    try {
        const payload = new URLSearchParams({ route: route });
        await axios.post('/step-template-data/auto-generate/counselling', payload);
        showAlert(`${route} Counselling Phase added successfully!`, 'success');
        await loadTimeline();
    } catch (err) {
        showAlert(err.response?.data || 'Failed to generate counselling phase.', 'danger');
    }
}

// --- CUSTOM STEP MODAL LOGIC (Failsafe) ---

window.openCustomStepModal = function(isEdit = false, stepJsonStr = null) {
    templateForm.reset();
    templateForm.classList.remove('was-validated');

    isEditMode = isEdit;

    if (isEditMode && stepJsonStr) {
        // Parse the escaped JSON string back to an object
        const step = JSON.parse(stepJsonStr.replace(/&quot;/g, '"'));

        document.getElementById('customStepModalTitle').innerHTML = '<i class="bi bi-pencil-square me-2"></i>Edit Step';
        templateIdInput.value = step.templateId;
        stepOrderInput.value = step.stepOrder;
        stepNameInput.value = step.stepName;
        stepDescInput.value = step.description || '';
        actorRoleSelect.value = step.defaultActorRole;
        admissionRouteSelect.value = step.admissionRoute || 'GENERAL';
        phaseNumberInput.value = step.phaseNumber || '';

        // UPDATED: Bind the radio buttons to the newly added CORRECTION category
        if (step.category === 'COUNSELLING') {
            document.getElementById('catCounselling').checked = true;
        } else if (step.category === 'CORRECTION') {
            document.getElementById('catCorrection').checked = true;
        } else {
            document.getElementById('catPreAdmission').checked = true;
        }

    } else {
        document.getElementById('customStepModalTitle').innerHTML = '<i class="bi bi-gear-fill me-2"></i>Add Custom Step';
        templateIdInput.value = '';
        stepOrderInput.value = allTemplates.length + 1; // Auto-suggest end of timeline
        document.getElementById('catPreAdmission').checked = true;
    }

    customStepModal.show();
}

async function handleFormSubmit(e) {
    e.preventDefault();
    if (!templateForm.checkValidity()) {
        templateForm.classList.add('was-validated');
        return;
    }

    const payload = {
        stepOrder: parseInt(stepOrderInput.value),
        stepName: stepNameInput.value.trim(),
        category: document.querySelector('input[name="stepCategory"]:checked').value,
        admissionRoute: admissionRouteSelect.value,
        phaseNumber: phaseNumberInput.value ? parseInt(phaseNumberInput.value) : null,
        description: stepDescInput.value.trim(),
        defaultActorRole: actorRoleSelect.value
    };

    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

        if (isEditMode) {
            await axios.put(`/step-template-data/templates/${templateIdInput.value}`, payload);
            showAlert('Step updated successfully.', 'success');
        } else {
            await axios.post('/step-template-data/templates', payload);
            showAlert('Custom step added successfully.', 'success');
        }

        customStepModal.hide();
        await loadTimeline();
    } catch (err) {
        console.error('Save error', err);
        const msg = err.response?.data?.message || err.response?.data || 'Failed to save step.';
        showAlert(msg, 'danger');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-save me-2"></i>Save Step';
    }
}

// --- DELETE LOGIC ---

window.initDelete = function(id, name) {
    deleteTemplateId = id;
    document.getElementById('delete-step-name').textContent = name;
    deleteModal.show();
};

async function confirmDelete() {
    if (!deleteTemplateId) return;

    try {
        const btn = document.getElementById('confirmDeleteBtn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Deleting...';

        await axios.delete(`/step-template-data/templates/${deleteTemplateId}`);
        showAlert('Step deleted successfully. Timeline re-sequenced.', 'success');
        deleteModal.hide();
        await loadTimeline();
    } catch (err) {
        console.error('Delete error', err);
        showAlert('Failed to delete step.', 'danger');
    } finally {
        const btn = document.getElementById('confirmDeleteBtn');
        btn.disabled = false;
        btn.innerHTML = 'Delete Permanently';
    }
}

// --- UTILS ---
function showAlert(msg, type) {
    alertBox.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-triangle'}-fill me-2"></i>${escapeHTML(msg)}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    setTimeout(() => {
        const alert = bootstrap.Alert.getOrCreateInstance(alertBox.querySelector('.alert'));
        if(alert) alert.close();
    }, 4000);
}