// Always send cookies for authentication
axios.defaults.withCredentials = true;

// Add CSRF token from meta tags to Axios headers globally
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
if (csrfToken && csrfHeader) {
  axios.defaults.headers.common[csrfHeader] = csrfToken;
}

document.addEventListener('DOMContentLoaded', () => {
  const tbody = document.querySelector('#schedulesTable tbody');

  // Bootstrap modal setup
  const updateModalElement = document.getElementById('updateScheduleModal');
  const updateModal = new bootstrap.Modal(updateModalElement);

  // Form elements
  const updateForm = document.getElementById('updateScheduleForm');
  const scheduleIdInput = document.getElementById('scheduleId');
  const startDateInput = document.getElementById('startDateInput');
  const endDateInput = document.getElementById('endDateInput');

  async function fetchAllSchedules() {
    try {
      const response = await axios.get('/schedule-data/schedules');
      return response.data;
    } catch (error) {
      console.error('Failed to load schedules:', error);
      return null;
    }
  }

  async function fetchScheduleById(id) {
    try {
      const response = await axios.get(`/schedule-data/schedules/${id}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch schedule:', error);
      return null;
    }
  }

    function renderSchedules(schedules) {
        tbody.innerHTML = '';
        const now = new Date();

        // Only show schedules whose endDate is in the future
        const validSchedules = schedules.filter(sch => new Date(sch.endDate) > now);

        if (!validSchedules || validSchedules.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center">No schedules found</td></tr>`;
            return;
        }

        validSchedules.forEach(schedule => {
            tbody.insertAdjacentHTML('beforeend', `
      <tr>
        <td>${schedule.name}${schedule.stream ? `<br><small class='text-muted'>${schedule.stream.streamName || schedule.stream}</small>` : ""}</td>
        <td>${schedule.description || ''}</td>
        <td>${new Date(schedule.startDate).toLocaleString()}</td>
        <td>${new Date(schedule.endDate).toLocaleString()}</td>
        <td>${schedule.status || ''}</td>
        <td>
          <button class="btn btn-primary btn-sm btn-update" data-id="${schedule.scheduleId}">Update</button>
        </td>
      </tr>
    `);
        });

    document.querySelectorAll('.btn-update').forEach(button => {
      button.addEventListener('click', async event => {
        // Get the closest button element to the clicked target, then get its data-id
        const clickedButton = event.target.closest('.btn-update'); // This ensures you get the button even if an icon inside was clicked
        const scheduleId = clickedButton ? clickedButton.getAttribute('data-id') : null;

        if (!scheduleId) { // Add a check to prevent calling fetchScheduleById with null/undefined
            console.error('Error: Could not retrieve schedule ID from the button.');
            return;
        }
        
        console.log('Fetching schedule with ID:', scheduleId); // Add debug log

        const schedule = await fetchScheduleById(scheduleId);
        if (schedule) {
          scheduleIdInput.value = schedule.scheduleId; // Also ensure this is schedule.scheduleId
          startDateInput.value = new Date(schedule.startDate).toISOString().slice(0, 16);
          endDateInput.value = new Date(schedule.endDate).toISOString().slice(0, 16);
          updateModal.show();
        }
      });
    });
  }

  updateForm.addEventListener('submit', async event => {
    event.preventDefault();

    const id = scheduleIdInput.value;
    const startDate = startDateInput.value;
    const endDate = endDateInput.value;

    if (new Date(endDate) <= new Date(startDate)) {
      alert('End Date must be after Start Date');
      return;
    }

    try {
      await axios.put(`/schedule-data/schedules/${id}`, {
        id,
        startDate,
        endDate
      });
      updateModal.hide();
      loadSchedules();
    } catch (error) {
      console.error('Failed to update schedule:', error);
      alert('Error updating schedule');
    }
  });

  async function loadSchedules() {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">Loading...</td></tr>`;
    const schedules = await fetchAllSchedules();
    if (schedules) {
      renderSchedules(schedules);
    } else {
      tbody.innerHTML =
        `<tr><td colspan="6" class="text-danger text-center">Error loading schedules.</td></tr>`;
    }
  }

  loadSchedules();
});
