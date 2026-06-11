let currentPage = 0;
const pageSize = 10;
let totalPages = 0;

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById('loginActivitiesBody');
    if (!tbody) return;

    document.getElementById('prevBtn').addEventListener('click', () => changePage(currentPage - 1));
    document.getElementById('nextBtn').addEventListener('click', () => changePage(currentPage + 1));

    setTimeout(() => loadLoginActivities(), 0);
});

async function loadLoginActivities(page = 0, size = pageSize) {
    const tbody = document.getElementById('loginActivitiesBody');
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Loading...</td></tr>`;

    try {
        const res = await axios.get('/user-management/data/get-login-activities', { params: { page, size } });
        const activities = Array.isArray(res.data?.data) ? res.data.data : [];

        totalPages = res.data.totalPages ?? 0;
        currentPage = res.data.pageNumber ?? 0;

        updatePaginationControls();

        if (activities.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center">No login activities found</td></tr>`;
            return;
        }

        tbody.innerHTML = activities.map((act, i) => `
            <tr>
                <td>${i + 1 + (page * size)}</td>
                <td>${act.username || '-'}</td>
                <td>${act.ipAddress || '-'}</td>
                <td>${act.time || '-'}</td>
                <td>
                    <span class="badge ${act.isSuccess ? 'bg-success' : 'bg-danger'}">
                        ${act.isSuccess ? 'Success' : 'Failed'}
                    </span>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Error loading login activities</td></tr>`;
        showToast('Error loading login activities', 'danger');
    }
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadLoginActivities(page);
    }
}

function updatePaginationControls() {
    document.getElementById('pageInfo').textContent = `Page ${currentPage + 1} of ${totalPages || 1}`;
    document.getElementById('prevBtn').disabled = currentPage <= 0;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages - 1;
}

function showToast(message, type) {
    const toastEl = document.getElementById("statusToast");
    if (!toastEl) return;
    const body = toastEl.querySelector(".toast-body");
    body.textContent = message;
    toastEl.className = `toast bg-${type} text-white`;
    new bootstrap.Toast(toastEl).show();
}
