document.addEventListener("DOMContentLoaded", async () => {
  const wrapper = document.getElementById("profileWrapper");
  const loggedInUserCode = wrapper?.dataset.usercode;

  const usernameEl = document.getElementById("username");
  const roleEl = document.getElementById("role");
  const dateEl = document.getElementById("dateJoined");
  const statusEl = document.getElementById("accountStatus");

  // Mapping backend codes to friendly display names
  const ROLE_LABELS = {
    'ADMIN': 'Admin',
    'INST_ADMIN': 'Institute Admin',
    'SUPER_ADMIN': 'Super Administrator',
    'STUDENT': 'Student',
    'TEACHER': 'Teacher'
    // add more as needed
  };

  // Loading placeholders
  usernameEl.textContent = 'Loading...';
  roleEl.textContent = '...';
  dateEl.textContent = '...';

  try {
    if (!loggedInUserCode) {
      throw new Error("User code not available");
    }

    const res = await axios.post(
      '/user-management/data/get-user-by-usercode',
      { user_code: loggedInUserCode },
      { headers: { 'Content-Type': 'application/json' } }
    );

    const user = res.data;

    // Populate fields
    usernameEl.textContent = user.username || '-';
    roleEl.textContent = ROLE_LABELS[user.role?.roleName] || user.role?.roleName || '-';

    if (user.dateJoined) {
      const dateObj = new Date(user.dateJoined);
      dateEl.textContent = dateObj.toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
      });
    } else {
      dateEl.textContent = '-';
    }

    // Status badges
    let statusHtml = '';
    statusHtml += user.enabled
      ? '<span class="badge bg-success me-2">Enabled</span>'
      : '<span class="badge bg-danger me-2">Disabled</span>';
    statusHtml += user.accountNonLocked
      ? '<span class="badge bg-success me-2">Unlocked</span>'
      : '<span class="badge bg-danger me-2">Locked</span>';
    statusHtml += user.accountNonExpired
      ? '<span class="badge bg-success me-2">Not Expired</span>'
      : '<span class="badge bg-warning me-2">Expired</span>';
    statusEl.innerHTML = statusHtml;

  } catch (err) {
    console.error("Error loading profile:", err);
    usernameEl.textContent = '-';
    roleEl.textContent = '-';
    dateEl.textContent = '-';
    statusEl.innerHTML = `<div class="alert alert-danger">Failed to load profile details.</div>`;
  }
});
