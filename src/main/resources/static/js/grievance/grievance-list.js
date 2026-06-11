document.addEventListener('DOMContentLoaded', () => {
    const modalElement = document.getElementById('grievanceDetailModal');
    if (!modalElement) return;

    const modal = new bootstrap.Modal(modalElement);
    const body  = document.getElementById('grievanceDetailBody');
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

    document.querySelectorAll('.grievance-row').forEach(row => {
        row.addEventListener('click', function (e) {
            e.preventDefault();
            const id = this.dataset.id;
            body.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>';
            modal.show();

            fetch('/grievances/' + id + '/detail', { credentials: 'same-origin' })
                .then(r => r.json())
                .then(g => {
                    const inst = g.institute ? `<tr><th class="table-light">Concerned Institute</th><td>${g.institute}</td></tr>` : '';
                    const statusBadge = g.status === 'OPEN'
                        ? '<span class="badge bg-warning text-dark">OPEN</span>'
                        : '<span class="badge bg-success">RESOLVED</span>';

                    // Determine what the status toggle button should say
                    const targetStatus = g.status === 'OPEN' ? 'RESOLVED' : 'OPEN';
                    const btnClass = g.status === 'OPEN' ? 'btn-success' : 'btn-warning text-dark';
                    const btnText = g.status === 'OPEN' ? '<i class="bi bi-check-circle me-1"></i>Mark as Resolved' : '<i class="bi bi-arrow-counterclockwise me-1"></i>Reopen Ticket';

                    body.innerHTML = `
                    <table class="table table-bordered table-sm mb-4">
                      <tbody>
                        <tr><th class="table-light w-40">Ticket Code</th><td><span class="font-monospace fw-bold text-primary">${g.ticketCode}</span></td></tr>
                        <tr><th class="table-light">Submitted By</th><td>${g.submittedBy}</td></tr>
                        <tr><th class="table-light">Category</th><td>${g.category}</td></tr>
                        <tr><th class="table-light">Submitted At</th><td>${new Date(g.submittedAt).toLocaleString('en-IN')}</td></tr>
                        <tr><th class="table-light">Status</th><td>${statusBadge}</td></tr>
                        ${inst}
                      </tbody>
                    </table>
                    <div class="card border-0 bg-light mb-3">
                      <div class="card-header fw-bold bg-white border-bottom"><i class="bi bi-chat-left-text me-2 text-primary"></i>Message</div>
                      <div class="card-body"><p class="mb-0 text-pre-wrap">${g.message}</p></div>
                    </div>
                    <div class="text-end border-top pt-3">
                        <button class="btn btn-sm ${btnClass} status-toggle-btn" data-id="${g.id}" data-status="${targetStatus}">
                            ${btnText}
                        </button>
                    </div>`;
                })
                .catch(() => { body.innerHTML = '<div class="alert alert-danger">Failed to load grievance details.</div>'; });
        });
    });

    // Handle Status Toggle Button Clicks securely via Event Delegation
    body.addEventListener('click', function(e) {
        const btn = e.target.closest('.status-toggle-btn');
        if (!btn) return;

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Updating...';

        fetch(`/grievances/${btn.dataset.id}/status`, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ status: btn.dataset.status })
        })
        .then(r => r.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                btn.disabled = false;
            } else {
                // Reload the page to reflect the new status in the background list
                window.location.reload();
            }
        })
        .catch(() => {
            alert("Network error occurred.");
            btn.disabled = false;
        });
    });
});