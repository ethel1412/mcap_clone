const ApprovalsModule = (() => {

  const getCsrfHeaders = () => {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    return token && header ? { [header]: token } : {};
  };

  const state = {
    requests: [],
    rejectingId: null,
  };

  const els = {
    tbody: null,
    searchInput: null,
    noResultsInfo: null,
    rejectModal: null,
    rejectBtn: null,
    rejectReason: null,
    toast: null,
    toastMsg: null,
    toastEl: null,
  };

  function init() {

    // 🔥 Initialize DOM AFTER load
    els.tbody = document.getElementById("approvalTableBody");
    els.searchInput = document.getElementById("tableSearchInput");
    els.noResultsInfo = document.getElementById("noSearchResults");
    els.rejectBtn = document.getElementById("confirmRejectBtn");
    els.rejectReason = document.getElementById("rejectionReason");
    els.toastMsg = document.getElementById("toastMessage");
    els.toastEl = document.getElementById("statusToast");

    // 🔥 SAFE Bootstrap init
    if (typeof bootstrap !== "undefined") {
      const modalEl = document.getElementById("rejectModal");
      if (modalEl) els.rejectModal = new bootstrap.Modal(modalEl);

      if (els.toastEl) els.toast = new bootstrap.Toast(els.toastEl);
    }

    if (els.rejectBtn) els.rejectBtn.addEventListener("click", submitRejection);
    if (els.searchInput) els.searchInput.addEventListener("keyup", filterTable);

    loadRequests();
  }

  function showToast(msg, type = "success") {
    if (!els.toast) return;
    els.toastMsg.textContent = msg;
    els.toastEl.className = `toast align-items-center text-white border-0 bg-${type}`;
    els.toast.show();
  }

  async function loadRequests() {

    if (!els.tbody) return;

    els.tbody.innerHTML =
      `<tr><td colspan="6" class="text-center py-4">
        <div class="spinner-border spinner-border-sm me-2"></div>Loading...
      </td></tr>`;

    els.noResultsInfo.classList.add("d-none");
    if (els.searchInput) els.searchInput.value = "";

    try {
      const response = await axios.get("/programme-requests/admin/all");
      state.requests = response.data || [];
      renderTable(state.requests);
    } catch (err) {
      console.error(err);
      els.tbody.innerHTML =
        `<tr><td colspan="6" class="text-center text-danger py-4">
          Failed to load requests.
        </td></tr>`;
    }
  }

  function renderTable(data) {

    if (!data || data.length === 0) {
      els.tbody.innerHTML = "";

      if (state.requests.length === 0) {
        els.tbody.innerHTML =
          `<tr><td colspan="6" class="text-center py-5 text-muted">
            <h5>No Requests Found</h5>
            <p>There are no programme requests in the system.</p>
          </td></tr>`;
      } else {
        els.noResultsInfo.classList.remove("d-none");
      }
      return;
    }

    els.noResultsInfo.classList.add("d-none");
    els.tbody.innerHTML = "";

    data.forEach((r) => {

      const tr = document.createElement("tr");

      if (r.status === "APPROVED") tr.style.backgroundColor = "#f0fff4";
      if (r.status === "REJECTED") tr.style.backgroundColor = "#fff5f5";

      tr.innerHTML = `
        <td class="fw-bold text-primary">${r.instituteName || "Unknown Institute"}</td>
        <td>${r.programmeName}</td>
        <td><span class="badge bg-info text-dark">${r.programmeLevel}</span></td>
        <td><span class="badge bg-secondary">${r.streamName}</span></td>
        <td class="small text-muted">${new Date(r.createdAt).toLocaleDateString()}</td>
        <td class="text-center"></td>
      `;

      const actionTd = tr.querySelector("td:last-child");

      const actions = renderActions(r);
      actionTd.appendChild(actions);

      els.tbody.appendChild(tr);
    });
  }

  function renderActions(request) {

    const container = document.createElement("div");

    if (request.status === "PENDING") {

      const approveBtn = document.createElement("button");
      approveBtn.className = "btn btn-sm btn-success me-2";
      approveBtn.innerHTML = `<i class="fas fa-check me-1"></i> Approve`;
      approveBtn.addEventListener("click", () => approve(request.requestId));

      const rejectBtn = document.createElement("button");
      rejectBtn.className = "btn btn-sm btn-danger";
      rejectBtn.innerHTML = `<i class="fas fa-times me-1"></i> Reject`;
      rejectBtn.addEventListener("click", () =>
        openReject(request.requestId, request.programmeName)
      );

      container.appendChild(approveBtn);
      container.appendChild(rejectBtn);

    } else if (request.status === "APPROVED") {
      container.innerHTML = `<span class="badge bg-success px-3 py-2">Approved</span>`;
    } else if (request.status === "REJECTED") {
      container.innerHTML = `<span class="badge bg-danger px-3 py-2">Rejected</span>`;
    }

    return container;
  }

  function filterTable() {
    const searchTerm = els.searchInput.value.toLowerCase();

    const filtered = state.requests.filter((item) =>
      (item.instituteName || "").toLowerCase().includes(searchTerm) ||
      (item.programmeName || "").toLowerCase().includes(searchTerm) ||
      (item.streamName || "").toLowerCase().includes(searchTerm) ||
      (item.status || "").toLowerCase().includes(searchTerm)
    );

    renderTable(filtered);
  }

  async function approve(requestId) {
    if (!confirm("Are you sure you want to approve this programme?")) return;

    try {
      await axios.post(`/programme-requests/admin/${requestId}/approve`, {}, {
        headers: { ...getCsrfHeaders() }
      });

      showToast("Programme Approved Successfully!");

      const req = state.requests.find(r => r.requestId === requestId);
      if (req) req.status = "APPROVED";

      filterTable();

    } catch (err) {
      console.error(err);
      showToast("Approval failed", "danger");
    }
  }

  function openReject(requestId, name) {
    state.rejectingId = requestId;
    document.getElementById("rejectProgrammeName").textContent = name;
    els.rejectReason.value = "";
    if (els.rejectModal) els.rejectModal.show();
  }

  async function submitRejection() {
    const reason = els.rejectReason.value.trim();
    if (!reason) return alert("Provide rejection reason");

    try {
      await axios.post(`/programme-requests/admin/${state.rejectingId}/reject`,
        new FormData().append("reason", reason),
        { headers: { ...getCsrfHeaders() } }
      );

      showToast("Request Rejected", "warning");
      if (els.rejectModal) els.rejectModal.hide();

      const req = state.requests.find(r => r.requestId === state.rejectingId);
      if (req) req.status = "REJECTED";

      filterTable();

    } catch (err) {
      console.error(err);
      showToast("Rejection failed", "danger");
    }
  }

  return { init, loadRequests, approve, openReject, filterTable };

})();

document.addEventListener("DOMContentLoaded", () => {
  try {
    ApprovalsModule.init();
  } catch (e) {
    console.error("Module crashed:", e);
  }
});