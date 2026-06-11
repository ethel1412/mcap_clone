document.addEventListener('DOMContentLoaded', async () => {
  const form = document.getElementById('assignForm');
  const alertSuccess = document.getElementById('alertSuccess');
  const alertError = document.getElementById('alertError');
  const select = form.querySelector('select[name="departmentId"]');
  const instituteId = form.querySelector('input[name="instituteId"]').value;

  // Fetch already assigned departments for this institute to exclude them
  let assignedDepartments = [];
  try {
    const response = await axios.get(`/institute-departments/data/by-institute?instituteId=${instituteId}`);
    assignedDepartments = response.data.map(dep => dep.departmentId);
  } catch (error) {
    console.error('Failed to fetch assigned departments', error);
  }

  // Populate the department dropdown excluding already assigned departments
  try {
    const allDepsResponse = await axios.get('/api/departments');
    const allDepartments = allDepsResponse.data;

    // Clear existing options
    select.innerHTML = '<option value="" disabled selected>Select a department</option>';

    // Add departments not already assigned
    allDepartments.forEach(dep => {
      if (!assignedDepartments.includes(dep.departmentId)) {
        const option = document.createElement('option');
        option.value = dep.departmentId;
        option.textContent = dep.departmentName;
        select.appendChild(option);
      }
    });
  } catch (error) {
    console.error('Failed to fetch departments', error);
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    alertSuccess.style.display = 'none';
    alertError.style.display = 'none';
    alertSuccess.textContent = '';
    alertError.textContent = '';

    const departmentId = select.value;
    if (!departmentId) {
      alertError.style.display = 'block';
      alertError.textContent = 'Please select a department.';
      select.focus();
      return;
    }

    const payload = {
      instituteId: Number(instituteId),
      departmentId: Number(departmentId),
      active: true,
      hodName: form.querySelector('input[name="hodName"]').value.trim() || null,
      email: form.querySelector('input[name="email"]').value.trim() || null,
      phone: form.querySelector('input[name="phone"]').value.trim() || null
    };

    try {
      await axios.post('/institute-departments/data', payload, {
        headers: { 'Content-Type': 'application/json' }
      });

      alertSuccess.style.display = 'block';
      alertSuccess.textContent = 'Department assigned successfully.';
      form.reset();

      // Optionally, refresh the dropdown to exclude newly assigned department
      const response = await axios.get(`/institute-departments/data/by-institute?instituteId=${instituteId}`);
      assignedDepartments = response.data.map(dep => dep.departmentId);
      select.innerHTML = '<option value="" disabled selected>Select a department</option>';
      const allDepsResponse = await axios.get('/institute-departments/data/departments');
      allDepsResponse.data.forEach(dep => {
        if (!assignedDepartments.includes(dep.departmentId)) {
          const option = document.createElement('option');
          option.value = dep.departmentId;
          option.textContent = dep.departmentName;
          select.appendChild(option);
        }
      });

    } catch (error) {
      alertError.style.display = 'block';
      if (error.response && error.response.data) {
        alertError.textContent = `Failed to assign department: ${error.response.data}`;
      } else {
        alertError.textContent = `Failed to assign department: ${error.message}`;
      }
    }
  });
});
