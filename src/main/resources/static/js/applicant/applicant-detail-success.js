import { showSuccess, showError } from './utils.js';

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('personal-details-form');

    if (form) {
        form.addEventListener('submit', function(event) {
            event.preventDefault();
            const formData = new FormData(form);
            const saveButton = form.querySelector('button[type="submit"]');
            const originalContent = Array.from(saveButton.childNodes).map(n => n.cloneNode(true));
            saveButton.disabled = true;
            saveButton.replaceChildren();

            const spinner = document.createElement('span');
            spinner.className = 'spinner-border spinner-border-sm';
            spinner.setAttribute('role', 'status');
            spinner.setAttribute('aria-hidden', 'true');
            const text = document.createTextNode(' Saving...');

            saveButton.append(spinner, text);

            axios.post(form.action, formData)
                .then(function(response) {
                    // CSP FIX: Replaced Swal.fire with native showSuccess
                    showSuccess('Your details have been updated successfully.');
                })
                .catch(function(error) {
                    console.error('Error updating details:', error);
                    // CSP FIX: Replaced Swal.fire with native showError
                    showError('Something went wrong! Please try again.');
                })
                .finally(function() {
                    // Re-enable button and restore original content safely
                    saveButton.disabled = false;
                    saveButton.replaceChildren(...originalContent);
                });
        });
    }
});