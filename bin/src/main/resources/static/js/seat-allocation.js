/**
 * Seat Allocation Management JavaScript
 * Handles seat capacity updates and success modal display
 */

/**
 * Updates seat capacity for a specific programme
 * @param {number} programmeId - The ID of the programme to update
 */
function updateSeats(programmeId) {
    const inputField = document.getElementById('totalSeats-' + programmeId);
    const hiddenField = document.getElementById('hiddenSeats-' + programmeId);
    const form = hiddenField.closest('form');
    
    // Validate input
    if (!inputField.value || inputField.value < 0) {
        alert('Please enter a valid seat count (0 or greater).');
        return;
    }
    
    // Update hidden field with current input value
    hiddenField.value = inputField.value;
    
    // Submit the form
    form.submit();
}

/**
 * Shows success modal if success parameter is present in URL
 */
function showSuccessModal() {
    const successModal = document.getElementById('successModal');
    if (successModal) {
        const modal = new bootstrap.Modal(successModal);
        modal.show();
    }
}

/**
 * Initialize page functionality when DOM is loaded
 */
function initializePage() {
    // Show success modal if present
    showSuccessModal();
    
    // Add enter key support for input fields
    const seatInputs = document.querySelectorAll('input[id^="totalSeats-"]');
    seatInputs.forEach(input => {
        input.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                const programmeId = this.id.replace('totalSeats-', '');
                updateSeats(programmeId);
            }
        });
    });
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', initializePage);
