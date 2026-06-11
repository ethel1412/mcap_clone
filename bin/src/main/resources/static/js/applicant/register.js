document.addEventListener('DOMContentLoaded', function () {
  const sections       = document.querySelectorAll('.form-section');
  const progressSteps  = document.querySelectorAll('.progress-step');
  const progressFill   = document.getElementById('progress-fill');
  const form           = document.getElementById('registrationForm');

  const nextBtn1 = document.getElementById('next-btn-1');
  const nextBtn2 = document.getElementById('next-btn-2');
  const prevBtn2 = document.getElementById('prev-btn-2');
  const prevBtn3 = document.getElementById('prev-btn-3');

  const addressTabs    = document.querySelectorAll('.address-tab');
  const addressPanels  = document.querySelectorAll('.address-panel');
  const sameAsPermanentCheckbox = document.getElementById('sameAsPermanent');

  const countryCodeSelect = document.getElementById('countryPhoneCode');
  const phoneInput        = document.getElementById('phoneNumber');

  const commAddressFields = {
    addressLine1: document.getElementById('commAddress1'),
    addressLine2: document.getElementById('commAddress2'),
    stateCode:    document.getElementById('commState'),
    districtCode: document.getElementById('commDistrict'),
    blockCode:    document.getElementById('commBlock'),
    pincode:      document.getElementById('commPincode')
  };

  const permAddressFields = {
    addressLine1: document.getElementById('permAddress1'),
    addressLine2: document.getElementById('permAddress2'),
    stateCode:    document.getElementById('permState'),
    districtCode: document.getElementById('permDistrict'),
    blockCode:    document.getElementById('permBlock'),
    pincode:      document.getElementById('permPincode')
  };

  // ---- Phone validation toggle
  function updatePhoneValidation() {
    if (!countryCodeSelect || !phoneInput) return;
    const cc = countryCodeSelect.value;
    if (cc === '+91') {
      phoneInput.maxLength = 10;
      phoneInput.pattern   = '[0-9]{10}';
      phoneInput.placeholder = '10-digit number';
    } else {
      phoneInput.maxLength = 15;
      phoneInput.pattern   = '[0-9]{5,15}';
      phoneInput.placeholder = 'Up to 15 digits';
    }
  }
  if (countryCodeSelect) {
    countryCodeSelect.addEventListener('change', updatePhoneValidation);
    updatePhoneValidation();
  }

  // ---- Progress + step switching
  let currentStep = 1;
  const totalSteps = sections.length;

  function updateProgress() {
    if (!progressFill) return;
    const percent = ((currentStep - 1) / (totalSteps - 1)) * 100;
    progressFill.style.width = percent + '%';
  }

  function showStep(step) {
    sections.forEach((sec, idx) => {
      const active = idx === (step - 1);
      sec.classList.toggle('active', active);
      // Disable inactive fields to avoid hidden required validation
      sec.querySelectorAll('input, select, textarea').forEach(el => {
        el.disabled = !active;
      });
    });

    progressSteps.forEach((el, idx) => {
      el.classList.toggle('active', idx < step);
    });

    currentStep = step;
    updateProgress();

    // Optional: centre the new section in view
    const currentSection = sections[step - 1];
    if (currentSection) currentSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  // Initialise: only step 1 enabled
  showStep(1);

  // ---- Validation for the *current* section
  function validateCurrentSection() {
    const curr = sections[currentStep - 1];
    if (!curr) return true;

    const fields = curr.querySelectorAll('input:not([type=hidden]):not([disabled]), select:not([disabled]), textarea:not([disabled])');

    let ok = true;
    let firstInvalid = null;

    fields.forEach(field => {
      field.classList.remove('is-invalid');
      const emptyRequired = field.hasAttribute('required') && !String(field.value || '').trim();
      const badByBrowser  = field.matches(':invalid'); // pattern/email/date, etc.

      if (emptyRequired || badByBrowser) {
        ok = false;
        field.classList.add('is-invalid');
        if (!firstInvalid) firstInvalid = field;
      }
    });

    if (!ok && firstInvalid) {
      firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
      firstInvalid.focus({ preventScroll: true });
    }
    return ok;
  }

  // ---- Address tab toggling
  addressTabs.forEach(tab => {
    tab.addEventListener('click', function () {
      const target = this.id === 'btn-permanent' ? 'permanent' : 'communication';
      addressTabs.forEach(t => t.classList.remove('active'));
      this.classList.add('active');
      addressPanels.forEach(panel => {
        panel.classList.toggle('active', panel.id === `${target}-address`);
      });
    });
  });

  // ---- Enable/disable communication fields (when "same as permanent" checked)
  function setCommunicationFieldsState(isReadOnly) {
    if (!commAddressFields.addressLine1) return;
    // text inputs readonly
    commAddressFields.addressLine1.readOnly = isReadOnly;
    commAddressFields.addressLine2.readOnly = isReadOnly;
    commAddressFields.pincode.readOnly     = isReadOnly;
    // selects disabled
    commAddressFields.stateCode.disabled    = isReadOnly;
    commAddressFields.districtCode.disabled = isReadOnly;
    commAddressFields.blockCode.disabled    = isReadOnly;

    Object.values(commAddressFields).forEach(field => {
      if (!field) return;
      field.classList.toggle('bg-light', isReadOnly);
    });
  }

  // ---- Fetch helper returns a Promise (no brittle timeouts)
  function updateDropdown(url, targetDropdown) {
    if (!targetDropdown) return Promise.resolve();
    targetDropdown.innerHTML = '<option value="">Loading...</option>';
    targetDropdown.disabled = true;

    return axios.get(url).then(resp => {
      targetDropdown.innerHTML = resp.data;
      targetDropdown.disabled = false;
      return targetDropdown;
    }).catch(err => {
      console.error('Failed to fetch and update dropdown:', err);
      targetDropdown.innerHTML = '<option value="">-- Error --</option>';
      targetDropdown.disabled = false;
      return targetDropdown;
    });
  }

  // Cascading selects
  document.querySelectorAll('.state-dropdown').forEach(stateSelect => {
    stateSelect.addEventListener('change', function () {
      const stateCode = this.value;
      const parent = this.closest('.row.g-2') || this.closest('.form-section') || document;
      const districtSelect = parent.querySelector('.district-dropdown');
      const blockSelect    = parent.querySelector('.block-dropdown');

      if (districtSelect) {
        districtSelect.innerHTML = '<option value="">-- select district --</option>';
        districtSelect.disabled = true;
      }
      if (blockSelect) {
        blockSelect.innerHTML = '<option value="">-- select block --</option>';
        blockSelect.disabled = true;
      }

      if (stateCode && districtSelect) {
        updateDropdown(`/applicants/districts?stateCode=${encodeURIComponent(stateCode)}`, districtSelect);
      }
    });
  });

  document.querySelectorAll('.district-dropdown').forEach(districtSelect => {
    districtSelect.addEventListener('change', function () {
      const districtCode = this.value;
      const parent = this.closest('.row.g-2') || this.closest('.form-section') || document;
      const blockSelect  = parent.querySelector('.block-dropdown');

      if (blockSelect) {
        blockSelect.innerHTML = '<option value="">-- select block --</option>';
        blockSelect.disabled = true;
      }
      if (districtCode && blockSelect) {
        updateDropdown(`/applicants/blocks?districtCode=${encodeURIComponent(districtCode)}`, blockSelect);
      }
    });
  });

  // ---- Same as permanent handler (copy + load dependent dropdowns reliably)
  if (sameAsPermanentCheckbox) {
    sameAsPermanentCheckbox.addEventListener('change', function () {
      const checked = this.checked;
      const flag = document.getElementById('sameAsPermanentFlag');
      if (flag) flag.value = String(checked);

      setCommunicationFieldsState(checked);

      if (checked) {
        // copy simple fields
        commAddressFields.addressLine1.value = permAddressFields.addressLine1.value;
        commAddressFields.addressLine2.value = permAddressFields.addressLine2.value;
        commAddressFields.pincode.value      = permAddressFields.pincode.value;

        // load and set district, then blocks, without timeouts
        const sVal = permAddressFields.stateCode.value;
        const dVal = permAddressFields.districtCode.value;
        const bVal = permAddressFields.blockCode.value;

        // set state directly
        commAddressFields.stateCode.value = sVal;

        // now fetch districts for that state, then set district, then fetch blocks, then set block
        updateDropdown(`/applicants/districts?stateCode=${encodeURIComponent(sVal)}`, commAddressFields.districtCode)
          .then(() => {
            commAddressFields.districtCode.value = dVal;
            return updateDropdown(`/applicants/blocks?districtCode=${encodeURIComponent(dVal)}`, commAddressFields.blockCode);
          })
          .then(() => {
            commAddressFields.blockCode.value = bVal;
          })
          .catch(console.error);

      } else {
        // reset comm fields
        Object.entries(commAddressFields).forEach(([key, field]) => {
          if (!field) return;
          if (key === 'stateCode') {
            // keep the same options as permanent, but clear selection
            field.innerHTML = permAddressFields.stateCode.innerHTML;
            field.value = '';
          } else if (field.tagName === 'SELECT') {
            field.innerHTML = '<option value="">-- select --</option>';
            field.value = '';
          } else {
            field.value = '';
          }
        });
      }
    });
  }

  // ---- Navigation
  if (nextBtn1) nextBtn1.addEventListener('click', () => { if (validateCurrentSection()) showStep(2); });
  if (nextBtn2) nextBtn2.addEventListener('click', () => { if (validateCurrentSection()) showStep(3); });
  if (prevBtn2) prevBtn2.addEventListener('click', () => showStep(1));
  if (prevBtn3) prevBtn3.addEventListener('click', () => showStep(2));

  // ---- Submit: validate current step; then re-enable all fields so values post
  if (form) {
        form.addEventListener('submit', function (event) {
            event.preventDefault(); // Stop the default page reload

            if (!validateCurrentSection()) {
                Swal.fire('Incomplete Form', 'Please fill out all required fields correctly.', 'warning');
                return;
            }
            
            // Re-enable all fields so their values are included in the submission
            document.querySelectorAll('.form-section').forEach(sec => {
                sec.querySelectorAll('input, select, textarea').forEach(el => el.disabled = false);
            });

            const formData = new FormData(form);
            const originalButtonHtml = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Submitting...`;

            axios.post(form.action, formData)
                .then(function (response) {
                    // This is the logic from your applicant-success.js file
                    const applicantNo = response.data.applicantNo;
                    const isNew = response.data.isNewUser;

                    let title = isNew ? 'Registration Successful!' : 'New Application Started!';
                    let text = isNew 
                        ? `Your Applicant ID is: <strong>${applicantNo}</strong><br><br>Please save this for future reference.`
                        : `A new application has been added to your existing account: <strong>${applicantNo}</strong>`;

                    Swal.fire({
                        icon: 'success',
                        title: title,
                        html: text,
                        confirmButtonText: 'Proceed to Login',
                        allowOutsideClick: false,
                        allowEscapeKey: false
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/applicants/login'; // Redirect to login page
                        }
                    });
                })
                .catch(function (error) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalButtonHtml;
                    
                    // ✅ THE FIX: Check for our specific 409 error status.
                    if (error.response && error.response.status === 409) {
                      Swal.fire({
                          icon: 'info',
                          title: 'Already Applied',
                          html: error.response.data.message, // Use the clear message from the backend
                          confirmButtonText: 'Go to Login Page', // This creates the button
                          showCancelButton: false,
                          allowOutsideClick: false
                      }).then((result) => {
                          // This block runs after the user clicks the button.
                          if (result.isConfirmed) {
                              window.location.href = '/applicants/login'; // Redirect the user
                          }
                      });
                  } else {
                      const errorMessage = error.response?.data?.message || 'An unknown error occurred.';
                      Swal.fire('Registration Failed', errorMessage, 'error');
                  }
            });
        });
    }
});