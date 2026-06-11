
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupEventListeners();
    setTimeout(() => {
        loadMyDepartments();
        loadAvailableDepartments();
    }, 100);
});

let departmentToDeleteId = null;
let departmentToDeleteName = '';
let departmentToEdit = null;

function initializePage() {
    console.log('Initializing My Departments page...');
}

function setupEventListeners() {
    const addForm = document.getElementById('addDepartmentForm');
    if (addForm) {
        addForm.addEventListener('submit', handleAddDepartment);
    }

    const editForm = document.getElementById('editDepartmentForm');
    if (editForm) {
        editForm.addEventListener('submit', handleEditDepartment);
    }

    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', handleDeleteConfirmation);
    }

    const addModal = document.getElementById('addDepartmentModal');
    if (addModal) {
        addModal.addEventListener('shown.bs.modal', function() {
            document.getElementById('departmentSelect').focus();
        });
        addModal.addEventListener('hidden.bs.modal', function() {
            resetAddForm();
        });
    }

    const editModal = document.getElementById('editDepartmentModal');
    if (editModal) {
        editModal.addEventListener('hidden.bs.modal', function() {
            resetEditForm();
        });
    }
}

async function loadMyDepartments() {
    const tableBody = document.getElementById('departmentsTableBody');
    const totalCount = document.getElementById('totalCount');
    const alertBox = document.getElementById('alertBox');

    try {
        showLoadingState(tableBody);

        const response = await axios.get('/institute-departments/data/my');
        const departments = response.data;

        if (departments && departments.length > 0) {
            renderDepartmentsTable(departments);
            totalCount.textContent = `${departments.length} department${departments.length !== 1 ? 's' : ''}`;
            alertBox.classList.add('d-none');
        } else {
            showEmptyState(tableBody);
            totalCount.textContent = '0 departments';
            alertBox.classList.remove('d-none');
        }

    } catch (error) {
        console.error('Error loading my departments:', error);
        showErrorState(tableBody, 'Failed to load departments. Please try again.');
        showToast('error', 'Error loading departments. Please refresh the page.');
        totalCount.textContent = '0 departments';
        alertBox.classList.remove('d-none');
    }
}

async function loadAvailableDepartments() {
    const departmentSelect = document.getElementById('departmentSelect');

    try {
        const allDepartmentsResponse = await axios.get('/institute-departments/data/departments');
        const allDepartments = allDepartmentsResponse.data;

        const myDepartmentsResponse = await axios.get('/institute-departments/data/my');
        const myDepartments = myDepartmentsResponse.data;

        const assignedDepartmentIds = myDepartments.map(dept => dept.departmentId);
        const availableDepartments = allDepartments.filter(dept =>
            !assignedDepartmentIds.includes(dept.departmentId)
        );

        departmentSelect.innerHTML = '';
        if (availableDepartments.length === 0) {
            departmentSelect.innerHTML = '<option value="">No departments available to add</option>';
            departmentSelect.disabled = true;
        } else {
            departmentSelect.innerHTML = '<option value="">Select a department...</option>';
            availableDepartments
                .sort((a, b) => a.departmentName.localeCompare(b.departmentName))
                .forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept.departmentId;
                    const code = dept.departmentCode ? ` (${dept.departmentCode})` : '';
                    option.textContent = `${dept.departmentName}${code}`;
                    departmentSelect.appendChild(option);
                });
            departmentSelect.disabled = false;
        }

    } catch (error) {
        console.error('Error loading available departments:', error);
        departmentSelect.innerHTML = '<option value="">Error loading departments</option>';
        departmentSelect.disabled = true;
    }
}

function renderDepartmentsTable(departments) {
    const tableBody = document.getElementById('departmentsTableBody');

    tableBody.innerHTML = departments.map((dept, index) => `
        <tr>
            <td class="text-center">${index + 1}</td>
            <td>
                <div class="fw-bold text-primary">${escapeHtml(dept.departmentName)}</div>
            </td>
            <td>
                <span class="badge bg-light text-dark">${escapeHtml(dept.departmentCode || 'N/A')}</span>
            </td>
            <td>${escapeHtml(dept.hodName || 'Not specified')}</td>
            <td>
                <div class="small">
                    ${dept.email ? `<div><i class="fas fa-envelope text-muted me-1"></i>${escapeHtml(dept.email)}</div>` : ''}
                    ${dept.phone ? `<div><i class="fas fa-phone text-muted me-1"></i>${escapeHtml(dept.phone)}</div>` : ''}
                    ${!dept.email && !dept.phone ? 'Not provided' : ''}
                </div>
            </td>
            <td class="text-center">
                <span class="badge ${dept.active ? 'bg-success' : 'bg-secondary'}">
                    ${dept.active ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td class="text-center">
                <div class="btn-group" role="group">
                    <button type="button" class="btn btn-sm btn-outline-primary" 
                            onclick="showEditModal(${dept.instituteDepartmentId}, '${escapeHtml(dept.departmentName)}', '${escapeHtml(dept.hodName || '')}', '${escapeHtml(dept.email || '')}', '${escapeHtml(dept.phone || '')}', ${dept.active})"
                            data-bs-toggle="tooltip" title="Edit Department">
                        <i class="fas fa-pen"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-danger" 
                            onclick="showDeleteConfirmation(${dept.instituteDepartmentId}, '${escapeHtml(dept.departmentName)}')"
                            data-bs-toggle="tooltip" title="Remove Department">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function showEditModal(instituteDepartmentId, departmentName, hodName, email, phone, active) {
    departmentToEdit = {
        id: instituteDepartmentId,
        name: departmentName
    };

    // Pre-fill the form
    document.getElementById('editDepartmentName').textContent = departmentName;
    document.getElementById('editHodName').value = hodName || '';
    document.getElementById('editEmail').value = email || '';
    document.getElementById('editPhone').value = phone || '';
    document.getElementById('editActive').value = active.toString();

    const modal = new bootstrap.Modal(document.getElementById('editDepartmentModal'));
    modal.show();
}

// ✅ FIXED: Clean add department function
async function handleAddDepartment(event) {
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
        const data = {
            departmentId: parseInt(formData.get('departmentId')),
            hodName: formData.get('hodName') || null,
            email: formData.get('email') || null,
            phone: formData.get('phone') || null,
            active: formData.get('active') === 'true'
        };

        await axios.post('/institute-departments/data', data, {
            headers: { 'Content-Type': 'application/json' }
        });

        showToast('success', 'Department added successfully!');

        // ✅ FIXED: Clean modal close - no duplicate code
        const modal = bootstrap.Modal.getInstance(document.getElementById('addDepartmentModal'));
        if (modal) {
            modal.hide();
        }
        forceRemoveModalBackdrop();

        await loadMyDepartments();
        await loadAvailableDepartments();

    } catch (error) {
        console.error('Error adding department:', error);
        handleApiError(error, form);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnText;
    }
}

// ✅ FIXED: Clean edit department function
async function handleEditDepartment(event) {
    event.preventDefault();

    if (!departmentToEdit) return;

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
        const data = {
            hodName: formData.get('hodName') || null,
            email: formData.get('email') || null,
            phone: formData.get('phone') || null,
            active: formData.get('active') === 'true'
        };

        await axios.put(`/institute-departments/data/${departmentToEdit.id}`, data, {
            headers: { 'Content-Type': 'application/json' }
        });

        showToast('success', `${departmentToEdit.name} updated successfully!`);

        // ✅ FIXED: Clean modal close - no duplicate code
        const modal = bootstrap.Modal.getInstance(document.getElementById('editDepartmentModal'));
        if (modal) {
            modal.hide();
        }
        forceRemoveModalBackdrop();

        await loadMyDepartments();

    } catch (error) {
        console.error('Error updating department:', error);
        handleApiError(error, form);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalBtnText;
    }
}

function showDeleteConfirmation(instituteDepartmentId, departmentName) {
    departmentToDeleteId = instituteDepartmentId;
    departmentToDeleteName = departmentName;

    document.getElementById('departmentToDelete').textContent = departmentName;

    const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    modal.show();
}

async function handleDeleteConfirmation() {
    if (!departmentToDeleteId) return;

    const confirmBtn = document.getElementById('confirmDeleteBtn');
    const originalBtnText = confirmBtn.innerHTML;

    try {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Deleting...';

        await axios.delete(`/institute-departments/data/${departmentToDeleteId}`);

        showToast('success', `${departmentToDeleteName} removed successfully!`);

        const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
        if (modal) {
            modal.hide();
        }
        forceRemoveModalBackdrop();

        await loadMyDepartments();
        await loadAvailableDepartments();

    } catch (error) {
        console.error('Error deleting department:', error);
        const errorMessage = error.response?.data || 'Failed to remove department. Please try again.';
        showToast('error', errorMessage);
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = originalBtnText;
        departmentToDeleteId = null;
        departmentToDeleteName = '';
    }
}

function showLoadingState(tableBody) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center py-4">
                <div class="spinner-border spinner-border-sm me-2" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                Loading departments...
            </td>
        </tr>
    `;
}

function showEmptyState(tableBody) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center py-5">
                <div class="text-muted">
                    <i class="fas fa-building fa-3x mb-3 d-block"></i>
                    <h5>No Departments Added</h5>
                    <p>Click "Add Department" to start managing your institute's departments.</p>
                </div>
            </td>
        </tr>
    `;
}

function showErrorState(tableBody, message) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center py-4">
                <div class="text-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    ${message}
                </div>
            </td>
        </tr>
    `;
}

function resetAddForm() {
    const form = document.getElementById('addDepartmentForm');
    form.reset();
    form.classList.remove('was-validated');
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    const departmentSelect = document.getElementById('departmentSelect');
    if (departmentSelect.options.length > 0) {
        departmentSelect.selectedIndex = 0;
    }
}

function resetEditForm() {
    const form = document.getElementById('editDepartmentForm');
    form.reset();
    form.classList.remove('was-validated');
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    departmentToEdit = null;
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
        showToast('error', 'This department is already assigned to your institute.');
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

// ✅ ULTIMATE BACKDROP FIX - Aggressive removal with multiple checks
function forceRemoveModalBackdrop() {
    // Immediate removal
    const backdrops = document.querySelectorAll('.modal-backdrop');
    backdrops.forEach(backdrop => {
        backdrop.remove();
        console.log('Removed backdrop:', backdrop);
    });

    // Reset body styles immediately
    document.body.classList.remove('modal-open');
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
    document.body.style.removeProperty('overflow');
    document.body.style.removeProperty('padding-right');

    // Double-check after 50ms
    setTimeout(() => {
        const remainingBackdrops = document.querySelectorAll('.modal-backdrop');
        remainingBackdrops.forEach(backdrop => {
            backdrop.remove();
            console.log('Removed remaining backdrop:', backdrop);
        });

        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';
    }, 50);

    // Triple-check after 200ms
    setTimeout(() => {
        const stillBackdrops = document.querySelectorAll('.modal-backdrop');
        stillBackdrops.forEach(backdrop => backdrop.remove());

        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';

        // Force body reset
        document.body.setAttribute('style', '');
        document.body.className = document.body.className.replace(/modal-open/g, '');

        console.log('Final backdrop cleanup completed');
    }, 200);
}

// Global function exports
window.showDeleteConfirmation = showDeleteConfirmation;
window.showEditModal = showEditModal;
