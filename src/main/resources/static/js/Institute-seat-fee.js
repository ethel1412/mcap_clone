/*
 * Institute Seat Acceptance Fee Management
 * - Numeric IDs used for API calls; ownership enforced server-side by userId
 * - CSRF token read from layout meta tags and sent as request header on all mutations
 * - All DOM manipulation via this file only; no inline scripts
 */

(function () {
    'use strict';

    /* ---- CSRF helpers (meta tags injected by Thymeleaf layout) ---- */
    function csrfToken() {
        return document.querySelector('meta[name="_csrf"]')?.content || '';
    }
    function csrfHeader() {
        return document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    }
    function authHeaders(contentType) {
        const h = {};
        const tok = csrfToken();
        const hdr = csrfHeader();
        if (tok && hdr) h[hdr] = tok;
        if (contentType) h['Content-Type'] = contentType;
        return h;
    }

    /* ---- State ---- */
    const BASE = '/institute/seat-fee';
    let allProgrammes = [];
    let allStreams = [];

    /* ---- Utility ---- */
    function escHtml(str) {
        return String(str == null ? '' : str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function formatINR(val) {
        return '\u20b9 ' + parseFloat(val || 0).toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function showAlert(msg, type) {
        type = type || 'success';
        const el = document.getElementById('alertContainer');
        if (!el) return;
        el.innerHTML = '<div class="alert alert-' + type +
            ' alert-dismissible fade show" role="alert">' +
            escHtml(msg) +
            '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>';
    }

    function clearErrors() {
        ['feeNameError', 'particularsError', 'programmeScopeError', 'streamScopeError']
            .forEach(function (id) {
                const el = document.getElementById(id);
                if (el) el.textContent = '';
            });
    }

    /* ---- Particulars row management ---- */
    function addParticularRow(name, amount) {
        name = name || '';
        amount = (amount !== undefined && amount !== null) ? amount : '';
        const tbody = document.getElementById('particularsBody');
        const tr = document.createElement('tr');

        const nameTd = document.createElement('td');
        const nameInput = document.createElement('input');
        nameInput.type = 'text';
        nameInput.className = 'form-control form-control-sm particular-name particulars-row-input';
        nameInput.placeholder = 'e.g. Tuition Fee';
        nameInput.maxLength = 200;
        nameInput.value = name;
        nameTd.appendChild(nameInput);

        const amtTd = document.createElement('td');
        const amtInput = document.createElement('input');
        amtInput.type = 'number';
        amtInput.className = 'form-control form-control-sm particular-amount particulars-row-input';
        amtInput.placeholder = '0.00';
        amtInput.min = '0';
        amtInput.step = '0.01';
        amtInput.value = amount;
        amtInput.addEventListener('input', recalcTotal);
        amtTd.appendChild(amtInput);

        const btnTd = document.createElement('td');
        btnTd.className = 'text-center';
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn btn-sm btn-outline-danger remove-row-btn';
        btn.setAttribute('aria-label', 'Remove row');
        btn.innerHTML = '<i class="bi bi-dash"></i>';
        btn.addEventListener('click', function () {
            tr.remove();
            recalcTotal();
        });
        btnTd.appendChild(btn);

        tr.appendChild(nameTd);
        tr.appendChild(amtTd);
        tr.appendChild(btnTd);
        tbody.appendChild(tr);
    }

    function recalcTotal() {
        const amounts = Array.from(document.querySelectorAll('.particular-amount'))
            .map(function (i) { return parseFloat(i.value) || 0; });
        const total = amounts.reduce(function (a, b) { return a + b; }, 0);
        const cell = document.getElementById('totalAmountCell');
        if (cell) cell.textContent = formatINR(total);
    }

    /* ---- Scope radio toggle ---- */
    function bindScopeRadios() {
        document.querySelectorAll('input[name="scopeType"]').forEach(function (radio) {
            radio.addEventListener('change', function () {
                const isProg = document.getElementById('scopeProgramme').checked;
                document.getElementById('programmeScopeDiv').hidden = !isProg;
                document.getElementById('streamScopeDiv').hidden = isProg;
            });
        });
    }

    /* ---- Populate selects ---- */
    function populateProgrammeSelect(selectedIds) {
        selectedIds = (selectedIds || []).map(Number);
        const sel = document.getElementById('programmeSelect');
        sel.innerHTML = '';
        allProgrammes.forEach(function (p) {
            const opt = document.createElement('option');
            opt.value = p.id;
            opt.textContent = p.name + ' (' + (p.streamName || '') + ')';
            if (selectedIds.indexOf(Number(p.id)) !== -1) opt.selected = true;
            sel.appendChild(opt);
        });
    }

    function populateStreamSelect(selectedIds) {
        selectedIds = (selectedIds || []).map(Number);
        const sel = document.getElementById('streamSelect');
        sel.innerHTML = '';
        allStreams.forEach(function (s) {
            const opt = document.createElement('option');
            opt.value = s.id;
            opt.textContent = s.name;
            if (selectedIds.indexOf(Number(s.id)) !== -1) opt.selected = true;
            sel.appendChild(opt);
        });
    }

    /* ---- Reset modal ---- */
    function resetModal() {
        document.getElementById('editFeeToken').value = '';
        document.getElementById('feeName').value = '';
        document.getElementById('modalTitleText').textContent = 'Add Fee Structure';
        document.getElementById('particularsBody').innerHTML = '';
        const totalCell = document.getElementById('totalAmountCell');
        if (totalCell) totalCell.textContent = formatINR(0);
        document.getElementById('scopeProgramme').checked = true;
        document.getElementById('programmeScopeDiv').hidden = false;
        document.getElementById('streamScopeDiv').hidden = true;
        populateProgrammeSelect();
        populateStreamSelect();
        clearErrors();
        addParticularRow();
    }

    /* ---- Load & render table ---- */
    function loadStructures() {
        fetch(BASE + '/list', { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                renderTable(data.data || []);
            })
            .catch(function () { showAlert('Failed to load fee structures.', 'danger'); });
    }

    function renderTable(structures) {
        const tbody = document.getElementById('feeStructuresTableBody');
        const empty = document.getElementById('emptyState');
        tbody.innerHTML = '';
        if (!structures.length) {
            empty.hidden = false;
            return;
        }
        empty.hidden = true;
        structures.forEach(function (s, i) {
            const tr = document.createElement('tr');

            // #
            const tdNum = document.createElement('td');
            tdNum.textContent = String(i + 1);
            tr.appendChild(tdNum);

            // Name
            const tdName = document.createElement('td');
            tdName.className = 'fw-semibold';
            tdName.textContent = s.feeName;
            tr.appendChild(tdName);

            // Total
            const tdTotal = document.createElement('td');
            tdTotal.className = 'total-amount-cell';
            tdTotal.textContent = formatINR(s.totalAmount);
            tr.appendChild(tdTotal);

            // Count
            const tdCount = document.createElement('td');
            const badge = document.createElement('span');
            badge.className = 'badge bg-primary';
            badge.textContent = String((s.particulars || []).length);
            tdCount.appendChild(badge);
            tr.appendChild(tdCount);

            // Scope
            const tdScope = document.createElement('td');
            const small = document.createElement('small');
            small.className = 'text-muted scope-badge';
            small.textContent = s.scopeSummary || '—';
            tdScope.appendChild(small);
            tr.appendChild(tdScope);

            // Actions — use numeric feeStructureId (ownership enforced server-side by userId)
            const feeId = s.feeStructureId;
            const tdAct = document.createElement('td');
            const viewBtn = document.createElement('button');
            viewBtn.className = 'btn btn-sm btn-outline-secondary me-1';
            viewBtn.title = 'View';
            viewBtn.setAttribute('aria-label', 'View fee structure ' + escHtml(s.feeName));
            viewBtn.innerHTML = '<i class="bi bi-eye"></i>';
            viewBtn.addEventListener('click', function () { viewStructure(feeId); });

            const editBtn = document.createElement('button');
            editBtn.className = 'btn btn-sm btn-outline-primary me-1';
            editBtn.title = 'Edit';
            editBtn.setAttribute('aria-label', 'Edit fee structure ' + escHtml(s.feeName));
            editBtn.innerHTML = '<i class="bi bi-pencil"></i>';
            editBtn.addEventListener('click', function () { editStructure(feeId); });

            const delBtn = document.createElement('button');
            delBtn.className = 'btn btn-sm btn-outline-danger';
            delBtn.title = 'Delete';
            delBtn.setAttribute('aria-label', 'Delete fee structure ' + escHtml(s.feeName));
            delBtn.innerHTML = '<i class="bi bi-trash"></i>';
            delBtn.addEventListener('click', function () { openDeleteConfirm(feeId, s.feeName); });

            tdAct.appendChild(viewBtn);
            tdAct.appendChild(editBtn);
            tdAct.appendChild(delBtn);
            tr.appendChild(tdAct);

            tbody.appendChild(tr);
        });
    }

    /* ---- Save ---- */
    function saveStructure() {
        clearErrors();
        const feeName = (document.getElementById('feeName').value || '').trim();
        if (!feeName) {
            document.getElementById('feeNameError').textContent = 'Fee structure name is required.';
            return;
        }

        const particulars = [];
        let rowsValid = true;
        document.querySelectorAll('#particularsBody tr').forEach(function (tr, idx) {
            const name = tr.querySelector('.particular-name').value.trim();
            const amt = parseFloat(tr.querySelector('.particular-amount').value);
            if (!name || isNaN(amt) || amt < 0) { rowsValid = false; return; }
            particulars.push({ particularName: name, amount: amt, displayOrder: idx });
        });
        if (!rowsValid || !particulars.length) {
            document.getElementById('particularsError').textContent =
                'Every row must have a description and a valid amount (\u2265 0).';
            return;
        }

        const scopeType = document.querySelector('input[name="scopeType"]:checked').value;
        let programmeTokens = null;
        let streamTokens = null;
        if (scopeType === 'programme') {
            programmeTokens = Array.from(document.getElementById('programmeSelect').selectedOptions)
                .map(function (o) { return o.value; });
            if (!programmeTokens.length) {
                document.getElementById('programmeScopeError').textContent = 'Select at least one programme.';
                return;
            }
        } else {
            streamTokens = Array.from(document.getElementById('streamSelect').selectedOptions)
                .map(function (o) { return o.value; });
            if (!streamTokens.length) {
                document.getElementById('streamScopeError').textContent = 'Select at least one stream.';
                return;
            }
        }

        const editId = (document.getElementById('editFeeToken').value || '').trim() || null;
        const payload = {
            feeStructureId: editId ? Number(editId) : null,
            feeName: feeName,
            particulars: particulars,
            programmeOfferedIds: programmeTokens ? programmeTokens.map(Number) : null,
            streamIds: streamTokens ? streamTokens.map(Number) : null
        };

        fetch(BASE + '/save', {
            method: 'POST',
            headers: authHeaders('application/json'),
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) {
                    showAlert(data.message || 'Save failed.', 'danger');
                    return;
                }
                bootstrap.Modal.getInstance(document.getElementById('feeModal')).hide();
                showAlert('Fee structure saved successfully.');
                loadStructures();
            })
            .catch(function () { showAlert('Network error. Please try again.', 'danger'); });
    }

    /* ---- Edit ---- */
    function editStructure(feeId) {
        fetch(BASE + '/' + encodeURIComponent(feeId), { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) { showAlert('Could not load fee structure.', 'danger'); return; }
                const s = data.data;
                resetModal();
                document.getElementById('editFeeToken').value = s.feeStructureId;
                document.getElementById('feeName').value = s.feeName;
                document.getElementById('modalTitleText').textContent = 'Edit Fee Structure';
                document.getElementById('particularsBody').innerHTML = '';
                (s.particulars || []).forEach(function (p) { addParticularRow(p.particularName, p.amount); });
                recalcTotal();

                const hasStream = (s.scopes || []).some(function (sc) { return sc.scopeType === 'STREAM'; });
                if (hasStream) {
                    document.getElementById('scopeStream').checked = true;
                    document.getElementById('programmeScopeDiv').hidden = true;
                    document.getElementById('streamScopeDiv').hidden = false;
                    const selIds = (s.scopes || [])
                        .filter(function (sc) { return sc.scopeType === 'STREAM'; })
                        .map(function (sc) { return sc.streamId; });
                    populateStreamSelect(selIds);
                } else {
                    document.getElementById('scopeProgramme').checked = true;
                    document.getElementById('programmeScopeDiv').hidden = false;
                    document.getElementById('streamScopeDiv').hidden = true;
                    const selIds = (s.scopes || [])
                        .filter(function (sc) { return sc.scopeType === 'PROGRAMME'; })
                        .map(function (sc) { return sc.programmeOfferedId; });
                    populateProgrammeSelect(selIds);
                }
                new bootstrap.Modal(document.getElementById('feeModal')).show();
            })
            .catch(function () { showAlert('Network error.', 'danger'); });
    }

    /* ---- View ---- */
    function viewStructure(feeId) {
        fetch(BASE + '/' + encodeURIComponent(feeId), { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) { showAlert('Could not load details.', 'danger'); return; }
                const s = data.data;
                const tbody = document.getElementById('viewParticularsBody');
                tbody.innerHTML = '';
                (s.particulars || []).forEach(function (p) {
                    const tr = document.createElement('tr');
                    const tdName = document.createElement('td');
                    tdName.textContent = p.particularName;
                    const tdAmt = document.createElement('td');
                    tdAmt.className = 'text-end fw-semibold';
                    tdAmt.textContent = formatINR(p.amount);
                    tr.appendChild(tdName);
                    tr.appendChild(tdAmt);
                    tbody.appendChild(tr);
                });
                const totalEl = document.getElementById('viewTotalAmount');
                if (totalEl) totalEl.textContent = formatINR(s.totalAmount);
                const titleEl = document.getElementById('viewModalFeeName');
                if (titleEl) titleEl.textContent = s.feeName;
                const scopeEl = document.getElementById('viewModalScopeText');
                if (scopeEl) scopeEl.textContent = s.scopeSummary || '—';
                new bootstrap.Modal(document.getElementById('viewModal')).show();
            })
            .catch(function () { showAlert('Network error.', 'danger'); });
    }

    /* ---- Delete ---- */
    let pendingDeleteToken = null;

    function openDeleteConfirm(token, name) {
        pendingDeleteToken = token;
        const nameEl = document.getElementById('deleteFeeName');
        if (nameEl) nameEl.textContent = name;
        new bootstrap.Modal(document.getElementById('deleteConfirmModal')).show();
    }

    function confirmDelete() {
        if (!pendingDeleteToken) return;
        fetch(BASE + '/' + encodeURIComponent(pendingDeleteToken), {
            method: 'DELETE',
            headers: authHeaders(null),
            credentials: 'same-origin'
        })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal')).hide();
                pendingDeleteToken = null;
                if (!data.success) { showAlert(data.message || 'Delete failed.', 'danger'); return; }
                showAlert('Fee structure deleted.');
                loadStructures();
            })
            .catch(function () { showAlert('Network error.', 'danger'); });
    }

    /* ---- Init ---- */
    document.addEventListener('DOMContentLoaded', function () {
        bindScopeRadios();

        document.getElementById('addRowBtn').addEventListener('click', function () { addParticularRow(); });
        document.getElementById('openAddModalBtn').addEventListener('click', function () {
            resetModal();
            new bootstrap.Modal(document.getElementById('feeModal')).show();
        });
        document.getElementById('saveFeeStructureBtn').addEventListener('click', saveStructure);
        document.getElementById('confirmDeleteBtn').addEventListener('click', confirmDelete);

        document.getElementById('feeModal').addEventListener('hidden.bs.modal', function () { clearErrors(); });

        Promise.all([
            fetch(BASE + '/programmes', { credentials: 'same-origin' }).then(function (r) { return r.json(); }),
            fetch(BASE + '/streams', { credentials: 'same-origin' }).then(function (r) { return r.json(); })
        ]).then(function (results) {
            allProgrammes = (results[0].data || []);
            allStreams = (results[1].data || []);
            populateProgrammeSelect();
            populateStreamSelect();
            loadStructures();
        }).catch(function () {
            showAlert('Failed to load dropdown data.', 'danger');
            loadStructures();
        });
    });

}());