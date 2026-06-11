const DepartmentApprovalsModule = (() => {
  // CSRF Header Helper
  const getCsrfHeaders = () => {
    const token = document
      .querySelector('meta[name="_csrf"]')
      ?.getAttribute("content");
    const header = document
      .querySelector('meta[name="_csrf_header"]')
      ?.getAttribute("content");
    return token && header ? { [header]: token } : {};
  };

  const state = {
    requests: [],
    rejectingId: null,
  };

  // 🔧 keep reference only (no init here)
 
  const els = {
    tbody: document.getElementById("approvalTableBody"),
    searchInput: document.getElementById("tableSearchInput"),
    noResultsInfo: document.getElementById("noSearchResults"),

    rejectModal: null, // ✅ FIX: initialize later

    rejectBtn: document.getElementById("confirmRejectBtn"),
    rejectReason: document.getElementById("rejectionReason"),
    rejectNameSpan: document.getElementById("rejectDepartmentName"),

    toast: null,
    toastMsg: document.getElementById("toastMessage"),
    toastEl: document.getElementById("statusToast"),
  };

  function init() {
    const rejectModalEl = document.getElementById("rejectModal");

  if (rejectModalEl) {
    els.rejectModal = new bootstrap.Modal(rejectModalEl);
  }

  if (els.rejectBtn) {
    els.rejectBtn.addEventListener("click", submitRejection);
  }

  if (els.searchInput) {
    els.searchInput.addEventListener("keyup", filterTable);
  }

  const refreshBtn = document.getElementById("refreshBtn");
  if (refreshBtn) {
    refreshBtn.addEventListener("click", loadRequests);
  }
  const toastEl = document.getElementById("statusToast");
	if (toastEl) {
	  els.toast = new bootstrap.Toast(toastEl);
	}
    loadRequests();
  }

  function showToast(msg, type = "success") {
    els.toastMsg.textContent = msg;
    els.toastEl.className = `toast align-items-center text-white border-0 bg-${type}`;
    if (els.toast) els.toast.show();
  }

  async function loadRequests() {
    els.tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4"><div class="spinner-border spinner-border-sm me-2"></div>Loading...</td></tr>`;
    els.noResultsInfo.classList.add("d-none");
    els.searchInput.value = "";

    try {
      const response = await axios.get("/department-requests/controller/all");
      state.requests = response.data || [];
      renderTable(state.requests);
    } catch (err) {
      console.error(err);
      els.tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">Failed to load requests.</td></tr>`;
    }
  }

  function renderTable(data) {
    if (!data || data.length === 0) {
      els.tbody.replaceChildren();

      if (state.requests.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");

        td.colSpan = 6;
        td.className = "text-center py-5 text-muted";

        const h5 = document.createElement("h5");
        h5.textContent = "No Requests Found";

        const p = document.createElement("p");
        p.textContent = "There are no department requests in the system.";

        td.appendChild(h5);
        td.appendChild(p);
        tr.appendChild(td);
        els.tbody.appendChild(tr);
      } else {
        els.noResultsInfo.classList.remove("d-none");
      }
      return;
    }

    els.noResultsInfo.classList.add("d-none");
    els.tbody.replaceChildren();

    data.forEach((r) => {
      const tr = document.createElement("tr");
      tr.id = `row-${r.requestId}`;

      // ✅ CSS-based styling (no inline style)
      if (r.status === "APPROVED") tr.classList.add("row-approved");
      if (r.status === "REJECTED") tr.classList.add("row-rejected");

      const td1 = document.createElement("td");
      td1.className = "fw-bold text-primary";
      td1.textContent = r.instituteName || "Unknown Institute";

      const td2 = document.createElement("td");
      td2.textContent = r.departmentName;

      const td3 = document.createElement("td");
      const badge = document.createElement("span");
      badge.className = "badge bg-light text-dark border";
      badge.textContent = r.departmentCode || "-";
      td3.appendChild(badge);

      const td4 = document.createElement("td");
      if (r.hodName) {
        td4.textContent = r.hodName;
      } else {
        const span = document.createElement("span");
        span.className = "text-muted";
        span.textContent = "-";
        td4.appendChild(span);
      }

      const td5 = document.createElement("td");
      td5.className = "small text-muted";
      td5.textContent = r.createdAt
        ? new Date(r.createdAt).toLocaleDateString()
        : "";

      const td6 = document.createElement("td");
      td6.className = "text-center";
      td6.id = `action-cell-${r.requestId}`;

      const actions = renderActions(r);
      td6.appendChild(actions);

      tr.append(td1, td2, td3, td4, td5, td6);
      els.tbody.appendChild(tr);
    });
  }

  function filterTable() {
    const searchTerm = els.searchInput.value.toLowerCase();

    const filteredData = state.requests.filter((item) => {
      return (
        (item.instituteName &&
          item.instituteName.toLowerCase().includes(searchTerm)) ||
        (item.departmentName &&
          item.departmentName.toLowerCase().includes(searchTerm)) ||
        (item.departmentCode &&
          item.departmentCode.toLowerCase().includes(searchTerm)) ||
        (item.status && item.status.toLowerCase().includes(searchTerm))
      );
    });

    renderTable(filteredData);
  }

  function renderActions(request) {
    const container = document.createElement("div");

    if (request.status === "PENDING") {
      const approveBtn = document.createElement("button");
      approveBtn.className = "btn btn-sm btn-success me-2";
      approveBtn.innerHTML = `<i class="fas fa-check me-1"></i> Approve`;
      approveBtn.addEventListener("click", () => {
        DepartmentApprovalsModule.approve(request.requestId);
      });

      const rejectBtn = document.createElement("button");
      rejectBtn.className = "btn btn-sm btn-danger";
      rejectBtn.innerHTML = `<i class="fas fa-times me-1"></i> Reject`;
      rejectBtn.addEventListener("click", () => {
        DepartmentApprovalsModule.openReject(
          request.requestId,
          request.departmentName
        );
      });

      container.appendChild(approveBtn);
      container.appendChild(rejectBtn);
    } else if (request.status === "APPROVED") {
      const span = document.createElement("span");
      span.className = "badge bg-success px-3 py-2";
      span.textContent = "Approved";
      container.appendChild(span);
    } else if (request.status === "REJECTED") {
      const span = document.createElement("span");
      span.className = "badge bg-danger px-3 py-2";
      span.textContent = "Rejected";
      container.appendChild(span);
    } else {
      const span = document.createElement("span");
      span.className = "badge bg-secondary";
      span.textContent = request.status;
      container.appendChild(span);
    }

    return container;
  }

  async function approve(requestId) {
    if (!confirm("Are you sure you want to approve this department?")) return;

    try {
      await axios.post(
        `/department-requests/controller/${requestId}/approve`,
        {},
        { headers: { ...getCsrfHeaders() } }
      );

      showToast("Department Approved Successfully!");

      const reqIndex = state.requests.findIndex(
        (r) => r.requestId === requestId
      );
      if (reqIndex > -1) {
        state.requests[reqIndex].status = "APPROVED";
      }

      filterTable();

      if (typeof GlobalNotifications !== "undefined")
        GlobalNotifications.refresh();
    } catch (err) {
      console.error(err);
      showToast(err.response?.data?.error || "Approval failed", "danger");
    }
  }

  function openReject(requestId, name) {
    state.rejectingId = requestId;
    if (els.rejectNameSpan) els.rejectNameSpan.textContent = name;
    if (els.rejectReason) els.rejectReason.value = "";

    // ✅ FIX: safe modal call
    if (els.rejectModal) {
      els.rejectModal.show();
    }
  }

  async function submitRejection() {
    const reason = els.rejectReason.value.trim();
    if (!reason) {
      alert("Please provide a rejection reason.");
      return;
    }

    els.rejectBtn.disabled = true;

    try {
      const formData = new FormData();
      formData.append("reason", reason);

      await axios.post(
        `/department-requests/controller/${state.rejectingId}/reject`,
        formData,
        { headers: { ...getCsrfHeaders() } }
      );

      showToast("Request Rejected.", "warning");

      if (els.rejectModal) {
        els.rejectModal.hide();
      }

      const reqIndex = state.requests.findIndex(
        (r) => r.requestId === state.rejectingId
      );
      if (reqIndex > -1) {
        state.requests[reqIndex].status = "REJECTED";
      }

      filterTable();

      if (typeof GlobalNotifications !== "undefined")
        GlobalNotifications.refresh();
    } catch (err) {
      console.error(err);
      showToast("Rejection failed", "danger");
    } finally {
      els.rejectBtn.disabled = false;
    }
  }

  return {
    init,
    loadRequests,
    approve,
    openReject,
    filterTable,
  };
})();

document.addEventListener("DOMContentLoaded", DepartmentApprovalsModule.init);