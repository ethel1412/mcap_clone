// Global variables
let programmeOfferedId = null;
let programmeName = '';
let departmentName = '';
let semesterToDelete = null;
let currentSemesterForSubjects = null;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Programme Subjects page loaded');
    initializePage();
    setupEventListeners();
    // Short delay to ensure page is fully loaded
    setTimeout(() => {
        loadProgrammeSemesters();
    }, 100);
});

function initializePage() {
    console.log('Initializing Programme Subjects page...');

    // Get programme info from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    programmeOfferedId = urlParams.get('programmeOfferedId');
    programmeName = urlParams.get('programmeName');
    departmentName = urlParams.get('departmentName');

    console.log('Programme Info:', { programmeOfferedId, programmeName, departmentName });

    // Set hidden input in add semester form
    const programmeOfferedIdInput = document.getElementById('programmeOfferedId');
    if (programmeOfferedIdInput) {
        programmeOfferedIdInput.value = programmeOfferedId;
    }
}

function setupEventListeners() {
    // Add Semester Form
    const addSemesterForm = document.getElementById('addSemesterForm');
    if (addSemesterForm) {
        addSemesterForm.addEventListener('submit', handleAddSemester);
    }

    // Add Subjects Form
    const addSubjectsForm = document.getElementById('addSubjectsForm');
    if (addSubjectsForm) {
        addSubjectsForm.addEventListener('submit', handleAddSubjects);
    }

    // Delete Semester Confirmation
    const confirmDeleteBtn = document.getElementById('confirmDeleteSemesterBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', handleDeleteSemester);
    }

    // Modal event listeners with proper cleanup
    const addSemesterModal = document.getElementById('addSemesterModal');
    if (addSemesterModal) {
        addSemesterModal.addEventListener('shown.bs.modal', function() {
            document.getElementById('semesterNumber').focus();
        });
        addSemesterModal.addEventListener('hidden.bs.modal', function() {
            resetAddSemesterForm();
            cleanupModalBackdrop();
        });
    }

    const addSubjectsModal = document.getElementById('addSubjectsModal');
    if (addSubjectsModal) {
        addSubjectsModal.addEventListener('hidden.bs.modal', function() {
            resetAddSubjectsForm();
            cleanupModalBackdrop();
        });
    }

    const deleteSemesterModal = document.getElementById('deleteSemesterModal');
    if (deleteSemesterModal) {
        deleteSemesterModal.addEventListener('hidden.bs.modal', function() {
            cleanupModalBackdrop();
        });
    }
}

async function loadProgrammeSemesters() {
    const container = document.getElementById('semestersContainer');
    const alertBox = document.getElementById('alertBox');

    try {
        showLoadingState();

        const response = await axios.get(`/semesters/data/by-programme/${programmeOfferedId}`);
        const semesters = response.data;

        hideLoadingState();

        if (semesters && semesters.length > 0) {
            renderSemesters(semesters);
            alertBox.classList.add('d-none');
        } else {
            showEmptyState();
            alertBox.classList.remove('d-none');
        }
    } catch (error) {
        console.error('Error loading semesters:', error);
        hideLoadingState();
        showErrorState('Failed to load semesters. Please try again.');
        showToast('error', 'Error loading semesters. Please refresh the page.');
        alertBox.classList.remove('d-none');
    }
}

function renderSemesters(semesters) {
    const container = document.getElementById('semestersContainer');

    const semesterCards = semesters
        .sort((a, b) => a.semesterNumber - b.semesterNumber)
        .map(semester => createSemesterCard(semester))
        .join('');

    container.innerHTML = semesterCards;

    // Load subjects for each semester
    semesters.forEach(semester => {
        loadSemesterSubjects(semester.semesterId);
    });
}

function createSemesterCard(semester) {
    const semesterTitle = semester.semesterName || `Semester ${semester.semesterNumber}`;

    return `
        <div class="semester-card" data-semester-id="${semester.semesterId}">
            <div class="semester-header">
                <h5 class="semester-title">
                    <i class="fas fa-calendar-alt"></i>
                    ${escapeHtml(semesterTitle)}
                </h5>
                <div class="semester-actions">
                    <button type="button" class="btn btn-sm btn-light" 
                            onclick="showAddSubjectsModal(${semester.semesterId}, '${escapeHtml(semesterTitle)}')"
                            title="Add Subjects">
                        <i class="fas fa-plus text-primary"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-light" 
                            onclick="showDeleteSemesterModal(${semester.semesterId}, '${escapeHtml(semesterTitle)}')"
                            title="Delete Semester">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
            
            <div class="table-responsive">
                <table class="table subjects-table mb-0">
                    <thead>
                        <tr>
                            <th style="width: 10%" class="text-center">#</th>
                            <th style="width: 60%">Subject Name</th>
                            <th style="width: 20%">Subject Code</th>
                            <th style="width: 10%" class="text-center">Actions</th>
                        </tr>
                    </thead>
                    <tbody id="subjects-${semester.semesterId}">
                        <tr>
                            <td colspan="4" class="loading-subjects">
                                <div class="spinner-border spinner-border-sm text-primary"></div>
                                Loading subjects...
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    `;
}


async function loadSemesterSubjects(semesterId) {
    const tbody = document.getElementById(`subjects-${semesterId}`);

    try {
        const response = await axios.get(`/subject-assignments/data/by-semester/${semesterId}`);
        const subjects = response.data;

        renderSemesterSubjects(semesterId, subjects);
    } catch (error) {
        console.error(`Error loading subjects for semester ${semesterId}:`, error);
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="error-subjects">
                        <i class="fas fa-exclamation-triangle"></i>
                        <div>Failed to load subjects</div>
                    </td>
                </tr>
            `;
        }
    }
}

function renderSemesterSubjects(semesterId, subjects) {
    const tbody = document.getElementById(`subjects-${semesterId}`);

    if (!subjects || subjects.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="p-0">
                    <div class="empty-semester">
                        <i class="fas fa-book-open"></i>
                        <h6>No subjects assigned yet</h6>
                        <p>Start building your curriculum by adding subjects to this semester.</p>
                        <button type="button" class="btn btn-outline-primary btn-sm" 
                                onclick="showAddSubjectsModal(${semesterId}, 'Semester')">
                            <i class="fas fa-plus me-1"></i>Add First Subject
                        </button>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    const subjectRows = subjects
        .sort((a, b) => a.subjectName.localeCompare(b.subjectName))
        .map((subject, index) => `
            <tr>
                <td class="text-center">
                    <span class="row-number">${index + 1}</span>
                </td>
                <td>
                    <div class="subject-name">${escapeHtml(subject.subjectName)}</div>
                    ${subject.description ? `<div class="subject-description">${escapeHtml(subject.description)}</div>` : ''}
                </td>
                <td>
                    <span class="subject-code">${escapeHtml(subject.subjectCode || 'N/A')}</span>
                </td>
                <td class="text-center">
                    <button type="button" class="btn btn-outline-danger action-btn" 
                            onclick="confirmRemoveSubject(${subject.assignmentId}, '${escapeHtml(subject.subjectName)}', ${semesterId})"
                            title="Remove Subject">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');

    tbody.innerHTML = subjectRows;
}


function getSubjectTypeBadgeClass(type) {
    switch(type?.toLowerCase()) {
        case 'core': return 'bg-primary';
        case 'elective': return 'bg-success';
        case 'practical': return 'bg-info';
        case 'theory': return 'bg-warning';
        default: return 'bg-secondary';
    }
}

async function confirmRemoveSubject(assignmentId, subjectName, semesterId) {
    if (!confirm(`Are you sure you want to remove "${subjectName}" from this semester?`)) {
        return;
    }

    try {
        // Show loading state on the row
        const btn = event.target.closest('button');
        const originalHtml = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        btn.disabled = true;

        await axios.delete(`/subject-assignments/data/${assignmentId}`);

        showToast('success', `Subject "${subjectName}" removed successfully!`);

        // Reload subjects for this semester
        await loadSemesterSubjects(semesterId);

    } catch (error) {
        console.error('Error removing subject:', error);
        const message = error.response?.data?.message || 'Failed to remove subject. Please try again.';
        showToast('error', message);

        // Reset button state
        if (btn) {
            btn.innerHTML = originalHtml;
            btn.disabled = false;
        }
    }
}

function showAddSubjectsModal(semesterId, semesterTitle) {
    currentSemesterForSubjects = semesterId;

    document.getElementById('modalSemesterId').value = semesterId;
    document.getElementById('modalSemesterTitle').textContent = semesterTitle;

    // Load available subjects
    loadAvailableSubjects(semesterId);

    const modalEl = document.getElementById('addSubjectsModal');
    let modal = bootstrap.Modal.getInstance(modalEl);
    if (!modal) {
        modal = new bootstrap.Modal(modalEl);
    }
    modal.show();
}

async function loadAvailableSubjects(semesterId) {
    const container = document.getElementById('subjectsContainer');

    try {
        // Clear any existing content
        container.innerHTML = `
            <div class="mb-3">
                <div class="input-group">
                    <input type="text" class="form-control" id="subjectSearchInput" 
                           placeholder="Search subjects by name..." autocomplete="off">
                    <button class="btn btn-outline-secondary" type="button" id="clearSearchBtn">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="form-text">
                    <i class="fas fa-info-circle me-1"></i>
                    Type to search subjects by name. Search is case-insensitive.
                </div>
            </div>
            <div id="subjectsList" style="max-height: 250px; overflow-y: auto;">
                <div class="text-center py-3">
                    <div class="spinner-border spinner-border-sm" role="status"></div> 
                    Loading available subjects...
                </div>
            </div>
        `;

        // Add search functionality
        setupSubjectSearch(semesterId);

        // Load initial subjects
        await performSubjectSearch(semesterId, '');

    } catch (error) {
        console.error('Error setting up subject search:', error);
        container.innerHTML = '<div class="text-danger text-center py-3">Error loading subjects. Please try again.</div>';
    }
}

function setupSubjectSearch(semesterId) {
    const searchInput = document.getElementById('subjectSearchInput');
    const clearBtn = document.getElementById('clearSearchBtn');
    let searchTimeout;

    // Search on input with debouncing
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        const searchTerm = this.value.trim();

        searchTimeout = setTimeout(async () => {
            await performSubjectSearch(semesterId, searchTerm);
        }, 300); // 300ms delay
    });

    // Clear search
    clearBtn.addEventListener('click', function() {
        searchInput.value = '';
        performSubjectSearch(semesterId, '');
        searchInput.focus();
    });

    // Clear on Escape key
    searchInput.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            this.value = '';
            performSubjectSearch(semesterId, '');
        }
    });
}

async function performSubjectSearch(semesterId, searchTerm) {
    const subjectsList = document.getElementById('subjectsList');

    try {
        // Show loading
        subjectsList.innerHTML = `
            <div class="text-center py-3">
                <div class="spinner-border spinner-border-sm" role="status"></div>
                ${searchTerm ? `Searching for "${searchTerm}"...` : 'Loading subjects...'}
            </div>
        `;

        // Fetch subjects with search parameter
        const searchParam = searchTerm ? `?search=${encodeURIComponent(searchTerm)}` : '';

        const [allSubjectsResponse, assignedSubjectsResponse] = await Promise.all([
            axios.get(`/semesters/data/all-subjects${searchParam}`),
            axios.get(`/subject-assignments/data/by-semester/${semesterId}`)
        ]);

        const allSubjects = allSubjectsResponse.data || [];
        const assignedSubjects = assignedSubjectsResponse.data || [];

        console.log(`Search "${searchTerm}" returned ${allSubjects.length} subjects`);

        // Get IDs of already assigned subjects
        const assignedSubjectIds = new Set(assignedSubjects.map(assigned => assigned.subjectId));

        // Filter out already assigned subjects
        const availableSubjects = allSubjects.filter(subject =>
            !assignedSubjectIds.has(subject.subjectId)
        );

        if (availableSubjects && availableSubjects.length > 0) {
            const subjectCheckboxes = availableSubjects
                .sort((a, b) => a.subjectName.localeCompare(b.subjectName))
                .map(subject => {
                    // Highlight search term in subject name
                    let highlightedName = escapeHtml(subject.subjectName);
                    if (searchTerm) {
                        const regex = new RegExp(`(${escapeRegExp(searchTerm)})`, 'gi');
                        highlightedName = highlightedName.replace(regex, '<mark>$1</mark>');
                    }

                    return `
                        <div class="form-check mb-2 p-2 rounded" style="background: #f8f9fc;">
                            <input class="form-check-input" type="checkbox" value="${subject.subjectId}" 
                                   id="subject_${subject.subjectId}" name="subjectIds">
                            <label class="form-check-label w-100" for="subject_${subject.subjectId}">
                                <div class="d-flex justify-content-between align-items-start">
                                    <div>
                                        <strong>${highlightedName}</strong>
                                        ${subject.subjectCode ? `<div class="text-muted small">${escapeHtml(subject.subjectCode)}</div>` : ''}
                                    </div>
                                    <div class="form-check-input-wrapper"></div>
                                </div>
                            </label>
                        </div>
                    `;
                }).join('');

            subjectsList.innerHTML = subjectCheckboxes;

            // Show count
            const countText = searchTerm ?
                `Found ${availableSubjects.length} subject${availableSubjects.length !== 1 ? 's' : ''} matching "${searchTerm}"` :
                `${availableSubjects.length} subject${availableSubjects.length !== 1 ? 's' : ''} available`;

            const countDiv = `<div class="text-muted text-center small py-2 border-bottom mb-2">${countText}</div>`;
            subjectsList.innerHTML = countDiv + subjectsList.innerHTML;

        } else {
            const emptyMessage = searchTerm ?
                `No subjects found matching "<strong>${escapeHtml(searchTerm)}</strong>". Try a different search term.` :
                allSubjects.length === 0 ?
                    'No subjects available in the database. Please contact admin to add subjects.' :
                    'All subjects are already assigned to this semester';

            subjectsList.innerHTML = `
                <div class="text-muted text-center py-4">
                    <i class="fas fa-search me-2"></i>
                    ${emptyMessage}
                </div>
            `;
        }

    } catch (error) {
        console.error('Error searching subjects:', error);
        subjectsList.innerHTML = `
            <div class="text-danger text-center py-3">
                <i class="fas fa-exclamation-triangle me-2"></i>
                Error searching subjects. Please try again.
            </div>
        `;
    }
}

// Helper function to escape special regex characters
function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

async function handleAddSemester(event) {
    event.preventDefault();

    const form = event.target;
    const submitBtn = document.getElementById('submitSemesterBtn');
    const originalText = submitBtn.innerHTML;

    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }

    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Adding...';

        const formData = new FormData(form);
        const payload = {
            programmeOfferedId: parseInt(formData.get('programmeOfferedId')),
            semesterNumber: parseInt(formData.get('semesterNumber')),
            semesterName: formData.get('semesterName') || null
        };

        await axios.post('/semesters/data', payload, {
            headers: { 'Content-Type': 'application/json' }
        });

        showToast('success', 'Semester added successfully!');

        // Properly hide modal
        const modalEl = document.getElementById('addSemesterModal');
        let modal = bootstrap.Modal.getInstance(modalEl);
        if (!modal) {
            modal = new bootstrap.Modal(modalEl);
        }
        modal.hide();

        // Wait for modal to completely hide before reloading
        modalEl.addEventListener('hidden.bs.modal', function onHidden() {
            modalEl.removeEventListener('hidden.bs.modal', onHidden);
            loadProgrammeSemesters();
        }, { once: true });

    } catch (error) {
        console.error('Error adding semester:', error);
        handleApiError(error, form);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

async function handleAddSubjects(event) {
    event.preventDefault();

    const form = event.target;
    const submitBtn = document.getElementById('submitSubjectsBtn');
    const originalText = submitBtn.innerHTML;

    const selectedSubjects = Array.from(document.querySelectorAll('input[name="subjectIds"]:checked'))
        .map(checkbox => parseInt(checkbox.value));

    if (selectedSubjects.length === 0) {
        showToast('error', 'Please select at least one subject.');
        return;
    }

    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Adding...';

        const payload = {
            semesterId: parseInt(document.getElementById('modalSemesterId').value),
            subjectIds: selectedSubjects
        };

        await axios.post('/subject-assignments/data', payload, {
            headers: { 'Content-Type': 'application/json' }
        });

        const count = selectedSubjects.length;
        showToast('success', `${count} subject${count > 1 ? 's' : ''} added successfully!`);

        // Properly hide modal
        const modalEl = document.getElementById('addSubjectsModal');
        let modal = bootstrap.Modal.getInstance(modalEl);
        if (!modal) {
            modal = new bootstrap.Modal(modalEl);
        }
        modal.hide();

        // Wait for modal to hide before reloading subjects
        modalEl.addEventListener('hidden.bs.modal', function onHidden() {
            modalEl.removeEventListener('hidden.bs.modal', onHidden);
            loadSemesterSubjects(currentSemesterForSubjects);
        }, { once: true });

    } catch (error) {
        console.error('Error adding subjects:', error);
        const errorMessage = error.response?.data?.message || 'Failed to add subjects. Please try again.';
        showToast('error', errorMessage);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

function showDeleteSemesterModal(semesterId, semesterTitle) {
    semesterToDelete = semesterId;

    document.getElementById('semesterToDelete').textContent = semesterTitle;

    const modalEl = document.getElementById('deleteSemesterModal');
    let modal = bootstrap.Modal.getInstance(modalEl);
    if (!modal) {
        modal = new bootstrap.Modal(modalEl);
    }
    modal.show();
}

async function handleDeleteSemester() {
    if (!semesterToDelete) return;

    const confirmBtn = document.getElementById('confirmDeleteSemesterBtn');
    const originalText = confirmBtn.innerHTML;

    try {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Deleting...';

        await axios.delete(`/semesters/data/${semesterToDelete}`);

        showToast('success', 'Semester deleted successfully!');

        // Properly hide modal
        const modalEl = document.getElementById('deleteSemesterModal');
        let modal = bootstrap.Modal.getInstance(modalEl);
        if (!modal) {
            modal = new bootstrap.Modal(modalEl);
        }
        modal.hide();

        // Wait for modal to hide before reloading
        modalEl.addEventListener('hidden.bs.modal', function onHidden() {
            modalEl.removeEventListener('hidden.bs.modal', onHidden);
            loadProgrammeSemesters();
        }, { once: true });

    } catch (error) {
        console.error('Error deleting semester:', error);
        const errorMessage = error.response?.data?.message || 'Failed to delete semester. Please try again.';
        showToast('error', errorMessage);
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = originalText;
        semesterToDelete = null;
    }
}

// Utility Functions
function showLoadingState() {
    const loadingState = document.getElementById('loadingState');
    if (loadingState) {
        loadingState.style.display = 'block';
    }
}

function hideLoadingState() {
    const loadingState = document.getElementById('loadingState');
    if (loadingState) {
        loadingState.style.display = 'none';
    }
}

function showEmptyState() {
    const container = document.getElementById('semestersContainer');
    container.innerHTML = `
        <div class="text-center py-5">
            <div class="text-muted">
                <i class="fas fa-calendar-plus fa-3x mb-3 d-block"></i>
                <h5>No Semesters Created</h5>
                <p>Click "Add Semester" to create the first semester for this programme.</p>
            </div>
        </div>
    `;
}

function showErrorState(message) {
    const container = document.getElementById('semestersContainer');
    container.innerHTML = `
        <div class="text-center py-5">
            <div class="text-danger">
                <i class="fas fa-exclamation-triangle fa-3x mb-3 d-block"></i>
                <h5>Error Loading Data</h5>
                <p>${message}</p>
            </div>
        </div>
    `;
}

function resetAddSemesterForm() {
    const form = document.getElementById('addSemesterForm');
    if (form) {
        form.reset();
        form.classList.remove('was-validated');
        form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    }
}

function resetAddSubjectsForm() {
    const form = document.getElementById('addSubjectsForm');
    if (form) {
        form.reset();
    }
    currentSemesterForSubjects = null;
}

function cleanupModalBackdrop() {
    // Remove any stuck modal backdrops
    setTimeout(() => {
        document.querySelectorAll('.modal-backdrop').forEach(backdrop => backdrop.remove());
        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
        document.body.style.paddingRight = '';
    }, 300);
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
        showToast('error', 'A semester with this number already exists for this programme.');
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

// Global function exports for onclick handlers
window.showAddSubjectsModal = showAddSubjectsModal;
window.showDeleteSemesterModal = showDeleteSemesterModal;
window.confirmRemoveSubject = confirmRemoveSubject;
