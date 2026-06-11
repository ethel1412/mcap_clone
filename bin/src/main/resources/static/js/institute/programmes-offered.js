
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupEventListeners();
    setTimeout(() => {
        loadProgrammesOffered();
        loadDepartments();
    }, 100);
});

let programmeToDeleteId = null;
let programmeToDeleteName = '';
let departmentToDeleteName = '';
let programmeToEdit = null;

function initializePage() {
    console.log('Initializing Programmes Offered page...');
}

function setupEventListeners() {
    const addForm = document.getElementById('addProgrammeForm');
    if (addForm) {
        addForm.addEventListener('submit', handleAddProgramme);
    }

    const editForm = document.getElementById('editProgrammeForm');
    if (editForm) {
        editForm.addEventListener('submit', handleEditProgramme);
    }

    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', handleDeleteConfirmation);
    }

    // Department change listener for programme loading
    const departmentSelect = document.getElementById('departmentSelect');
    if (departmentSelect) {
        departmentSelect.addEventListener('change', function() {
            loadAvailableProgrammes(this.value);
        });
    }

    const addModal = document.getElementById('addProgrammeModal');
    if (addModal) {
        addModal.addEventListener('shown.bs.modal', function() {
            document.getElementById('departmentSelect').focus();
        });
        addModal.addEventListener('hidden.bs.modal', function() {
            resetAddForm();
        });
    }

    const editModal = document.getElementById('editProgrammeModal');
    if (editModal) {
        editModal.addEventListener('hidden.bs.modal', function() {
            resetEditForm();
        });
    }
}

async function loadProgrammesOffered() {
    const tableBody = document.getElementById('programmesTableBody');
    const totalCount = document.getElementById('totalCount');
    const alertBox = document.getElementById('alertBox');

    try {
        showLoadingState(tableBody);

        const response = await axios.get('/programmes-offered/data/my');
        const programmes = response.data;

        if (programmes && programmes.length > 0) {
            renderProgrammesTable(programmes);
            totalCount.textContent = `${programmes.length} programme${programmes.length !== 1 ? 's' : ''}`;
            alertBox.classList.add('d-none');
        } else {
            showEmptyState(tableBody);
            totalCount.textContent = '0 programmes';
            alertBox.classList.remove('d-none');
        }

    } catch (error) {
        console.error('Error loading programmes offered:', error);
        showErrorState(tableBody, 'Failed to load programmes. Please try again.');
        showToast('error', 'Error loading programmes. Please refresh the page.');
        totalCount.textContent = '0 programmes';
        alertBox.classList.remove('d-none');
    }
}

async function loadDepartments() {
    const departmentSelect = document.getElementById('departmentSelect');
    const editDepartmentSelect = document.getElementById('editDepartmentSelect');

    try {
        const response = await axios.get('/institute-departments/data/my');
        const departments = response.data;

        const departmentOptions = departments
            .filter(dept => dept.active) // Only active departments
            .sort((a, b) => a.departmentName.localeCompare(b.departmentName))
            .map(dept => `<option value="${dept.instituteDepartmentId}">${dept.departmentName}</option>`)
            .join('');

        if (departmentOptions) {
            departmentSelect.innerHTML = '<option value="">Select a department...</option>' + departmentOptions;
            editDepartmentSelect.innerHTML = '<option value="">Select a department...</option>' + departmentOptions;
            departmentSelect.disabled = false;
            editDepartmentSelect.disabled = false;
        } else {
            departmentSelect.innerHTML = '<option value="">No active departments found</option>';
            editDepartmentSelect.innerHTML = '<option value="">No active departments found</option>';
            departmentSelect.disabled = true;
            editDepartmentSelect.disabled = true;
        }

    } catch (error) {
        console.error('Error loading departments:', error);
        departmentSelect.innerHTML = '<option value="">Error loading departments</option>';
        editDepartmentSelect.innerHTML = '<option value="">Error loading departments</option>';
        departmentSelect.disabled = true;
        editDepartmentSelect.disabled = true;
    }
}

// Updated to use checkbox container instead of select dropdown
async function loadAvailableProgrammes(instituteDepartmentId) {
    const programmeContainer = document.getElementById('programmeCheckboxContainer');

    if (!instituteDepartmentId) {
        programmeContainer.innerHTML = '<div class="text-muted text-center py-3">Select a department first</div>';
        return;
    }

    try {
        programmeContainer.innerHTML = '<div class="text-muted text-center py-3">Loading programmes...</div>';

        // Get all programmes
        const allProgrammesResponse = await axios.get('/programme-data');
        const allProgrammes = allProgrammesResponse.data;

        // Get already offered programmes for this institute
        const offeredProgrammesResponse = await axios.get('/programmes-offered/data/my');
        const offeredProgrammes = offeredProgrammesResponse.data;

        // Filter out programmes already offered by this department
        const offeredProgrammeIds = offeredProgrammes
            .filter(co => co.instituteDepartmentId == instituteDepartmentId)
            .map(co => co.programmeId);

        const availableProgrammes = allProgrammes.filter(programme =>
            !offeredProgrammeIds.includes(programme.programmeId)
        );

        if (availableProgrammes.length > 0) {
            const checkboxes = availableProgrammes
                .sort((a, b) => a.programmeName.localeCompare(b.programmeName))
                .map(programme => `
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" value="${programme.programmeId}" 
                               id="programme_${programme.programmeId}">
                        <label class="form-check-label" for="programme_${programme.programmeId}">
                            ${programme.programmeName} 
                            <span class="badge bg-info ms-1">${programme.programmeLevel || 'N/A'}</span>
                        </label>
                    </div>
                `).join('');

            programmeContainer.innerHTML = checkboxes;
        } else {
            programmeContainer.innerHTML = '<div class="text-muted text-center py-3">No available programmes for this department</div>';
        }

    } catch (error) {
        console.error('Error loading available programmes:', error);
        programmeContainer.innerHTML = '<div class="text-danger text-center py-3">Error loading programmes</div>';
    }
}

function renderProgrammesTable(programmes) {
    const tableBody = document.getElementById('programmesTableBody');

    tableBody.innerHTML = programmes.map((programme, index) => `
        <tr>
            <td class="text-center">${index + 1}</td>
            <td>
                <div class="fw-bold text-primary">${escapeHtml(programme.programmeName)}</div>
            </td>
            <td>
                <span class="badge bg-light text-dark">${escapeHtml(programme.departmentName)}</span>
            </td>
            <td>
                <span class="badge bg-info text-dark">${escapeHtml(programme.programmeLevel || 'N/A')}</span>
            </td>
            <td>
                <span class="badge bg-secondary">${escapeHtml(programme.streamName || 'N/A')}</span>
            </td>
            <td class="text-center">
                <div class="btn-group" role="group">
                    <button type="button" class="btn btn-sm btn-outline-primary" 
                            onclick="showEditModal(${programme.programmeOfferedId}, '${escapeHtml(programme.programmeName)}', ${programme.instituteDepartmentId})"
                            data-bs-toggle="tooltip" title="Edit Programme Assignment">
                        <i class="fas fa-pen"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-success" 
                            onclick="manageProgrammeSubjects(${programme.programmeOfferedId}, '${escapeHtml(programme.programmeName)}', '${escapeHtml(programme.departmentName)}')"
                            data-bs-toggle="tooltip" title="Manage Subjects">
                        <i class="fas fa-book-open"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-danger" 
                            onclick="showDeleteConfirmation(${programme.programmeOfferedId}, '${escapeHtml(programme.programmeName)}', '${escapeHtml(programme.departmentName)}')"
                            data-bs-toggle="tooltip" title="Remove Programme">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function showEditModal(programmeOfferedId, programmeName, instituteDepartmentId) {
    programmeToEdit = {
        id: programmeOfferedId,
        name: programmeName,
        departmentId: instituteDepartmentId
    };

    document.getElementById('editProgrammeName').textContent = programmeName;
    document.getElementById('editDepartmentSelect').value = instituteDepartmentId;

    const modal = new bootstrap.Modal(document.getElementById('editProgrammeModal'));
    modal.show();
}

// Updated to handle multiple checkbox selections
async function handleAddProgramme(event) {
    event.preventDefault();

    const form = event.target;
    const submitBtn = document.getElementById('submitBtn');
    const originalBtnText = submitBtn.innerHTML;

    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }

    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Adding...';

        const formData = new FormData(form);
        const instituteDepartmentId = parseInt(formData.get('instituteDepartmentId'));

        // Get selected programme IDs from checkboxes
        const selectedCheckboxes = document.querySelectorAll('#programmeCheckboxContainer input[type="checkbox"]:checked');
        const selectedProgrammeIds = Array.from(selectedCheckboxes).map(cb => parseInt(cb.value));

        if (selectedProgrammeIds.length === 0) {
            showToast('error', 'Please select at least one programme.');
            document.getElementById('programmeCheckboxContainer').classList.add('border-danger');
            return;
        }

        // Remove error styling
        document.getElementById('programmeCheckboxContainer').classList.remove('border-danger');

        // Add programmes one by one
        for (const programmeId of selectedProgrammeIds) {
            const data = {
                instituteDepartmentId: instituteDepartmentId,
                programmeId: programmeId
            };

            await axios.post('/programmes-offered/data', data, {
                headers: { 'Content-Type': 'application/json' }
            });
        }

        const programmesCount = selectedProgrammeIds.length;
        showToast('success', `${programmesCount} programme${programmesCount > 1 ? 's' : ''} added successfully!`);

        // Properly close modal and remove backdrop
        const modal = bootstrap.Modal.getInstance(document.getElementById('addProgrammeModal'));
        if (modal) {
            modal.hide();
        }
        forceRemoveModalBackdrop();

        await loadProgrammesOffered();

    } catch (error) {
        console.error('Error adding programme:', error);
        handleApiError(error, form);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnText;
    }
}

async function handleEditProgramme(event) {
    event.preventDefault();

    if (!programmeToEdit) return;

    const form = event.target;
    const submitBtn = document.getElementById('editSubmitBtn');
    const originalBtnText = submitBtn.innerHTML;

    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }

    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Updating...';

        const formData = new FormData(form);

        // Get the current programme data to preserve programmeId
        const currentResponse = await axios.get(`/programmes-offered/data/${programmeToEdit.id}`);
        const currentProgramme = currentResponse.data;

        const data = {
            instituteDepartmentId: parseInt(formData.get('instituteDepartmentId')),
            programmeId: currentProgramme.programmeId // Keep the same programme
        };

        await axios.put(`/programmes-offered/data/${programmeToEdit.id}`, data, {
            headers: { 'Content-Type': 'application/json' }
        });

        showToast('success', `${programmeToEdit.name} updated successfully!`);

        const modal = bootstrap.Modal.getInstance(document.getElementById('editProgrammeModal'));
        if (modal) {
            modal.hide();
        }
        forceRemoveModalBackdrop();

        await loadProgrammesOffered();

    } catch (error) {
        console.error('Error updating programme:', error);
        handleApiError(error, form);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnText;
    }
}

function showDeleteConfirmation(programmeOfferedId, programmeName, departmentName) {
    programmeToDeleteId = programmeOfferedId;
    programmeToDeleteName = programmeName;
    departmentToDeleteName = departmentName;

    document.getElementById('programmeToDelete').textContent = programmeName;
    document.getElementById('departmentToDelete').textContent = departmentName;

    const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    modal.show();
}

async function handleDeleteConfirmation() {
    if (!programmeToDeleteId) return;

    const confirmBtn = document.getElementById('confirmDeleteBtn');
    const originalBtnText = confirmBtn.innerHTML;

    try {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Deleting...';

        await axios.delete(`/programmes-offered/data/${programmeToDeleteId}`);

        showToast('success', `${programmeToDeleteName} removed from ${departmentToDeleteName} successfully!`);

        const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
        if (modal) {
            modal.hide();
        }
        forceRemoveModalBackdrop();

        await loadProgrammesOffered();

    } catch (error) {
        console.error('Error deleting programme:', error);
        const errorMessage = error.response?.data || 'Failed to remove programme. Please try again.';
        showToast('error', errorMessage);
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = originalBtnText;
        programmeToDeleteId = null;
        programmeToDeleteName = '';
        departmentToDeleteName = '';
    }
}

function showLoadingState(tableBody) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="6" class="text-center py-4">
                <div class="spinner-border spinner-border-sm me-2" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                Loading programmes...
            </td>
        </tr>
    `;
}

function showEmptyState(tableBody) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="6" class="text-center py-5">
                <div class="text-muted">
                    <i class="fas fa-graduation-cap fa-3x mb-3 d-block"></i>
                    <h5>No Programmes Offered</h5>
                    <p>Click "Add Programme" to start offering programmes to students.</p>
                </div>
            </td>
        </tr>
    `;
}

function showErrorState(tableBody, message) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="6" class="text-center py-4">
                <div class="text-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    ${message}
                </div>
            </td>
        </tr>
    `;
}

// Navigate to programme subjects management
function manageProgrammeSubjects(programmeOfferedId, programmeName, departmentName) {
    console.log('Managing subjects for programme:', programmeOfferedId, programmeName, departmentName);

    // Navigate to the programme subjects page with parameters
    const params = new URLSearchParams({
        programmeOfferedId: programmeOfferedId,
        programmeName: programmeName,
        departmentName: departmentName
    });

    window.location.href = `/programme-subjects?${params.toString()}`;
}


function resetAddForm() {
    const form = document.getElementById('addProgrammeForm');
    form.reset();
    form.classList.remove('was-validated');
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    // Reset checkbox container instead of select
    const programmeContainer = document.getElementById('programmeCheckboxContainer');
    programmeContainer.innerHTML = '<div class="text-muted text-center py-3">Select a department first</div>';
    programmeContainer.classList.remove('border-danger');
}

function resetEditForm() {
    const form = document.getElementById('editProgrammeForm');
    form.reset();
    form.classList.remove('was-validated');
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    programmeToEdit = null;
}

function handleApiError(error, form) {
    if (error.response && error.response.status === 400) {
        const errorData = error.response.data;
        if (typeof errorData === 'object' && errorData.errors) {
            handleValidationErrors(form, errorData.errors);
        } else {
            showToast('error', errorData.message || 'Validation error occurred.');
        }
    } else if (error.response && error.response.status === 409) {
        showToast('error', 'This programme is already offered by this department.');
    } else {
        const errorMessage = error.response?.data?.message || 'Operation failed. Please try again.';
        showToast('error', errorMessage);
    }
}

function handleValidationErrors(form, errorData) {
    if (typeof errorData === 'object' && errorData !== null) {
        Object.keys(errorData).forEach(field => {
            const input = form.querySelector(`[name="${field}"]`);
            if (input) {
                input.classList.add('is-invalid');
                const feedback = input.nextElementSibling;
                if (feedback && feedback.classList.contains('invalid-feedback')) {
                    feedback.textContent = errorData[field];
                }
            }
        });
    } else {
        showToast('error', errorData || 'Validation error occurred.');
    }
}

function showToast(type, message) {
    const toastId = type === 'success' ? 'successToast' : 'errorToast';
    const messageId = type === 'success' ? 'successMessage' : 'errorMessage';

    const toastEl = document.getElementById(toastId);
    const messageEl = document.getElementById(messageId);

    if (toastEl && messageEl) {
        messageEl.textContent = message;
        const toast = new bootstrap.Toast(toastEl, {
            autohide: true,
            delay: type === 'success' ? 3000 : 5000
        });
        toast.show();
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

//Backdrop removal function
function forceRemoveModalBackdrop() {
    setTimeout(() => {
        const backdrops = document.querySelectorAll('.modal-backdrop');
        backdrops.forEach(backdrop => backdrop.remove());

        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';
        document.body.style.removeProperty('overflow');
        document.body.style.removeProperty('padding-right');
    }, 100);
}

// Global function exports
window.showDeleteConfirmation = showDeleteConfirmation;
window.showEditModal = showEditModal;
