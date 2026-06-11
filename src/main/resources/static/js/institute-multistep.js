document.addEventListener('DOMContentLoaded', function () {
    var stateDropdown = document.getElementById('stateDropdown');
    if (stateDropdown && stateDropdown.value) {
        stateDropdown.dispatchEvent(new Event('change'));
    }

    var prevBtn        = document.getElementById('prevBtn');
    var nextBtn        = document.getElementById('nextBtn');
    var submitBtn      = document.getElementById('submitBtn');
    var form           = document.getElementById('instituteForm');
    var formSteps      = Array.from(document.querySelectorAll('.form-step'));
    var stepIndicators = Array.from(document.querySelectorAll('.step-indicator'));
    var progressTrack  = document.querySelector('.progress-track');
    var progressFill   = document.querySelector('.progress-fill');

    var currentStep = 0;
    var LAST_STEP   = formSteps.length - 1;

    var instituteIdInput   = document.querySelector('input[name="instituteId"]');
    var currentInstituteId = (instituteIdInput && instituteIdInput.value)
                             ? parseInt(instituteIdInput.value, 10) : null;

    function showFieldError(field, message) {
        field.classList.add('is-invalid');
        var errorDiv = field.parentElement.querySelector('.js-error-message');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback js-error-message';
            field.parentElement.appendChild(errorDiv);
        }
        var serverError = field.parentElement.querySelector('.invalid-feedback:not(.js-error-message)');
        if (serverError) { serverError.style.display = 'none'; }
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }

    function clearFieldError(field) {
        field.classList.remove('is-invalid');
        var errorDiv = field.parentElement.querySelector('.js-error-message');
        if (errorDiv) {
            errorDiv.textContent = '';
            errorDiv.style.display = 'none';
        }
        var serverError = field.parentElement.querySelector('.invalid-feedback:not(.js-error-message)');
        if (serverError && serverError.textContent.trim()) {
            serverError.style.display = 'block';
            field.classList.add('is-invalid');
        }
    }

    var uniqueFields = document.querySelectorAll(
        '#aisheId, #institutionOfficialEmailId, #institutionOfficialContactNumber, #institutionWebsite'
    );
    uniqueFields.forEach(function (field) {
        field.addEventListener('input', function () { clearFieldError(field); });
        field.addEventListener('blur', async function () {
            if (field.value.trim()) { await checkUniqueness(field); }
        });
    });

    async function checkUniqueness(field) {
        if (!field.value.trim()) { clearFieldError(field); return true; }

        var endpoints = {
            'aisheId':                         '/api/validate/aisheId',
            'institutionOfficialEmailId':       '/api/validate/email',
            'institutionOfficialContactNumber': '/api/validate/contact',
            'institutionWebsite':               '/api/validate/website'
        };
        var endpoint = endpoints[field.id];
        if (!endpoint) { return true; }

        try {
            var response = await axios.post(endpoint, {
                value:       field.value.trim(),
                instituteId: currentInstituteId
            });
            if (!response.data.isUnique) {
                showFieldError(field, response.data.message);
                return false;
            }
            clearFieldError(field);
            return true;
        } catch (error) {
            var msg = 'Validation failed. Please try again.';
            if (error.response && error.response.data && error.response.data.message) {
                msg = error.response.data.message;
            } else if (error.message) {
                msg = 'Network error: ' + error.message;
            }
            showFieldError(field, msg);
            return false;
        }
    }

    async function validateCurrentStep() {
        var isStepValid    = true;
        var currentStepEl  = formSteps[currentStep];
        var requiredFields = currentStepEl.querySelectorAll('input[required], select[required]');

        currentStepEl.querySelectorAll('.is-invalid').forEach(function (el) {
            clearFieldError(el);
        });

        requiredFields.forEach(function (field) {
            if (!field.value.trim()) {
                isStepValid = false;
                showFieldError(field, 'This field is required.');
            }
        });

        if (!isStepValid) {
            var first = currentStepEl.querySelector('.is-invalid');
            if (first) { first.focus(); }
            return false;
        }

        var serverFields = currentStepEl.querySelectorAll(
            '#aisheId, #institutionOfficialEmailId, #institutionOfficialContactNumber, #institutionWebsite'
        );
        for (var i = 0; i < serverFields.length; i++) {
            var f = serverFields[i];
            if (f.value.trim()) {
                var unique = await checkUniqueness(f);
                if (!unique) { isStepValid = false; }
            }
        }

        if (!isStepValid) {
            var firstInvalid = currentStepEl.querySelector('.is-invalid');
            if (firstInvalid) { firstInvalid.focus(); }
        }
        return isStepValid;
    }

    function showStep(stepIndex) {
        formSteps.forEach(function (s) { s.classList.remove('active-step'); });
        formSteps[stepIndex].classList.add('active-step');
        updateButtons();
        updateStepIndicator();

        // NIC Audit Fix #1 — show/hide CAPTCHA on the final step
        if (typeof instituteCaptcha !== 'undefined') {
            if (stepIndex === LAST_STEP) {
                instituteCaptcha.showSection();
            } else {
                instituteCaptcha.hideSection();
            }
        }
    }

    function updateButtons() {
        prevBtn.style.display = (currentStep === 0) ? 'none' : 'block';
        if (currentStep === LAST_STEP) {
            nextBtn.classList.add('d-none');
            submitBtn.classList.remove('d-none');
        } else {
            nextBtn.classList.remove('d-none');
            submitBtn.classList.add('d-none');
        }
    }

    function updateStepIndicator() {
        stepIndicators.forEach(function (indicator, index) {
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

        var firstIcon   = stepIndicators[0].querySelector('.step-indicator-icon');
        var currentIcon = stepIndicators[currentStep].querySelector('.step-indicator-icon');
        var lastIcon    = stepIndicators[stepIndicators.length - 1].querySelector('.step-indicator-icon');

        var getCenter = function (icon) { return icon.offsetLeft + (icon.offsetWidth / 2); };

        var firstCenter   = getCenter(firstIcon);
        var currentCenter = getCenter(currentIcon);
        var lastCenter    = getCenter(lastIcon);

        progressTrack.style.left  = firstCenter + 'px';
        progressTrack.style.width = (lastCenter - firstCenter) + 'px';
        progressFill.style.left   = firstCenter + 'px';
        progressFill.style.width  = (currentCenter - firstCenter) + 'px';
    }

    nextBtn.addEventListener('click', async function () {
        var isValid = await validateCurrentStep();
        if (isValid && currentStep < LAST_STEP) {
            currentStep++;
            showStep(currentStep);
        }
    });

    prevBtn.addEventListener('click', function () {
        if (currentStep > 0) {
            currentStep--;
            showStep(currentStep);
        }
    });

    form.addEventListener('submit', async function (e) {
        var isValid = await validateCurrentStep();
        if (!isValid) {
            e.preventDefault();
            var firstInvalid = form.querySelector('.is-invalid');
            if (firstInvalid) {
                var errorStepEl = firstInvalid.closest('.form-step');
                if (errorStepEl) {
                    var idx = formSteps.indexOf(errorStepEl);
                    if (idx > -1 && idx !== currentStep) {
                        currentStep = idx;
                        showStep(currentStep);
                    }
                }
            }
        }
    });

    var serverErrorField = document.querySelector('.is-invalid');
    if (serverErrorField) {
        var errorStepEl = serverErrorField.closest('.form-step');
        if (errorStepEl) {
            var idx = formSteps.indexOf(errorStepEl);
            if (idx > -1) { currentStep = idx; }
        }
    }
    showStep(currentStep);

    window.addEventListener('resize', updateStepIndicator);
});
