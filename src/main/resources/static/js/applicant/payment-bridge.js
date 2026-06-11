document.addEventListener("DOMContentLoaded", function () {
    const payButton = document.getElementById("payBtn");

    if (!payButton) return;

    const paymentSessionId = payButton.getAttribute("data-session-id");

    payButton.addEventListener("click", function () {
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
                mode: "sandbox" // Change to "production" when going live
            });

            cashfree.checkout({
                paymentSessionId: paymentSessionId,
                redirectTarget: "_self"
            });
        } catch (e) {
            console.error("Checkout error:", e);
            alert("Error starting payment");
        }
    });

    // Auto-click the button to launch modal immediately after a small delay
    setTimeout(() => {
        payButton.click();
        payButton.classList.remove("d-none"); // Show button as fallback if popup fails
    }, 500);
});