 // Bootstrap form validation
 (() => {
    'use strict'

    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
      form.addEventListener('submit', e => {
        if (!form.checkValidity()) {
          e.preventDefault();
          e.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  })();

  // Sync hidden login username with visible application number input
  (() => {
    const appInput = document.getElementById('reqAppNo');
    const loginHidden = document.getElementById('loginUsernameHidden');
    if (appInput && loginHidden) {
      const sync = () => {
        loginHidden.value = appInput.value.trim();
      };
      appInput.addEventListener('input', sync);
      document.addEventListener('DOMContentLoaded', sync);
      sync(); // Initial sync in case th:value is present on load
    }
  })();