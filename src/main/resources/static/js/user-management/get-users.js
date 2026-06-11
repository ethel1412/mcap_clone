let currentPage = 0;
const pageSize = 10;
let totalPages = 0;

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById('usersBody');
    if (!tbody) return;

    document.getElementById('prevBtn')
        .addEventListener('click', () => changePage(currentPage - 1));

    document.getElementById('nextBtn')
        .addEventListener('click', () => changePage(currentPage + 1));

    setTimeout(() => loadUsers(), 0);
});

async function loadUsers(page = 0, size = pageSize) {
    const tbody = document.getElementById('usersBody');

    // ===== Loading state =====
    tbody.replaceChildren();
    const trLoad = document.createElement("tr");
    const tdLoad = document.createElement("td");
    tdLoad.colSpan = 8;
    tdLoad.className = "text-muted text-center";
    tdLoad.textContent = "Loading...";
    trLoad.appendChild(tdLoad);
    tbody.appendChild(trLoad);

    try {
        const res = await axios.get('/user-management/data/get-users', {
            params: { page, size }
        });

        const users = Array.isArray(res.data?.data) ? res.data.data : [];

        totalPages = res.data.totalPages ?? 0;
        currentPage = res.data.pageNumber ?? 0;

        updatePaginationControls();

        // ===== Clear loading =====
        tbody.replaceChildren();

        if (users.length === 0) {
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            td.colSpan = 8;
            td.className = "text-muted text-center";
            td.textContent = "No users found";
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        users.forEach((u, i) => {
            const isLocked = !u.accountNonLocked;
            const roleName = u.role?.roleName || '';

            const tr = document.createElement("tr");

            // index
            const td1 = document.createElement("td");
            td1.textContent = i + 1 + (page * size);

            // userCode
            const td2 = document.createElement("td");
            td2.textContent = u.userCode;

            // username
            const td3 = document.createElement("td");
            td3.textContent = u.username || '';

            // role
            const td4 = document.createElement("td");
            td4.textContent = roleName;

            // org type
            const td5 = document.createElement("td");
            td5.textContent = u.orgOwnerType || '-';

            // date
            const td6 = document.createElement("td");
            td6.textContent = u.dateJoined || '';

            // status badge
            const td7 = document.createElement("td");
            const badge = document.createElement("span");
            badge.className = `badge ${isLocked ? 'bg-danger' : 'bg-success'}`;
            badge.textContent = isLocked ? 'Locked' : 'Active';
            td7.appendChild(badge);

            // action button
            const td8 = document.createElement("td");
            td8.className = "text-center";

            const btn = document.createElement("button");
            btn.className = `btn btn-sm ${isLocked ? 'btn-success' : 'btn-danger'}`;
            btn.textContent = isLocked ? 'Unlock' : 'Lock';

            // SAME logic as before
            btn.addEventListener("click", () => {
                toggleLock(u.userCode, isLocked ? true : false);
            });

            td8.appendChild(btn);

            tr.append(td1, td2, td3, td4, td5, td6, td7, td8);
            tbody.appendChild(tr);
        });

    } catch (err) {
        tbody.replaceChildren();

        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 8;
        td.className = "text-danger text-center";
        td.textContent = "Error loading users";

        tr.appendChild(td);
        tbody.appendChild(tr);

        showToast('Error fetching users', 'danger');
    }
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadUsers(page);
    }
}

function updatePaginationControls() {
    document.getElementById('pageInfo').textContent =
        `Page ${currentPage + 1} of ${totalPages || 1}`;

    document.getElementById('prevBtn').disabled = currentPage <= 0;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages - 1;
}

async function toggleLock(userCode, unlock) {
    try {
        const res = await axios.post(
            '/user-management/data/lock-unlock-user',
            { userCode, lock: unlock },
            { headers: { 'Content-Type': 'application/json' } }
        );

        showToast(res.data || 'Action successful', 'success');
        loadUsers(currentPage);

    } catch (err) {
        showToast(err.response?.data || 'Error updating status', 'danger');
    }
}

function showToast(message, type) {
    const toastEl = document.getElementById('statusToast');

    if (!toastEl) {
        alert(message);
        return;
    }

    const body = toastEl.querySelector('.toast-body');
    if (body) body.textContent = message;

    toastEl.className = `toast bg-${type} text-white`;
    new bootstrap.Toast(toastEl).show();
}