document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('personal-details-form');
    if (form) {
        form.addEventListener('submit', function(event) {
            event.preventDefault(); // Stop the default browser submission

            const formData = new FormData(form);
            const saveButton = form.querySelector('button[type="submit"]');
            const originalButtonHtml = saveButton.innerHTML;

            // Disable button and show spinner
            saveButton.disabled = true;
            saveButton.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...`;

            axios.post(form.action, formData)
                .then(function(response) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Success!',
                        text: 'Your details have been updated successfully.',
                        timer: 2000,
                        showConfirmButton: false
                    });
                })
                .catch(function(error) {
                    console.error('Error updating details:', error);
                    Swal.fire({
                        icon: 'error',
                        title: 'Oops...',
                        text: 'Something went wrong! Please try again.',
                    });
                })
                .finally(function() {
                    // Re-enable button and restore original text
                    saveButton.disabled = false;
                    saveButton.innerHTML = originalButtonHtml;
                });
        });
    }
});