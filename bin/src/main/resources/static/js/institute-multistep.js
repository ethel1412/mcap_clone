document.addEventListener('DOMContentLoaded', function () {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');
    const form = document.getElementById('instituteForm');
    const formSteps = Array.from(document.querySelectorAll('.form-step'));
    const stepIndicators = Array.from(document.querySelectorAll('.step-indicator'));
    
    const progressTrack = document.querySelector('.progress-track');
    const progressFill = document.querySelector('.progress-fill');

    let currentStep = 0;

    // Get instituteId for validation calls
    const instituteIdInput = document.querySelector('input[name="instituteId"]');
    const currentInstituteId = instituteIdInput && instituteIdInput.value ? parseInt(instituteIdInput.value, 10) : null;

    /**
     * Show validation error for a field
     */
    function showError(field, message) {
        field.classList.add('is-invalid');
        
        // Find or create error message div
        let errorDiv = field.parentElement.querySelector('.js-error-message');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback js-error-message';
            field.parentElement.appendChild(errorDiv);
        }
        
        // Hide any existing server-side error messages
        const serverErrorDiv = field.parentElement.querySelector('.invalid-feedback:not(.js-error-message)');
        if (serverErrorDiv) {
            serverErrorDiv.style.display = 'none';
        }
        
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
    
    /**
     * Clear validation error for a field
     */
    function clearError(field) {
        field.classList.remove('is-invalid');
        const errorDiv = field.parentElement.querySelector('.js-error-message');
        if (errorDiv) {
            errorDiv.textContent = '';
            errorDiv.style.display = 'none';
        }
        
        // Show server-side errors again if they exist
        const serverErrorDiv = field.parentElement.querySelector('.invalid-feedback:not(.js-error-message)');
        if (serverErrorDiv && serverErrorDiv.textContent.trim()) {
            serverErrorDiv.style.display = 'block';
            field.classList.add('is-invalid');
        }
    }

    // Add real-time validation on blur for unique fields
    const uniqueFields = document.querySelectorAll('#aisheId, #institutionOfficialEmailId, #institutionOfficialContactNumber, #institutionWebsite');
    uniqueFields.forEach(field => {
        // Clear errors on input
        field.addEventListener('input', () => clearError(field));
        
        // Validate on blur (when user leaves field)
        field.addEventListener('blur', async () => {
            if (field.value.trim()) {
                await checkUniqueness(field);
            }
        });
    });

    /**
     * Check uniqueness via AJAX call to validation controller
     */
    async function checkUniqueness(field) {
        // If field is empty, clear errors and return valid
        if (!field.value.trim()) {
            clearError(field);
            return true;
        }

        const endpoints = {
            'aisheId': '/api/validate/aisheId',
            'institutionOfficialEmailId': '/api/validate/email',
            'institutionOfficialContactNumber': '/api/validate/contact',
            'institutionWebsite': '/api/validate/website'
        };

        const endpoint = endpoints[field.id];
        if (!endpoint) return true;

        try {
            console.log(`Validating ${field.id} with value: ${field.value}`); // Debug log
            
            const response = await axios.post(endpoint, {
                value: field.value.trim(),
                instituteId: currentInstituteId 
            });
            
            console.log(`Validation response for ${field.id}:`, response.data); // Debug log
            
            if (!response.data.isUnique) {
                showError(field, response.data.message);
                return false;
            } else {
                clearError(field);
                return true;
            }
        } catch (error) {
            console.error(`Validation request for ${field.id} failed:`, error);
            let errorMessage = 'Validation failed. Please try again.';
            
            if (error.response && error.response.data && error.response.data.message) {
                errorMessage = error.response.data.message;
            } else if (error.message) {
                errorMessage = `Network error: ${error.message}`;
            }
            
            showError(field, errorMessage);
            return false;
        }
    }

    /**
     * Validate all fields in the current step
     */
    async function validateCurrentStep() {
        let isStepValid = true;
        const currentStepElement = formSteps[currentStep];
        const requiredFields = currentStepElement.querySelectorAll('input[required], select[required]');

        // Clear previous validation states
        currentStepElement.querySelectorAll('.is-invalid').forEach(el => {
            clearError(el);
        });

        // Check required fields first
        for (const field of requiredFields) {
            if (!field.value.trim()) {
                isStepValid = false;
                showError(field, 'This field is required.');
            }
        }

        // If basic validation failed, stop here
        if (!isStepValid) {
            const firstInvalid = currentStepElement.querySelector('.is-invalid');
            if (firstInvalid) firstInvalid.focus();
            return false;
        }

        // Check uniqueness for fields that have values
        const fieldsToValidateOnServer = currentStepElement.querySelectorAll('#aisheId, #institutionOfficialEmailId, #institutionOfficialContactNumber, #institutionWebsite');
        for (const field of fieldsToValidateOnServer) {
            if (field.value.trim()) { // Only validate non-empty fields
                const isUnique = await checkUniqueness(field);
                if (!isUnique) isStepValid = false;
            }
        }

        if (!isStepValid) {
            const firstInvalid = currentStepElement.querySelector('.is-invalid');
            if (firstInvalid) firstInvalid.focus();
        }
        
        return isStepValid;
    }

    // Rest of your existing functions remain the same...
    function showStep(stepIndex) {
        formSteps.forEach(step => step.classList.remove('active-step'));
        formSteps[stepIndex].classList.add('active-step');
        updateButtons();
        updateStepIndicator(); 
    }

    function updateButtons() {
        prevBtn.style.display = (currentStep === 0) ? 'none' : 'block';
        if (currentStep === formSteps.length - 1) {
            nextBtn.classList.add('d-none');
            submitBtn.classList.remove('d-none');
        } else {
            nextBtn.classList.remove('d-none');
            submitBtn.classList.add('d-none');
        }
    }

    function updateStepIndicator() {
        stepIndicators.forEach((indicator, index) => {
            if (index < currentStep) {
                indicator.classList.add('completed');
                indicator.classList.remove('active');
            } else if (index === currentStep) {
                indicator.classList.add('active');
                indicator.classList.remove('completed');
            } else {
                indicator.classList.remove('active', 'completed');
            }
        });

        const firstIcon = stepIndicators[0].querySelector('.step-indicator-icon');
        const currentIcon = stepIndicators[currentStep].querySelector('.step-indicator-icon');
        const lastIcon = stepIndicators[stepIndicators.length - 1].querySelector('.step-indicator-icon');
        
        const getIconCenter = (icon) => icon.offsetLeft + (icon.offsetWidth / 2);

        const firstIconCenter = getIconCenter(firstIcon);
        const currentIconCenter = getIconCenter(currentIcon);
        const lastIconCenter = getIconCenter(lastIcon);

        progressTrack.style.left = `${firstIconCenter}px`;
        progressTrack.style.width = `${lastIconCenter - firstIconCenter}px`;

        progressFill.style.left = `${firstIconCenter}px`;
        progressFill.style.width = `${currentIconCenter - firstIconCenter}px`;
    }

    // Event listeners
    nextBtn.addEventListener('click', async () => {
        const isValid = await validateCurrentStep();
        if (isValid && currentStep < formSteps.length - 1) {
            currentStep++;
            showStep(currentStep);
        }
    });

    prevBtn.addEventListener('click', () => {
        if (currentStep > 0) {
            currentStep--;
            showStep(currentStep);
        }
    });

    form.addEventListener('submit', async function(e) {
        const isValid = await validateCurrentStep();
        if (!isValid) {
            e.preventDefault();
            const firstInvalid = form.querySelector('.is-invalid');
            if (firstInvalid) {
                const errorStepElement = firstInvalid.closest('.form-step');
                if (errorStepElement) {
                    const errorStepIndex = formSteps.indexOf(errorStepElement);
                    if (errorStepIndex > -1 && errorStepIndex !== currentStep) {
                        currentStep = errorStepIndex;
                        showStep(currentStep);
                    }
                }
            }
        }
    });

    // Handle server-side errors on page load
    const serverErrorField = document.querySelector('.is-invalid');
    if (serverErrorField) {
        const errorStepElement = serverErrorField.closest('.form-step');
        if (errorStepElement) {
            const errorStepIndex = formSteps.indexOf(errorStepElement);
            if (errorStepIndex > -1) {
                currentStep = errorStepIndex;
                showStep(currentStep);
            }
        }
    } else {
        showStep(currentStep);
    }

    window.addEventListener('resize', updateStepIndicator);
});
