/**
 * payment-bridge.js  —  Razorpay Standard Checkout integration
 *
 * What it does:
 *  1. Reads the Razorpay order_id, key_id, and internal receipt_id from data-* attributes
 *     on the hidden #payBtn element (set by Thymeleaf in make-payment.html).
 *  2. Opens the Razorpay checkout modal automatically.
 *  3. On success, the Razorpay handler callback populates the hidden form fields
 *     (razorpay_payment_id, razorpay_order_id, razorpay_signature) and submits
 *     the form to POST /applicants/payment/payment-callback for server-side verification.
 *  4. On modal dismiss (user closes without paying), redirects to the dashboard.
 */
document.addEventListener("DOMContentLoaded", function () {
    const payBtn = document.getElementById("payBtn");
    if (!payBtn) return;

    const razorpayOrderId = payBtn.getAttribute("data-razorpay-order-id");
    const keyId           = payBtn.getAttribute("data-key-id");
    const receiptId       = payBtn.getAttribute("data-receipt-id");

    // Guard: abort early if required params are missing
    if (!razorpayOrderId || razorpayOrderId === "null" ||
        !keyId           || keyId === "null") {
        console.error("[payment-bridge] Missing Razorpay order ID or key ID.");
        alert("Payment session could not be initialised. Please try again.");
        return;
    }

    if (typeof window.Razorpay !== "function") {
        console.error("[payment-bridge] Razorpay SDK not loaded.");
        alert("Payment Gateway failed to load. Please check your internet connection and try again.");
        return;
    }

    // ── Razorpay checkout options ────────────────────────────────────────────
    var options = {
        key:      keyId,
        order_id: razorpayOrderId,
        name:     "MCAP — Medical College Admission Portal",
        description: "Application / Seat Acceptance Fee",
        theme:    { color: "#0d6efd" },

        // Called by Razorpay after a SUCCESSFUL payment — do NOT redirect here;
        // instead populate the hidden form and POST to the backend for signature verification.
        handler: function (response) {
            document.getElementById("rzp_payment_id").value = response.razorpay_payment_id;
            document.getElementById("rzp_order_id").value   = response.razorpay_order_id;
            document.getElementById("rzp_signature").value  = response.razorpay_signature;
            document.getElementById("rzpCallbackForm").submit();
        },

        // Called when the user closes the Razorpay modal without completing payment
        modal: {
            ondismiss: function () {
                console.info("[payment-bridge] Payment modal dismissed by user.");
                window.location.href = "/applicants/dashboard";
            }
        }
    };
    // ────────────────────────────────────────────────────────────────────────

    function openCheckout() {
        try {
            var rzp = new window.Razorpay(options);

            // Surface Razorpay-level errors (e.g. network errors during payment)
            rzp.on("payment.failed", function (response) {
                console.error("[payment-bridge] Payment failed:", response.error);
                alert("Payment failed: " + (response.error.description || "Unknown error") +
                      ". Please try again or contact support.");
                window.location.href = "/applicants/dashboard";
            });

            rzp.open();
        } catch (e) {
            console.error("[payment-bridge] Error opening Razorpay checkout:", e);
            alert("Could not open the payment window. Please try again.");
        }
    }

    // Wire fallback button
    payBtn.addEventListener("click", openCheckout);

    // Auto-open after a short delay, then show the fallback button
    setTimeout(function () {
        openCheckout();
        payBtn.classList.remove("d-none"); // Fallback visible if modal fails to appear
    }, 500);
});
