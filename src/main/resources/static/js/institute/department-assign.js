document.addEventListener('DOMContentLoaded', () => {
  const addDepartmentModalEl = document.getElementById('addDepartmentModal');

  if (!addDepartmentModalEl) {
    return;
  }

  const form = addDepartmentModalEl.querySelector('form');
  const submitBtn = form.querySelector('button[type="submit"]');

  const reloadDepartments = () => {
    if (typeof loadDepartments === 'function') {
      loadDepartments();
    } else {
      console.warn('Could not find loadDepartments() function. Reloading page as a fallback.');
      location.reload();
    }
  };

  addDepartmentModalEl.addEventListener('hidden.bs.modal', () => {
    reloadDepartments();
    form.reset();
    form.classList.remove('was-validated');
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    event.stopPropagation();

    if (!form.checkValidity()) {
      form.classList.add('was-validated');
      return;
    }

    const originalContent = Array.from(submitBtn.childNodes).map(node => node.cloneNode(true));

    submitBtn.disabled = true;
    submitBtn.replaceChildren();

    const icon = document.createElement("i");
    icon.className = "fas fa-spinner fa-spin me-2";

    const text = document.createTextNode("Adding...");

    submitBtn.append(icon, text);

    const formData = new FormData(form);
    const payload = {
      departmentId: Number(formData.get('departmentId')),
      hodName: formData.get('hodName').trim() || null,
      email: formData.get('email').trim() || null,
      phone: formData.get('phone').trim() || null,
      active: formData.get('active') === 'true'
    };

    try {
      await axios.post('/institute-departments/data', payload, {
        headers: { 'Content-Type': 'application/json' }
      });

      if (typeof showToast === 'function') {
        showToast('success', 'Department assigned successfully.');
      } else {
        console.log('Department assigned successfully.');
      }

      const modal = bootstrap.Modal.getInstance(addDepartmentModalEl);
      if (modal) {
        modal.hide();
      }

    } catch (error) {
      console.error('Failed to assign department:', error);

      if (typeof showToast === 'function') {
        const errorMessage = error.response?.data?.message || 'Failed to assign department.';
        showToast('error', errorMessage);
      }

    } finally {
      submitBtn.disabled = false;
      submitBtn.replaceChildren(...originalContent);
    }
  });
});