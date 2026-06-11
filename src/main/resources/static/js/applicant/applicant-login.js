document.addEventListener('DOMContentLoaded', function () {

    const sendBtn = document.getElementById('sendOtpBtn');
    const identifierInput = document.getElementById('identifier');

    if (!sendBtn || !identifierInput) {
        console.error("Required elements not found in DOM");
        return;
    }

    sendBtn.addEventListener('click', async function (e) {
        e.preventDefault();

        const identifierValue = identifierInput.value.trim();

        if (!identifierValue) {
            alert("Please enter your application number / email / mobile first.");
            return;
        }

        const csrfInput = document.querySelector('input[name="_csrf"]');
        const csrfToken = csrfInput ? csrfInput.value : '';

        const successDiv = document.getElementById("successMsg");
        const errorDiv = document.getElementById("errorMsg");
        const debugDiv = document.getElementById("otpDebugDiv");
        const debugSpan = document.getElementById("otpDebugValue");

        // Hide all messages first
        if (successDiv) successDiv.style.display = "none";
        if (errorDiv) errorDiv.style.display = "none";
        if (debugDiv) debugDiv.style.display = "none";

        try {

            sendBtn.disabled = true;
            sendBtn.innerText = "Sending...";

            const response = await fetch("/otp/send-otp", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-TOKEN": csrfToken
                },
                body: JSON.stringify({ identifier: identifierValue,purpose: "APPLICANT_LOGIN" })  // ✅ FIXED
            });

            const data = await response.json();

            if (data?.status === "OTP_SENT") {

                if (successDiv) {
                    successDiv.textContent = "OTP sent successfully.";
                    successDiv.style.display = "block";
                }

                if (data.otpDebug && debugDiv && debugSpan) {
                    debugSpan.textContent = data.otpDebug;
                    debugDiv.style.display = "block";
                }
                
                const hiddenUsername = document.getElementById("loginUsernameHidden");
    			if (hiddenUsername) {
       				 hiddenUsername.value = identifierValue;
    			}

            } else {

                if (errorDiv) {
                    errorDiv.textContent = data.message || "Failed to send OTP.";
                    errorDiv.style.display = "block";
                }
            }

        } catch (error) {

            console.error("OTP Error:", error);

            if (errorDiv) {
                errorDiv.textContent = "Something went wrong. Please try again.";
                errorDiv.style.display = "block";
            }

        } finally {

            sendBtn.disabled = false;
            sendBtn.innerText = "Send OTP";
        }
    });

});

 
  