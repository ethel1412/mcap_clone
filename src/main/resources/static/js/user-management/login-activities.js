let currentPage = 0;
const pageSize = 10;
let totalPages = 0;

document.addEventListener("DOMContentLoaded", () => {
    const tbody = document.getElementById('loginActivitiesBody');
    if (!tbody) return;

    document.getElementById('prevBtn')
        .addEventListener('click', () => changePage(currentPage - 1));

    document.getElementById('nextBtn')
        .addEventListener('click', () => changePage(currentPage + 1));

    setTimeout(() => loadLoginActivities(), 0);
});

async function loadLoginActivities(page = 0, size = pageSize) {
    const tbody = document.getElementById('loginActivitiesBody');

    // ===== Loading =====
    tbody.replaceChildren();
    const trLoad = document.createElement("tr");
    const tdLoad = document.createElement("td");
    tdLoad.colSpan = 5;
    tdLoad.className = "text-center text-muted";
    tdLoad.textContent = "Loading...";
    trLoad.appendChild(tdLoad);
    tbody.appendChild(trLoad);

    try {
        const res = await axios.get('/user-management/data/get-login-activities', {
            params: { page, size }
        });

        const activities = Array.isArray(res.data?.data) ? res.data.data : [];

        totalPages = res.data.totalPages ?? 0;
        currentPage = res.data.pageNumber ?? 0;

        updatePaginationControls();

        // ===== Clear loading =====
        tbody.replaceChildren();

        if (activities.length === 0) {
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            td.colSpan = 5;
            td.className = "text-center";
            td.textContent = "No login activities found";
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        activities.forEach((act, i) => {
            const tr = document.createElement("tr");

            const td1 = document.createElement("td");
            td1.textContent = i + 1 + (page * size);

            const td2 = document.createElement("td");
            td2.textContent = act.username || '-';

            const td3 = document.createElement("td");
            td3.textContent = act.ipAddress || '-';

            const td4 = document.createElement("td");
            td4.textContent = act.time || '-';

            const td5 = document.createElement("td");

            const badge = document.createElement("span");
            badge.className = `badge ${act.isSuccess ? 'bg-success' : 'bg-danger'}`;
            badge.textContent = act.isSuccess ? 'Success' : 'Failed';

            td5.appendChild(badge);

            tr.append(td1, td2, td3, td4, td5);
            tbody.appendChild(tr);
        });

    } catch (err) {
        tbody.replaceChildren();

        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 5;
        td.className = "text-center text-danger";
        td.textContent = "Error loading login activities";
        tr.appendChild(td);
        tbody.appendChild(tr);

        showToast('Error loading login activities', 'danger');
    }
}

function changePage(page) {
    if (page >= 0 && page < totalPages) {
        loadLoginActivities(page);
    }
}

function updatePaginationControls() {
    document.getElementById('pageInfo').textContent =
        `Page ${currentPage + 1} of ${totalPages || 1}`;

    document.getElementById('prevBtn').disabled = currentPage <= 0;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages - 1;
}

function showToast(message, type) {
    const toastEl = document.getElementById("statusToast");
    if (!toastEl) return;

    const body = toastEl.querySelector(".toast-body");
    if (body) body.textContent = message;

    toastEl.className = `toast bg-${type} text-white`;
    new bootstrap.Toast(toastEl).show();
}