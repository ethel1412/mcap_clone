document.addEventListener('DOMContentLoaded', () => {
  console.log("JS Loaded - create-schedule.js");

  const form = document.getElementById('createScheduleForm');
  const alertBox = document.getElementById('alert-box');
  const submitBtn = document.getElementById('submitBtn');

  if (!form) {
    console.error("Form element #createScheduleForm not found!");
    return;
  }

  // CSRF token and header name from meta tags
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

  // Initialize Flatpickr on datetime inputs
  flatpickr('.datetimepicker', {
    enableTime: true,
    dateFormat: "Y-m-d H:i",
    time_24hr: true,
    minDate: "today"
  });

  form.addEventListener('submit', async event => {
    console.log("Submit event triggered");
    event.preventDefault();

    // Clear previous validation states and alerts
    form.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    alertBox.innerHTML = '';

    // Simple validity check including dropdown
    if (!form.checkValidity()) {
      form.querySelectorAll(':invalid').forEach(el => el.classList.add('is-invalid'));
      return;
    }

    // Validate that endDate is after startDate
    const startDate = form.startDate.value;
    const endDate = form.endDate.value;

    if (new Date(endDate) <= new Date(startDate)) {
      form.endDate.classList.add('is-invalid');
      alertBox.innerHTML = `<div class="alert alert-danger" role="alert">End Date must be after Start Date.</div>`;
      return;
    }

    // Prepare data from form fields
      const data = {
          name: form.name.value,
          description: form.description.value.trim(),
          startDate: form.startDate.value,
          endDate: form.endDate.value
      };


    // Disable submit button to avoid duplicated submits
    submitBtn.disabled = true;
    submitBtn.textContent = 'Creating...';

    try {
      const response = await axios.post('/schedule-data/schedules', data, {
        headers: {
          'Content-Type': 'application/json',
          [csrfHeader]: csrfToken
        }
      });

      alertBox.innerHTML = `<div class="alert alert-success" role="alert">Schedule created successfully!</div>`;
      form.reset();
    } catch (error) {
      const msg = error.response?.data?.message || 'Failed to create schedule.';
      alertBox.innerHTML = `<div class="alert alert-danger" role="alert">${msg}</div>`;
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Create Schedule';
    }
  });
    document.getElementById('scheduleName').addEventListener('change', function() {
        const dropdown = document.getElementById('admission-window-dropdown');
        if (this.value === "Admission Window") { // or the exact string for Admission Window
            dropdown.style.display = 'block';
        } else {
            dropdown.style.display = 'none';
            document.getElementById('admissionWindow').value = '';
        }
    });

    document.getElementById('admissionWindow').addEventListener('change', function() {
        const selected = this.options[this.selectedIndex];
        const start = selected.getAttribute('data-start');
        const end = selected.getAttribute('data-end');
        if (start && end) {
            document.getElementById('startDate').value = start;
            document.getElementById('endDate').value = end;
        }
    });




});
