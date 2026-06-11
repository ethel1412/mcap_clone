let currentPage = 0;
const pageSize = 10;
let totalPages = 0;

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById('usersBody');
    if (!tbody) return;

    document.getElementById('prevBtn').addEventListener('click', () => changePage(currentPage - 1));
    document.getElementById('nextBtn').addEventListener('click', () => changePage(currentPage + 1));

    setTimeout(() => loadUsers(), 0);
});

async function loadUsers(page = 0, size = pageSize) {
    const tbody = document.getElementById('usersBody');
    tbody.innerHTML = `<tr><td colspan="7" class="text-muted text-center">Loading...</td></tr>`;

    try {
        const res = await axios.get('/user-management/data/get-users', { params: { page, size } });
        const users = Array.isArray(res.data?.data) ? res.data.data : [];

        totalPages = res.data.totalPages ?? 0;
        currentPage = res.data.pageNumber ?? 0;

        updatePaginationControls();

        if (users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-muted text-center">No users found</td></tr>`;
            return;
        }

        tbody.innerHTML = users.map((u, i) => {
            const isLocked = !u.accountNonLocked;
            const roleName = u.role?.roleName || '';
            return `
                <tr>
                  <td>${i + 1 + (page * size)}</td>
                  <td>${u.userCode}</td>
                  <td>${u.username || ''}</td>
                  <td>${roleName}</td>
                  <td>${u.orgOwnerType || '-'}</td>
                  <td>${u.dateJoined || ''}</td>
                  <td>
                    <span class="badge ${isLocked ? 'bg-danger' : 'bg-success'}">
                      ${isLocked ? 'Locked' : 'Active'}
                    </span>
                  </td>
                  <td class="text-center">
                    <button class="btn btn-sm ${isLocked ? 'btn-success' : 'btn-danger'}"
                        onclick="toggleLock('${u.userCode}', ${isLocked ? true : false})">
                      ${isLocked ? 'Unlock' : 'Lock'}
                    </button>
                  </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger text-center">Error loading users</td></tr>`;
        showToast('Error fetching users', 'danger');
    }
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadUsers(page);
    }
}

function updatePaginationControls() {
    document.getElementById('pageInfo').textContent = `Page ${currentPage + 1} of ${totalPages || 1}`;
    document.getElementById('prevBtn').disabled = currentPage <= 0;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages - 1;
}

async function toggleLock(userCode, unlock) {
    try {
        const res = await axios.post('/user-management/data/lock-unlock-user',
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
