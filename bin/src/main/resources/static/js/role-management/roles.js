// Import pagination helper
import { Pagination } from '/js/common/pagination.js';

// Always send cookies with requests (needed for session auth)
axios.defaults.withCredentials = true;

document.addEventListener("DOMContentLoaded", () => {
    // Initialize pagination
    Pagination.init({
        onPageChange: (page, size) => loadRoles(page, size),
        initialPageSize: 10
    });

    // Load initial page
    loadRoles(0, 10);

    // Attach form submit handler for Add Role
    document.getElementById("addRoleForm").addEventListener("submit", submitAddRole);
});

async function loadRoles(page = 0, size = 10) {
    const tbody = document.getElementById("rolesBody");
    tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted">Loading...</td></tr>`;

    try {
        // Fetch all roles
        const response = await axios.get("/role-data/roles");
        const roles = response.data || [];

        // Client-side pagination
        const totalPages = Math.max(1, Math.ceil(roles.length / size));
        const pagedRoles = roles.slice(page * size, (page + 1) * size);

        tbody.innerHTML = pagedRoles.length
            ? pagedRoles.map((role, idx) => `
                <tr>
                    <td>${idx + 1 + (page * size)}</td>
                    <td>${role.roleId}</td>
                    <td>${role.roleName}</td>
                </tr>
            `).join("")
            : `<tr><td colspan="3" class="text-center">No roles found</td></tr>`;

        // Update pagination controls
        Pagination.update({ pageNumber: page, totalPageCount: totalPages });

    } catch (error) {
        console.error("Error loading roles:", error);
        tbody.innerHTML = `<tr><td colspan="3" class="text-danger text-center">Error loading roles</td></tr>`;
        showToast("Error fetching roles.", "danger");
    }
}

async function submitAddRole(event) {
    event.preventDefault();

    const roleId = document.getElementById("roleIdInput").value.trim();
    const roleName = document.getElementById("roleNameInput").value.trim();
    const errorDiv = document.getElementById("addRoleError");

    if (!roleId || !roleName) {
        errorDiv.textContent = "Both Role ID and Role Name are required.";
        errorDiv.classList.remove("d-none");
        return;
    }

    errorDiv.classList.add("d-none");

    // Get CSRF token from meta tags
    const csrfTokenEl = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');
    let headers = { 'Content-Type': 'application/json' };

    if (csrfTokenEl && csrfHeaderEl) {
        headers[csrfHeaderEl.content] = csrfTokenEl.content;
    }

    try {
        await axios.post("/role-data/roles", { roleId, roleName }, { headers });

        showToast("Role added successfully.", "success");

        // Hide modal & reset form
        bootstrap.Modal.getInstance(document.getElementById("addRoleModal")).hide();
        event.target.reset();

        // Refresh current page
        loadRoles(Pagination.currentPage || 0, 10);

    } catch (error) {
        console.error("Add Role Error:", error.response || error);

        let errMsg = "Failed to add role.";
        if (error.response?.data) {
            if (typeof error.response.data === 'string') {
                errMsg = error.response.data;
            } else if (typeof error.response.data.message === 'string') {
                errMsg = error.response.data.message;
            } else if (error.response.data.errors && Array.isArray(error.response.data.errors)) {
                errMsg = error.response.data.errors.join(", ");
            } else {
                try {
                    errMsg = JSON.stringify(error.response.data);
                } catch {}
            }
        }

        errorDiv.textContent = errMsg;
        errorDiv.classList.remove("d-none");
    }
}

function showToast(message, type) {
    const toastEl = document.getElementById("statusToast");
    if (!toastEl) {
        alert(message);
        return;
    }
    toastEl.querySelector(".toast-body").textContent = message;
    toastEl.className = `toast bg-${type} text-white`;
    new bootstrap.Toast(toastEl).show();
}
