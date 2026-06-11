axios.defaults.withCredentials = true; // Keep cookies for session auth

document.addEventListener("DOMContentLoaded", () => {
    loadMenus();
    document.getElementById("addMenuForm").addEventListener("submit", submitAddMenu);
});

async function loadMenus() {
    const tbody = document.getElementById("menusBody");
    tbody.innerHTML = `<tr><td colspan="4" class="text-center">Loading...</td></tr>`;

    try {
        const res = await axios.get("/role-data/menus");
        tbody.innerHTML = res.data.map((menu, idx) => `
            <tr>
                <td>${idx + 1}</td>
                <td>${menu.menuId || '-'}</td>
                <td>${menu.name}</td>
                <td>
                    ${menu.assignedRoles && menu.assignedRoles.length
                        ? menu.assignedRoles.map(r =>
                            `<span class="badge bg-primary me-1">${r.roleName}</span>`
                          ).join('')
                        : '<span class="text-muted">No roles assigned</span>'}
                </td>
            </tr>
        `).join("");
    } catch (err) {
        console.error("Error loading menus:", err);
        tbody.innerHTML = `<tr><td colspan="4" class="text-danger text-center">Error loading menus</td></tr>`;
    }
}

async function submitAddMenu(e) {
  e.preventDefault();

  const name = document.getElementById("menuNameInput").value.trim();
  const iconClass = document.getElementById("iconInput").value.trim();
  const orderIndex = parseInt(document.getElementById("orderInput").value) || 0;
  const parentMenuId = document.getElementById("parentMenuSelect").value || null;
  const active = document.getElementById("activeCheck").checked;

  const errorDiv = document.getElementById("addMenuError");

  if (!name) {
    errorDiv.textContent = "Menu name is required.";
    errorDiv.classList.remove("d-none");
    return;
  }
  errorDiv.classList.add("d-none");

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  try {
    await axios.post("/role-data/menus",
      {
        menuName: name,
        iconClass,
        orderIndex,
        active,
        parentMenuId: parentMenuId ? parseInt(parentMenuId) : null
      },
      { headers: { [csrfHeader]: csrfToken } }
    );

    bootstrap.Modal.getInstance(document.getElementById("addMenuModal")).hide();
    e.target.reset();
    loadMenus();
  } catch (err) {
    console.error("Error adding menu:", err);
    errorDiv.textContent = err.response?.data?.message || "Failed to add menu.";
    errorDiv.classList.remove("d-none");
  }
}

async function populateParentMenuSelect() {
  try {
    const res = await axios.get("/role-data/menus");
    const select = document.getElementById("parentMenuSelect");
    select.innerHTML = '<option value="">-- None (Top Level) --</option>';
    res.data.forEach(menu => {
      select.innerHTML += `<option value="${menu.menuId}">${menu.name}</option>`;
    });
  } catch (e) {
    console.error("Failed to load parent menus", e);
  }
}

async function loadIcons() {
  const container = document.getElementById("iconPickerContainer");
  const searchBox = document.getElementById("iconSearch");

  try {
    const res = await fetch("/data/icons.json");
    const icons = await res.json();

    // convert object keys ("house", "user", etc.)
    let allIcons = Object.keys(icons);

    function renderIcons(filter = "") {
      container.innerHTML = "";
      const filtered = allIcons.filter(name => name.includes(filter.toLowerCase())).slice(0, 100); // limit 100

      filtered.forEach(name => {
        const iconClass = `fa-solid fa-${name}`;
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "btn btn-outline-secondary icon-option";
        btn.dataset.icon = iconClass;
        btn.innerHTML = `<i class="${iconClass}"></i>`;
        btn.onclick = () => {
          // highlight selected
          document.querySelectorAll(".icon-option").forEach(b => b.classList.remove("btn-success"));
          btn.classList.add("btn-success");
          // save to hidden field
          document.getElementById("iconInput").value = iconClass;
        };
        container.appendChild(btn);
      });
    }

    renderIcons();

    // search event
    searchBox.addEventListener("input", e => renderIcons(e.target.value));

  } catch (err) {
    console.error("Failed to load icons:", err);
    container.innerHTML = `<div class="text-danger">Error loading icons</div>`;
  }
}

// call this when modal opens
document.getElementById("addMenuModal").addEventListener("shown.bs.modal", loadIcons);

