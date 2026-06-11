document.addEventListener("DOMContentLoaded", function () {
    const refreshBtn = document.getElementById("refreshBtn");
    const statusEl = document.getElementById("statusText");

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    if (!refreshBtn) return;

    const orderId = refreshBtn.getAttribute("data-order-id");

    refreshBtn.addEventListener("click", function () {
        const originalText = refreshBtn.innerText;
        refreshBtn.innerText = "Checking...";
        refreshBtn.disabled = true;

        // Fetch from the updated API endpoint
        fetch(`/applicants/payment/payment-status-api/${orderId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
            }
        })
        .then(response => {
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            const status = data.order_status;
            statusEl.textContent = status;

            // Update classes dynamically
            if (status === "PAID" || status === "SUCCESS") {
                statusEl.className = "text-success fw-bold";
                // Optionally reload the page so Thymeleaf can render the success UI
                location.reload();
            } else if (status === "FAILED") {
                statusEl.className = "text-danger fw-bold";
            } else {
                statusEl.className = "text-warning fw-bold";
            }
        })
        .catch(err => {
            console.error("Error fetching status:", err);
            statusEl.textContent = "Error checking status";
            statusEl.className = "text-danger fw-bold";
        })
        .finally(() => {
            refreshBtn.innerText = originalText;
            refreshBtn.disabled = false;
        });
    });
});