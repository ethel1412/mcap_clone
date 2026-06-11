(function() {
    'use strict';

    // 1. GET VALUES FROM HIDDEN INPUTS
    const programmeOfferedId = document.getElementById('programmeOfferedId').value;

    // CHANGED: Pull the string code instead of the integer ID
    const admissionWindowCode = document.getElementById('admissionWindowCode').value;

    // DOM Elements
    const applicantTypeSelect = document.getElementById('applicantType');
    const reservationTypeSelect = document.getElementById('reservationType');
    const reservedPercentageInput = document.getElementById('reservedPercentage');
    const categoryGroup = document.getElementById('categoryGroup');
    const categorySelect = document.getElementById('categoryCode');
    const examSourceGroup = document.getElementById('examSourceGroup');
    const examSourceSelect = document.getElementById('examSource');
    const reservationForm = document.getElementById('reservationForm');

    // Configure Axios defaults
    axios.defaults.headers.common['Content-Type'] = 'application/json';

    // Add CSRF token to axios requests
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (token && header) {
        axios.defaults.headers.common[header] = token;
    }

    // Show/hide fields based on reservation type
    reservationTypeSelect.addEventListener('change', function() {
        const selectedType = this.value;

        // Reset dependent inputs
        categoryGroup.classList.add('d-none');
        categorySelect.required = false;
        categorySelect.value = '';

        examSourceGroup.classList.add('d-none');
        examSourceSelect.value = '';

        if (selectedType === 'EXAM_QUALIFIED') {
            categoryGroup.classList.remove('d-none');
            categorySelect.required = true;
            examSourceGroup.classList.remove('d-none');
        }
    });

    // Handle form submission
    reservationForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const submitBtn = this.querySelector('button[type="submit"]');
        
        // preserve original content safely
		const originalContent = submitBtn.cloneNode(true);
		
		submitBtn.disabled = true;
		submitBtn.replaceChildren();
		
		const spinner = document.createElement("span");
		spinner.className = "spinner-border spinner-border-sm me-1";
		
		submitBtn.appendChild(spinner);
		submitBtn.appendChild(document.createTextNode("Adding..."));
        const applicantType = applicantTypeSelect.value;
        const reservationType = reservationTypeSelect.value;
        const reservedPercentage = parseFloat(reservedPercentageInput.value);

        // Validation: applicant type
        if (!applicantType) {
            showError('Please select an applicant type');
            submitBtn.disabled = false;
            submitBtn.replaceChildren(...originalContent.childNodes);
            return;
        }

        // Validation: reservation type
        if (!reservationType) {
            showError('Please select a reservation type');
            submitBtn.disabled = false;
            submitBtn.replaceChildren(...originalContent.childNodes);
            return;
        }

        // Validation: percentage
        if (isNaN(reservedPercentage) || reservedPercentage <= 0 || reservedPercentage > 100) {
            showError('Please enter a valid reserved percentage between 0 and 100');
            submitBtn.disabled = false;
            submitBtn.replaceChildren(...originalContent.childNodes);
            return;
        }

        // Build request body
        // CHANGED: Use admissionWindowCode to match the updated backend DTO requirement
        const requestBody = {
            programmeOfferedId: parseInt(programmeOfferedId),
            admissionWindowCode: admissionWindowCode,
            applicantType: applicantType,
            reservationType: reservationType,
            reservedPercentage: reservedPercentage
        };

        // Add categoryCode and examSource for EXAM_QUALIFIED
        if (reservationType === 'EXAM_QUALIFIED') {
            const categoryCode = categorySelect.value;
            if (!categoryCode) {
                showError('Please select a community category');
                submitBtn.disabled = false;
                submitBtn.replaceChildren(...originalContent.childNodes);
                return;
            }
            requestBody.categoryCode = categoryCode;

            const examSource = examSourceSelect.value;
            if (!examSource) {
                showError('Please select an exam');
                submitBtn.disabled = false;
                submitBtn.replaceChildren(...originalContent.childNodes);
                return;
            }
            requestBody.examSource = examSource;
        }

        console.log('Sending request:', JSON.stringify(requestBody));

        try {
            const response = await axios.post('/seat-reservations/create', requestBody);

            showSuccess('Reservation created successfully!');
            reservationForm.reset();
            categoryGroup.classList.add('d-none');
            examSourceGroup.classList.add('d-none');

            setTimeout(() => location.reload(), 1500);

        } catch (error) {
            submitBtn.disabled = false;
            submitBtn.replaceChildren(...originalContent.childNodes);

            if (error.response) {
                let errorMessage = 'Failed to create reservation';

                if (error.response.data) {
                    if (typeof error.response.data === 'string') {
                        errorMessage = error.response.data;
                    } else if (error.response.data.message) {
                        errorMessage = error.response.data.message;
                    } else if (error.response.data.error) {
                        errorMessage = error.response.data.error;
                    }
                }

                showError(errorMessage);
                console.error('Server error:', error.response);
            } else if (error.request) {
                showError('No response from server. Please check your connection.');
                console.error('No response:', error.request);
            } else {
                showError('Error: ' + error.message);
                console.error('Error:', error);
            }
        }
    });

    // Event delegation for delete buttons
    document.addEventListener('click', function(e) {
        const deleteBtn = e.target.closest('.btn-delete');
        if (deleteBtn) {
            const reservationId = deleteBtn.dataset.reservationId;
            deleteReservation(reservationId);
        }
    });

    // Delete reservation function
    async function deleteReservation(reservationId) {
        if (!confirm('Are you sure you want to delete this reservation?')) {
            return;
        }

        try {
            const response = await axios.delete(
                `/seat-reservations/delete/${reservationId}`,
                {
                    params: { programmeOfferedId: programmeOfferedId }
                }
            );

            showSuccess(response.data.message || 'Reservation deleted successfully!');
            setTimeout(() => location.reload(), 1500);

        } catch (error) {
            if (error.response) {
                const errorMessage = error.response.data.message ||
                                     error.response.data ||
                                     'Failed to delete reservation';
                showError(errorMessage);
            } else {
                showError('Network error: ' + error.message);
            }
        }
    }

    // Toast notification functions
    function showSuccess(message) {
        const toastEl = document.getElementById('successToast');
        document.getElementById('toastMessage').textContent = message;
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
    }

    function showError(message) {
        const toastEl = document.getElementById('errorToast');
        document.getElementById('errorToastMessage').textContent = message;
        const toast = new bootstrap.Toast(toastEl);
        toast.show();
    }

})();