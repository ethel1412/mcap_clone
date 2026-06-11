// Institute Registration Fee Management - JavaScript

let currentEditFeeId = null;
let deleteTargetFeeId = null;

const casteColorMap = {
    'GENERAL': 'bg-primary',
    'OBC': 'bg-warning',
    'SC': 'bg-success',
    'ST': 'bg-danger',
    'EWS': 'bg-info'
};

// Get CSRF token from meta tags
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.content;
}

function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
}

// Build CSRF headers
function getCsrfHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };
    
    const token = getCsrfToken();
    const header = getCsrfHeader();
    
    if (token && header) {
        headers[header] = token;
        console.log('Adding CSRF Header:', header, '=', token);
    } else {
        console.warn('CSRF token or header not found!');
    }
    
    return headers;
}

// Page initialization
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page loaded');
    console.log('CSRF Token:', getCsrfToken());
    console.log('CSRF Header:', getCsrfHeader());
    
    loadFees();

    // Modal event listeners
    document.getElementById('addFeeModal').addEventListener('hidden.bs.modal', function() {
        resetForm();
    });

    // Button event listeners
    document.getElementById('saveFeeBtn').addEventListener('click', function() {
        console.log('Save button clicked');
        saveFee();
    });

    document.getElementById('confirmDeleteBtn').addEventListener('click', function() {
        console.log('Confirm delete button clicked');
        confirmDelete();
    });
});

// Load all fees
function loadFees() {
    console.log('Loading fees...');
    fetch('/institute-registration-fee/list', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Fees loaded:', data);
        if (data.success) {
            populateFeesTable(data.data);
            populateSummaryCards(data.data);
        } else {
            showAlert('Error: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error loading fees:', error);
        showAlert('Error loading fees: ' + error, 'danger');
    });
}

// Populate fees table
function populateFeesTable(fees) {
    const tbody = document.getElementById('feesTableBody');
    tbody.replaceChildren();
    
    if (fees.length === 0) {
        document.getElementById('feesTable').style.display = 'none';
        document.getElementById('emptyState').style.display = 'block';
        return;
    } else {
        document.getElementById('feesTable').style.display = 'table';
        document.getElementById('emptyState').style.display = 'none';
    }

    fees.forEach(function(fee) {
        const casteColorClass = casteColorMap[fee.caste] || 'bg-secondary';

        const row = document.createElement('tr');

        // Caste badge
        const td1 = document.createElement('td');
        const badge = document.createElement('span');
        badge.className = `badge ${casteColorClass}`;
        badge.textContent = fee.caste;
        td1.appendChild(badge);

        // Amount
        const td2 = document.createElement('td');
        td2.textContent = `₹ ${parseFloat(fee.amount).toFixed(2)}`;

        // Date
        const td3 = document.createElement('td');
        td3.textContent = new Date(fee.updatedAt).toLocaleDateString('en-IN');

        // Actions
        const td4 = document.createElement('td');
        const group = document.createElement('div');
        group.className = 'btn-group btn-group-sm';

        const editBtn = document.createElement('button');
        editBtn.type = "button";
        editBtn.className = "btn btn-info";
        editBtn.addEventListener("click", () => editFee(fee.feeId, fee.caste, fee.amount));

        const editIcon = document.createElement("i");
        editIcon.className = "fa-solid fa-edit";
        editBtn.appendChild(editIcon);

        const deleteBtn = document.createElement('button');
        deleteBtn.type = "button";
        deleteBtn.className = "btn btn-danger";
        deleteBtn.addEventListener("click", () => showDeleteConfirm(fee.feeId, fee.caste));

        const deleteIcon = document.createElement("i");
        deleteIcon.className = "fa-solid fa-trash";
        deleteBtn.appendChild(deleteIcon);

        group.append(editBtn, deleteBtn);
        td4.appendChild(group);

        row.append(td1, td2, td3, td4);
        tbody.appendChild(row);
    });
}

// Populate summary cards
function populateSummaryCards(fees) {
    const container = document.getElementById('summaryCards');
    container.replaceChildren();

    if (fees.length === 0) return;

    fees.forEach(function(fee) {
        const casteColorClass = casteColorMap[fee.caste] || 'bg-secondary';

        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4 col-xl-3';

        const card = document.createElement('div');
        card.className = 'card border-0 shadow-sm';

        const body = document.createElement('div');
        body.className = 'card-body';

        const flex = document.createElement('div');
        flex.className = 'd-flex justify-content-between align-items-center';

        const inner = document.createElement('div');

        const badge = document.createElement('span');
        badge.className = `badge ${casteColorClass}`;
        badge.textContent = fee.caste;

        const title = document.createElement('h6');
        title.className = 'card-title mt-2 mb-0';
        title.textContent = `₹ ${parseFloat(fee.amount).toFixed(2)}`;

        inner.append(badge, title);
        flex.appendChild(inner);
        body.appendChild(flex);
        card.appendChild(body);
        col.appendChild(card);

        container.appendChild(col);
    });
}

// Save fee (create or update)
function saveFee() {
    console.log('saveFee function called');
    const caste = document.getElementById('caste').value;
    const amount = document.getElementById('amount').value;
    const feeId = document.getElementById('feeId').value;

    console.log('Caste:', caste, 'Amount:', amount, 'FeeId:', feeId);

    // Validation
    if (!caste) {
        showFieldError('casteError', 'Please select a caste category');
        return;
    }
    if (!amount || parseFloat(amount) <= 0) {
        showFieldError('amountError', 'Please enter a valid amount');
        return;
    }

    const feeData = {
        caste: caste,
        amount: parseFloat(amount)
    };

    console.log('Fee data to send:', feeData);

    let url = '/institute-registration-fee/save';
    let method = 'POST';

    if (feeId) {
        url = '/institute-registration-fee/update/' + feeId;
        method = 'PUT';
    }

    fetch(url, {
        method: method,
        headers: getCsrfHeaders(),
        body: JSON.stringify(feeData),
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Success response:', data);
        if (data.success) {
            showAlert(data.message, 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('addFeeModal'));
            if (modal) {
                modal.hide();
            }
            resetForm();
            loadFees();
        } else {
            showAlert('Error: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error saving fee:', error);
        showAlert('Error saving fee: ' + error, 'danger');
    });
}

// Edit fee
function editFee(feeId, caste, amount) {
    console.log('Editing fee:', feeId, caste, amount);
    currentEditFeeId = feeId;
    document.getElementById('feeId').value = feeId;
    document.getElementById('caste').value = caste;
    document.getElementById('caste').disabled = true;
    document.getElementById('amount').value = amount;
    document.getElementById('modalTitle').textContent = 'Edit Registration Fee';
    clearFieldErrors();
    const modal = new bootstrap.Modal(document.getElementById('addFeeModal'));
    modal.show();
}

// Show delete confirmation modal
function showDeleteConfirm(feeId, caste) {
    console.log('Showing delete confirm for:', feeId, caste);
    deleteTargetFeeId = feeId;
    document.getElementById('deleteCasteName').textContent = caste;
    const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    modal.show();
}

// Confirm delete
function confirmDelete() {
    console.log('Confirming delete for feeId:', deleteTargetFeeId);
    fetch('/institute-registration-fee/delete/' + deleteTargetFeeId, {
        method: 'DELETE',
        headers: getCsrfHeaders(),
        credentials: 'include'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Delete response:', data);
        if (data.success) {
            showAlert('Fee deleted successfully', 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'));
            if (modal) {
                modal.hide();
            }
            loadFees();
        } else {
            showAlert('Error: ' + data.message, 'danger');
        }
    })
    .catch(error => {
        console.error('Error deleting fee:', error);
        showAlert('Error deleting fee: ' + error, 'danger');
    });
}

// Reset form
function resetForm() {
    console.log('Resetting form');
    document.getElementById('feeForm').reset();
    document.getElementById('feeId').value = '';
    document.getElementById('caste').disabled = false;
    document.getElementById('modalTitle').textContent = 'Add Registration Fee';
    clearFieldErrors();
    currentEditFeeId = null;
}

// Show alert message
function showAlert(message, type) {
    const container = document.getElementById('alertContainer');
    container.replaceChildren();

    const alertDiv = document.createElement("div");
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.setAttribute("role", "alert");

    const icon = document.createElement("i");
    icon.className = `fa-solid fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2`;

    const text = document.createTextNode(message);

    const closeBtn = document.createElement("button");
    closeBtn.type = "button";
    closeBtn.className = "btn-close";
    closeBtn.setAttribute("data-bs-dismiss", "alert");

    alertDiv.append(icon, text, closeBtn);
    container.appendChild(alertDiv);

    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Show field error
function showFieldError(fieldId, message) {
    const errorElement = document.getElementById(fieldId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

// Clear all field errors
function clearFieldErrors() {
    document.querySelectorAll('.error-text').forEach(el => {
        el.style.display = 'none';
        el.textContent = '';
    });
}