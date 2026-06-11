// Import pagination helper
import { Pagination } from '/js/common/pagination.js';

// Always send cookies with requests
axios.defaults.withCredentials = true;

document.addEventListener("DOMContentLoaded", () => {
    Pagination.init({
        onPageChange: (page, size) => loadRoles(page, size),
        initialPageSize: 10
    });

    loadRoles(0, 10);

    document.getElementById("addRoleForm")
        .addEventListener("submit", submitAddRole);
});

async function loadRoles(page = 0, size = 10) {
    const tbody = document.getElementById("rolesBody");

    // ===== Loading state =====
    tbody.replaceChildren();

    const trLoad = document.createElement("tr");
    const tdLoad = document.createElement("td");
    tdLoad.colSpan = 3;
    tdLoad.className = "text-center text-muted";
    tdLoad.textContent = "Loading...";
    trLoad.appendChild(tdLoad);
    tbody.appendChild(trLoad);

    try {
        const response = await axios.get("/role-data/roles");
        const roles = response.data || [];

        const totalPages = Math.max(1, Math.ceil(roles.length / size));
        const pagedRoles = roles.slice(page * size, (page + 1) * size);

        // ===== Clear loading =====
        tbody.replaceChildren();

        if (pagedRoles.length) {
            pagedRoles.forEach((role, idx) => {
                const tr = document.createElement("tr");

                const td1 = document.createElement("td");
                td1.textContent = idx + 1 + (page * size);

                const td2 = document.createElement("td");
                td2.textContent = role.roleId;

                const td3 = document.createElement("td");
                td3.textContent = role.roleName;

                tr.append(td1, td2, td3);
                tbody.appendChild(tr);
            });
        } else {
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            td.colSpan = 3;
            td.className = "text-center";
            td.textContent = "No roles found";
            tr.appendChild(td);
            tbody.appendChild(tr);
        }

        Pagination.update({
            pageNumber: page,
            totalPageCount: totalPages
        });

    } catch (error) {
        console.error("Error loading roles:", error);

        tbody.replaceChildren();

        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 3;
        td.className = "text-danger text-center";
        td.textContent = "Error loading roles";
        tr.appendChild(td);
        tbody.appendChild(tr);

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

    const csrfTokenEl = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');

    let headers = { 'Content-Type': 'application/json' };

    if (csrfTokenEl && csrfHeaderEl) {
        headers[csrfHeaderEl.content] = csrfTokenEl.content;
    }

    try {
        await axios.post("/role-data/roles",
            { roleId, roleName },
            { headers }
        );

        showToast("Role added successfully.", "success");

        bootstrap.Modal
            .getInstance(document.getElementById("addRoleModal"))
            .hide();

        event.target.reset();

        loadRoles(Pagination.currentPage || 0, 10);

    } catch (error) {
        console.error("Add Role Error:", error.response || error);

        let errMsg = "Failed to add role.";

        if (error.response?.data) {
            if (typeof error.response.data === 'string') {
                errMsg = error.response.data;
            } else if (typeof error.response.data.message === 'string') {
                errMsg = error.response.data.message;
            } else if (Array.isArray(error.response.data.errors)) {
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

    const body = toastEl.querySelector(".toast-body");
    if (body) body.textContent = message;

    toastEl.className = `toast bg-${type} text-white`;

    new bootstrap.Toast(toastEl).show();
}