let rejectionModalInstance; 
let confirmationModalInstance;

/**
 * Update the number displayed on status cards (Accepted, Rejected, Pending, etc.)
 */
function updateStatCard(direction, status) {
    const statEl = document.querySelector(`[data-stat="${status.toLowerCase()}"]`);
    if (statEl) {
        let count = parseInt(statEl.textContent, 10);
        statEl.textContent = direction === 'increment' ? count + 1 : Math.max(0, count - 1);
    }
}

/**
 * Called when Accept/Reject/Correction buttons are clicked.
 * Decides which modal to open based on status.
 */
function updateStatus(button, status) {
    const instituteId = button.getAttribute('data-id');
    const instituteName = button.closest('tr').querySelector('td:nth-child(2)').textContent.trim();
    const normalizedStatus = status.toUpperCase(); 

    if (normalizedStatus === 'ACCEPTED') {
        showConfirmationModal(instituteId, normalizedStatus, instituteName);
    } else if (normalizedStatus === 'REJECTED' || normalizedStatus === 'CORRECTION_REQUIRED') {
        showRejectionModal(instituteId, instituteName, normalizedStatus); 
    }
}

/**
 * Show confirmation modal when accepting an institute.
 */
function showConfirmationModal(instituteId, status, instituteName) {
    const modalEl = document.getElementById('confirmationModal');
    const inputId = document.getElementById('confirmationInstituteId');
    const inputStatus = document.getElementById('confirmationStatus');
    const nameEl = document.getElementById('confirmationInstituteName');
    const actionEl = document.getElementById('confirmationAction'); 

    if (!modalEl || !inputId || !inputStatus || !nameEl || !actionEl) {
        console.error('Confirmation modal elements missing');
        showToast('Confirmation dialog is not available. Please refresh the page and try again.', 'danger');
        return;
    }

    inputId.value = instituteId;
    inputStatus.value = status;
    nameEl.textContent = instituteName;
    actionEl.textContent = status.toLowerCase();
    new bootstrap.Modal(modalEl).show();
}

/**
 * Show rejection/correction modal.
 * Adjusts title, placeholder and buttons depending on action.
 */
function showRejectionModal(instituteId, instituteName, statusToSet) {
    const modalEl = document.getElementById('rejectionModal');
    const inputId = document.getElementById('rejectionInstituteId');
    const visibleNameEl = document.getElementById('rejectionInstituteName'); 
    const rejectionReasonText = document.getElementById('rejectionReasonText');
    const submitRejectionBtn = document.getElementById('submitRejectionBtn'); 
    const sendForCorrectionBtn = document.getElementById('sendForCorrectionBtn'); 

    if (!modalEl || !inputId || !visibleNameEl || !rejectionReasonText || !submitRejectionBtn || !sendForCorrectionBtn) {
        console.error('Rejection modal elements missing');
        showToast('Rejection dialog is not available. Please refresh the page and try again.', 'danger');
        return;
    }

    inputId.value = instituteId;
    visibleNameEl.textContent = instituteName;
    rejectionReasonText.value = '';

    submitRejectionBtn.style.display = 'inline-block';
    sendForCorrectionBtn.style.display = 'inline-block';

    if (statusToSet === 'REJECTED') {
        rejectionReasonText.placeholder = 'Enter detailed reason for permanent rejection...';
        rejectionModalLabel.innerHTML = '<i class="fas fa-times-circle me-2"></i>Reason for Rejection'; 
    } else if (statusToSet === 'CORRECTION_REQUIRED') {
        rejectionReasonText.placeholder = 'Describe specific corrections required by the institute...';
        rejectionModalLabel.innerHTML = '<i class="fas fa-edit me-2"></i>Reason for Correction';
    } else {
        rejectionReasonText.placeholder = 'Please provide a reason for action...';
        rejectionModalLabel.innerHTML = '<i class="fas fa-exclamation-triangle me-2 text-warning"></i>Institute Application Action'; 
    }

    new bootstrap.Modal(modalEl).show();
}

/**
 * Handles actual status update via AJAX.
 * Updates:
 *   - DB (via backend call)
 *   - UI table row
 *   - Status count cards
 */
function performAjaxUpdate(instituteId, status, reason = null, button) {
    if (!button) {
        button = document.querySelector(`.action-btn[data-id='${instituteId}']`);
        if (!button) {
            window.location.reload();
            return;
        }
    }

    const normalized = status.toUpperCase(); 

    axios.post(`/admin/institute/${instituteId}/status`, { status: normalized, reason })
        .then(({ data }) => {
            showToast(data.message, 'success');

            const row = button.closest('tr');
            updateStatCard('decrement', 'pending'); 
            updateStatCard('increment', normalized.toLowerCase()); 

            if (row) {
                const statusCell = row.querySelector('.status-cell');
                const actionCell = row.querySelector('.action-cell');
                let badgeClass, text;

                if (normalized === 'ACCEPTED') {
                    badgeClass = 'text-bg-success';
                    text = 'ACCEPTED';
                } else if (normalized === 'REJECTED') {
                    badgeClass = 'text-bg-danger';
                    text = 'REJECTED';
                } else if (normalized === 'CORRECTION_REQUIRED') {
                    badgeClass = 'text-bg-warning';
                    text = 'CORRECTION REQUIRED';
                } else {
                    badgeClass = 'text-bg-info';
                    text = 'PENDING';
                }

                statusCell.innerHTML = `<span class="badge rounded-pill ${badgeClass}">${text}</span>`;
                actionCell.innerHTML = '<span class="text-muted">—</span>'; 
            } else {
                window.location.reload(); 
            }
        })
        .catch(err => {
            showToast(err.response?.data?.message || 'Update failed. Please try again.', 'danger');
        });
}

/**
 * Show "Preview" modal with institute details loaded via AJAX.
 */
function previewInstitute(button) {
    const instituteId = button.getAttribute('data-id');
    const modalEl = document.getElementById('instituteModal');
    if (!modalEl) {
        console.warn('Institute preview modal not found.');
        return;
    }
    const modal = new bootstrap.Modal(modalEl);

    axios.get(`/admin/institute/${instituteId}/preview`)
        .then(({ data }) => {
            const inst = data.institute || {};
            const addr = inst.addressDTO || {};
            const setText = (id, txt) => {
                const el = document.getElementById(id);
                if (el) el.textContent = txt || 'N/A';
            };

            setText('modalInstituteName', inst.instituteName);
            setText('modalAISHEId', inst.AISHEId);
            setText('modalUniversityName', inst.universityName);
            setText('modalYearEstablished', inst.yearEstablished);
            setText('modalBorderDistrictArea', inst.borderDistrictArea);
            setText('modalManagementTypeName', data.managementTypeName);
            setText('modalAffiliationTypeName', data.affiliationTypeName);
            setText('modalHead', inst.institutionHeadDetails);
            setText('modalContact', inst.institutionOfficialContactNumber);
            setText('modalEmail', inst.institutionOfficialEmailId);
            setText('modalAddress1', addr.addressLine1);
            setText('modalAddress2', addr.addressLine2);
            setText('modalPinCode', addr.pincode);
            setText('modalStateName', data.stateName);
            setText('modalDistrictName', data.districtName);
            setText('modalBlockName', data.blockName);

            const link = document.getElementById('modalWebsite');
            if (link) {
                link.textContent = inst.institutionWebsite || 'Not Available';
                link.href = inst.institutionWebsite ? `//${inst.institutionWebsite.replace(/^https?:\/\//, '')}` : '#';
            }

            modal.show();
        })
        .catch(() => showToast('Failed to load preview details.', 'danger'));
}

/**
 * Initialize DataTable for institutes list.
 */
function initInstituteManagement() {
    const tbl = document.getElementById('institutesTable');
    if (tbl) {
        if ($.fn.dataTable.isDataTable('#institutesTable')) {
            $('#institutesTable').DataTable();
        } else {
            $('#institutesTable').DataTable({
                pageLength: 10,
                lengthMenu: [[10,25,50,-1],[10,25,50,'All']],
                language: {
                    emptyTable: 'No institute applications found.',
                    zeroRecords: 'No matching records found.'
                },
                order: [[0,'asc']],
                columnDefs: [{ orderable: false, targets: -1 }]
            });
        }
    }
}

/**
 * Main initializer - attaches event handlers once DOM is ready.
 */
document.addEventListener('DOMContentLoaded', () => {
    initInstituteManagement();

    // Attach events for rejection form actions
    const rejectionForm = document.getElementById('rejectionForm');
    if (rejectionForm) {
        const submitRejectionBtn = document.getElementById('submitRejectionBtn');
        if (submitRejectionBtn) {
            submitRejectionBtn.addEventListener('click', (e) => {
                e.preventDefault(); 
                const instituteId = document.getElementById('rejectionInstituteId').value;
                const reason = document.getElementById('rejectionReasonText').value.trim();
                if (!reason) {
                    showToast('Please provide a reason for rejection.', 'warning');
                    return;
                }
                performAjaxUpdate(instituteId, 'REJECTED', reason); 
                bootstrap.Modal.getInstance(document.getElementById('rejectionModal')).hide();
            });
        }
        
        const sendForCorrectionBtn = document.getElementById('sendForCorrectionBtn');
        if (sendForCorrectionBtn) {
            sendForCorrectionBtn.addEventListener('click', (e) => {
                e.preventDefault(); 
                const instituteId = document.getElementById('rejectionInstituteId').value;
                const reason = document.getElementById('rejectionReasonText').value.trim();
                if (!reason) {
                    showToast('Please provide a reason to send for correction.', 'warning');
                    return;
                }
                performAjaxUpdate(instituteId, 'CORRECTION_REQUIRED', reason); 
                bootstrap.Modal.getInstance(document.getElementById('rejectionModal')).hide();
            });
        }
    }

    // Confirm button in Accept modal
    const confirmAcceptRejectBtn = document.getElementById('confirmAcceptReject');
    if (confirmAcceptRejectBtn) {
        confirmAcceptRejectBtn.addEventListener('click', () => {
            const instituteId = document.getElementById('confirmationInstituteId').value;
            const status = document.getElementById('confirmationStatus').value; 
            performAjaxUpdate(instituteId, status);
            bootstrap.Modal.getInstance(document.getElementById('confirmationModal')).hide();
        });
    }

    // Initialize Bootstrap tooltips everywhere
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    });
});

window.updateStatus = updateStatus;
window.previewInstitute = previewInstitute;

/**
 * Bootstrap Toast manager to display consistent success/error/info messages.
 */
function showToast(message, type = 'info') {
    const toastContainer = document.querySelector('.toast-container#toastContainer'); 
    if (!toastContainer) {
        console.warn('Toast container not found. Cannot display toast:', message);
        return;
    }

    const statusToastEl = document.getElementById('statusToast'); 
    if (statusToastEl) {
        const toastBody = statusToastEl.querySelector('.toast-body');
        const toastHeaderStrong = statusToastEl.querySelector('.toast-header strong.me-auto');

        if (toastBody) toastBody.textContent = message;
        if (toastHeaderStrong) toastHeaderStrong.textContent = type.charAt(0).toUpperCase() + type.slice(1);

        statusToastEl.classList.remove('bg-info', 'bg-success', 'bg-warning', 'bg-danger', 'text-white'); 
        
        let headerIconClass = '';
        switch(type) {
            case 'success': headerIconClass = 'fas fa-check-circle text-success'; break;
            case 'danger': headerIconClass = 'fas fa-times-circle text-danger'; break;
            case 'warning': headerIconClass = 'fas fa-exclamation-triangle text-warning'; break;
            case 'info':
            default: headerIconClass = 'fas fa-info-circle text-info'; break;
        }

        const toastHeaderIcon = statusToastEl.querySelector('.toast-header i.fa-bell');
        if (toastHeaderIcon) {
            toastHeaderIcon.className = headerIconClass + ' me-2';
        }

        statusToastEl.classList.add(`bg-${type}`);
        if (type !== 'info') {
            statusToastEl.classList.add('text-white');
        } else {
            statusToastEl.classList.remove('text-white');
        }
        
        const toast = new bootstrap.Toast(statusToastEl);
        toast.show();
        
        // Reset toast appearance to default after hiding
        statusToastEl.addEventListener('hidden.bs.toast', () => {
            if (toastHeaderIcon) toastHeaderIcon.className = 'fas fa-bell me-2';
            if (toastHeaderStrong) toastHeaderStrong.textContent = 'Notification';
            statusToastEl.classList.remove('bg-info', 'bg-success', 'bg-warning', 'bg-danger', 'text-white');
            statusToastEl.classList.add('bg-light');
        }, { once: true });
    } else {
        console.warn('Predefined statusToast not found. Displaying dynamic toast:', message);
    }
}
