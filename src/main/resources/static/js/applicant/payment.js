export function initializePaymentForm() {

    // 1. Prevent Multiple Form Submissions & Show Spinner on the Pay button
    const paymentForm = document.getElementById('payment-initiation-form');
    if (paymentForm) {
        paymentForm.addEventListener('submit', function() {
            const submitBtn = paymentForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Processing...';
            }
        });
    }

    // 2. Handle Cashfree Checkout on make-payment.html
    const payBtn = document.getElementById("payBtn");
    if (payBtn) {
        const paymentSessionId = payBtn.getAttribute("data-session-id");

        payBtn.addEventListener("click", function () {
            if (!paymentSessionId || paymentSessionId === "null") {
                alert("Missing or invalid payment session ID. Please try again.");
                return;
            }

            if (typeof window.Cashfree !== "function") {
                alert("Payment Gateway failed to load. Please check your internet connection.");
                return;
            }

            try {
                const cashfree = window.Cashfree({
                    mode: "sandbox" // Change to "production" before live
                });

                cashfree.checkout({
                    paymentSessionId: paymentSessionId,
                    redirectTarget: "_self" // Redirects in the same tab
                });
            } catch (e) {
                console.error("Checkout error:", e);
                alert("Error starting payment");
            }
        });
    }
}