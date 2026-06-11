
function showToast(message, type = 'success') {
    const toastEl = document.getElementById('statusToast');
    if (!toastEl) {
        console.warn("Toast element #statusToast not found.");
        return;
    }
    const toastBody = toastEl.querySelector('.toast-body');
    const toastHeader = toastEl.querySelector('.toast-header');

    if (!toastBody || !toastHeader) {
        console.warn("Toast body or header not found within #statusToast.");
        return;
    }

    toastBody.textContent = message;

    // Reset classes to avoid stacking
    toastEl.className = 'toast align-items-center border-0';
    toastHeader.className = 'toast-header';

    const bootstrapClass = type === 'success' ? 'bg-success' : 'bg-danger';
    toastEl.classList.add(bootstrapClass, 'text-white');
    toastHeader.classList.add(bootstrapClass, 'text-white'); // Apply to header as well for consistency

    const toast = new bootstrap.Toast(toastEl);
    toast.show();
}


function formatDateTimeLocal(dateTimeString) {
    if (!dateTimeString || typeof dateTimeString !== 'string') return '';
    // Take the first 16 characters (YYYY-MM-DDTHH:MM)
    return dateTimeString.substring(0, 16);
}


function addSingleClickMultiselect(selectElement) {
    // Remove any existing click listeners to prevent duplicates
    selectElement.removeEventListener('mousedown', preventDefaultMultiselect);
    selectElement.removeEventListener('click', toggleOptionSelected);

    // Prevent default behavior on mousedown to stop native multi-select behavior
    selectElement.addEventListener('mousedown', preventDefaultMultiselect);
    // Use a click listener to toggle selection
    selectElement.addEventListener('click', toggleOptionSelected);
}


function preventDefaultMultiselect(e) {
    if (e.target.tagName === 'OPTION') {
        e.preventDefault();
    }
}


function toggleOptionSelected(e) {
    if (e.target.tagName === 'OPTION') {
        e.target.selected = !e.target.selected;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('input[type="datetime-local"]').forEach(dateInput => {
        dateInput.addEventListener('input', function(e) {
            // A small timeout helps with browser-specific date picker quirks
            setTimeout(() => {
                this.blur();
            }, 100);
        });
    });
});