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
    roleEl.textContent =
      ROLE_LABELS[user.role?.roleName] || user.role?.roleName || '-';

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

    // ===== Status badges (FIXED) =====
    statusEl.replaceChildren();

    function createBadge(text, type) {
      const span = document.createElement("span");
      span.className = `badge bg-${type} me-2`;
      span.textContent = text;
      return span;
    }

    statusEl.append(
      user.enabled
        ? createBadge("Enabled", "success")
        : createBadge("Disabled", "danger"),

      user.accountNonLocked
        ? createBadge("Unlocked", "success")
        : createBadge("Locked", "danger"),

      user.accountNonExpired
        ? createBadge("Not Expired", "success")
        : createBadge("Expired", "warning")
    );

  } catch (err) {
    console.error("Error loading profile:", err);

    usernameEl.textContent = '-';
    roleEl.textContent = '-';
    dateEl.textContent = '-';

    // ===== Error UI (FIXED) =====
    statusEl.replaceChildren();

    const alertDiv = document.createElement("div");
    alertDiv.className = "alert alert-danger";
    alertDiv.textContent = "Failed to load profile details.";

    statusEl.appendChild(alertDiv);
  }
});