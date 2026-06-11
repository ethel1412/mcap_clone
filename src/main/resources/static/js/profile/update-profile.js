document.addEventListener("DOMContentLoaded", async () => {
  const wrapper = document.getElementById("updateProfileWrapper");
  const userCode = wrapper?.dataset.usercode;

  const form = document.getElementById("updateProfileForm");

  if (!userCode) {
    showAlert("danger", "User code not available.");
    return;
  }

  try {
    // Load existing profile data
    const res = await axios.post(
      '/user-management/data/get-user-by-usercode',
      { user_code: userCode },
      { headers: { 'Content-Type': 'application/json' } }
    );

    const user = res.data;

    document.getElementById("userCode").value = user.userCode;
    document.getElementById("enabled").checked = user.enabled || false;
    document.getElementById("accountNonExpired").checked = user.accountNonExpired || false;
    document.getElementById("accountNonLocked").checked = user.accountNonLocked || false;
    document.getElementById("credentialsNonExpired").checked = user.credentialsNonExpired || false;

  } catch (err) {
    console.error("Error loading profile for update:", err);
    showAlert("danger", "Failed to load profile details.");
  }

  // Handle form submit
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const formData = new FormData(form);
    const data = Object.fromEntries(formData);

    // Convert checkbox values from "on"/undefined to boolean
    data.enabled = form.enabled.checked;
    data.accountNonExpired = form.accountNonExpired.checked;
    data.accountNonLocked = form.accountNonLocked.checked;
    data.credentialsNonExpired = form.credentialsNonExpired.checked;

    try {
      const res = await axios.post(
        '/user-management/data/update-user',
        data,
        { headers: { 'Content-Type': 'application/json' } }
      );
      showAlert("success", "Profile updated successfully.");
    } catch (err) {
      console.error("Update error:", err);
      showAlert("danger", err.response?.data || "Failed to update profile.");
    }
  });

  function showAlert(type, message) {
    const alertBox = document.getElementById("alert-box");
    alertBox.innerHTML = `
      <div class="alert alert-${type} alert-dismissible fade show" role="alert">
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      </div>`;
  }
});
