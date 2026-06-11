document.addEventListener("DOMContentLoaded", function () {
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    const userType = document.getElementById("userType");
    const loginForm = document.getElementById("login-form");
    const loginBtn = document.getElementById("login-submit-btn");
    const dob = document.getElementById("dob-temp");
    const hiddenDob = document.getElementById("dob");
    const dobGroup = document.getElementById("dob-group");
    const captchaInput = document.getElementById("captcha");
    const refreshButton = document.getElementById("refresh-button");

    const loginError = document.getElementById("login-error");
    const loginSuccess = document.getElementById("login-success");

    const otpForm = document.getElementById("otp-form");
    const otpInput = document.getElementById("otp");
    const otpError = document.getElementById("otp-modal-error");
    const otpSuccess = document.getElementById("otp-modal-success");
    const verifyOtpBtn = document.getElementById("verifyOtpBtn");
    const resendOtpBtn = document.getElementById("resendOtpBtn");

    let isSubmitting = false;
    let resendCountdownTimer = null;
    let resendSeconds = 30;

    if (csrfTokenMeta && csrfHeaderMeta && window.axios) {
        axios.defaults.headers.common[csrfHeaderMeta.content] = csrfTokenMeta.content;
        axios.defaults.withCredentials = true;
    }

    window.addEventListener("pageshow", function (event) {
        const navigation = performance.getEntriesByType("navigation");
        if (
            event.persisted ||
            (navigation.length > 0 && navigation[0].type === "back_forward")
        ) {
            window.location.reload();
        }
    });

    function showElement(el) {
        if (el) {
            el.classList.remove("d-none");
        }
    }

    function hideElement(el) {
        if (el) {
            el.classList.add("d-none");
        }
    }

    function setText(el, message) {
        if (el) {
            el.innerText = message || "";
        }
    }

    function clearAlerts() {
        hideElement(loginError);
        hideElement(loginSuccess);
        hideElement(otpError);
        hideElement(otpSuccess);

        setText(loginError, "");
        setText(loginSuccess, "");
        setText(otpError, "");
        setText(otpSuccess, "");
    }

    function showLoginError(message) {
        hideElement(loginSuccess);
        setText(loginError, message || "Login failed");
        showElement(loginError);
    }

    function showLoginSuccess(message) {
        hideElement(loginError);
        setText(loginSuccess, message || "Success");
        showElement(loginSuccess);
    }

    function showOtpError(message) {
        hideElement(otpSuccess);
        setText(otpError, message || "Invalid OTP");
        showElement(otpError);
    }

    function showOtpSuccess(message) {
        hideElement(otpError);
        setText(otpSuccess, message || "OTP verified");
        showElement(otpSuccess);
    }

    function getOtpModal() {
        const otpModalElement = document.getElementById("otpModal");
        let otpModal = bootstrap.Modal.getInstance(otpModalElement);
        if (!otpModal) {
            otpModal = new bootstrap.Modal(otpModalElement, {
                backdrop: "static",
                keyboard: false
            });
        }
        return otpModal;
    }

    function handleUserTypeChange() {
        if (!userType || !dobGroup || !dob) {
            return;
        }

        if (userType.value === "APPLICANT") {
            dobGroup.classList.remove("d-none");
            dob.setAttribute("required", "required");
        } else {
            dobGroup.classList.add("d-none");
            dob.removeAttribute("required");
            dob.value = "";
            if (hiddenDob) {
                hiddenDob.value = "";
            }
        }
    }

    function encryptField(encryptor, fieldId, fieldName) {
        const field = document.getElementById(fieldId);
        if (!field) {
            alert(fieldName + " element not found");
            return false;
        }

        const value = field.value;
        if (!value || value.trim() === "") {
            alert(fieldName + " is required");
            return false;
        }

        const encryptedValue = encryptor.encrypt(value);
        if (!encryptedValue) {
            alert(fieldName + " encryption failed");
            return false;
        }

        if (fieldId === "dob-temp") {
            if (!hiddenDob) {
                alert("Hidden DOB field not found");
                return false;
            }
            hiddenDob.value = encryptedValue;
        } else {
            field.value = encryptedValue;
        }

        return true;
    }

    function restorePasswordIfNeeded() {
        const passwordField = document.getElementById("password");
        if (passwordField && passwordField.dataset.originalValue) {
            passwordField.value = passwordField.dataset.originalValue;
        }
    }

    function resetCaptchaInput() {
        if (captchaInput) {
            captchaInput.value = "";
        }
    }

    function setLoginButtonState(disabled, text) {
        if (!loginBtn) {
            return;
        }
        loginBtn.disabled = disabled;
        loginBtn.innerText = text || "Log In";
    }

    function setVerifyButtonState(disabled, text) {
        if (!verifyOtpBtn) {
            return;
        }
        verifyOtpBtn.disabled = disabled;
        verifyOtpBtn.innerText = text || "Verify OTP";
    }

    function startResendCountdown() {
        if (!resendOtpBtn) {
            return;
        }

        resendSeconds = 30;
        resendOtpBtn.disabled = true;
        resendOtpBtn.innerText = `Resend OTP in ${resendSeconds}s`;

        if (resendCountdownTimer) {
            clearInterval(resendCountdownTimer);
        }

        resendCountdownTimer = setInterval(function () {
            resendSeconds -= 1;

            if (resendSeconds <= 0) {
                clearInterval(resendCountdownTimer);
                resendCountdownTimer = null;
                resendOtpBtn.disabled = false;
                resendOtpBtn.innerText = "Resend OTP";
                return;
            }

            resendOtpBtn.innerText = `Resend OTP in ${resendSeconds}s`;
        }, 1000);
    }

    async function submitLoginForm() {
        if (!loginForm || isSubmitting) {
            return;
        }

        clearAlerts();

        const passwordField = document.getElementById("password");
        const type = userType ? userType.value : "";

        try {
            const publicKey = await getPublicKey();
            if (!publicKey) {
                showLoginError("Unable to load public key");
                return;
            }

            const encryptor = new JSEncrypt();
            encryptor.setPublicKey(publicKey);

            if (passwordField && !passwordField.dataset.originalValue) {
                passwordField.dataset.originalValue = passwordField.value;
            }

            const encryptedPassword = encryptor.encrypt(passwordField.dataset.originalValue);
            if (!encryptedPassword) {
                showLoginError("Password encryption failed");
                return;
            }

            passwordField.value = encryptedPassword;

            if (type === "APPLICANT") {
                if (!dob || !dob.value) {
                    restorePasswordIfNeeded();
                    showLoginError("Please select Date of Birth");
                    return;
                }

                if (!encryptField(encryptor, "dob-temp", "Date of Birth")) {
                    restorePasswordIfNeeded();
                    return;
                }
            } else if (dob) {
                dob.value = "";
                if (hiddenDob) {
                    hiddenDob.value = "";
                }
            }

            isSubmitting = true;
            setLoginButtonState(true, "Logging in...");

            const formData = new FormData(loginForm);
            const response = await axios.post(
                loginForm.action,
                new URLSearchParams(formData),
                {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    }
                }
            );

            const data = response.data || {};

            hideElement(loginError);
            hideElement(loginSuccess);

            if (data.status === "OTP_REQUIRED") {
                isSubmitting = false;
                setLoginButtonState(false, "Log In");
                showLoginSuccess(data.message || "OTP sent successfully");
                if (loginBtn) {
                    loginBtn.disabled = true;
                }
                startResendCountdown();
                getOtpModal().show();
                return;
            }

            if (data.status === "LOGIN_SUCCESS") {
                window.location.replace(data.redirectUrl);
                return;
            }

            isSubmitting = false;
            setLoginButtonState(false, "Log In");
            restorePasswordIfNeeded();
            showLoginError(data.message || "Login failed");
        } catch (error) {
            getCaptcha();
            resetCaptchaInput();
            restorePasswordIfNeeded();

            hideElement(loginSuccess);

            if (
                error.response &&
                error.response.data &&
                error.response.data.status === "OTP_REQUIRED"
            ) {
                isSubmitting = false;
                setLoginButtonState(false, "Log In");
                showLoginSuccess(error.response.data.message || "OTP sent successfully");
                if (loginBtn) {
                    loginBtn.disabled = true;
                }
                startResendCountdown();
                getOtpModal().show();
                return;
            }

            const message =
                error.response?.data?.message || "Invalid credentials";

            showLoginError(message);
            isSubmitting = false;
            setLoginButtonState(false, "Log In");
        }
    }

    async function submitOtpVerification() {
        const otpValue = otpInput ? otpInput.value.trim() : "";

        hideElement(otpError);
        hideElement(otpSuccess);

        if (!/^\d{6}$/.test(otpValue)) {
            showOtpError("Please enter valid 6 digit OTP");
            return;
        }

        setVerifyButtonState(true, "Verifying...");

        try {
            const response = await axios.post("/otp/verify-login-otp", {
                otp: otpValue,
                purpose: "APPLICANT_LOGIN"
            });

            const data = response.data || {};

            hideElement(loginError);
            hideElement(loginSuccess);

            if (data.status === "OTP_REQUIRED") {
                setVerifyButtonState(false, "Verify OTP");
                showLoginSuccess(data.message || "OTP sent successfully");
                startResendCountdown();
                getOtpModal().show();
                return;
            }

            showOtpSuccess(data.message || "OTP verified successfully");

            if (data.redirectUrl) {
                window.location.replace(data.redirectUrl);
                return;
            }

            window.location.reload();
        } catch (error) {
            setVerifyButtonState(false, "Verify OTP");
            showOtpError(error.response?.data?.message || "Invalid OTP");
        }
    }

    async function resendOtp() {
        if (!resendOtpBtn || resendOtpBtn.disabled) {
            return;
        }

        resendOtpBtn.disabled = true;
        resendOtpBtn.innerText = "Sending...";

        try {
            const response = await axios.post("/otp/resend-login-otp", {
                purpose: "APPLICANT_LOGIN"
            });

            const data = response.data || {};
            showOtpSuccess(data.message || "OTP resent successfully");
            startResendCountdown();
        } catch (error) {
            resendOtpBtn.disabled = false;
            resendOtpBtn.innerText = "Resend OTP";
            showOtpError(error.response?.data?.message || "Unable to resend OTP");
        }
    }

    if (userType) {
        userType.addEventListener("change", handleUserTypeChange);
        handleUserTypeChange();
    }

    if (refreshButton) {
        refreshButton.addEventListener("click", function () {
            getCaptcha();
        });
    }

    getCaptcha();

    if (loginForm) {
        loginForm.addEventListener("submit", function (e) {
            e.preventDefault();
            submitLoginForm();
        });
    }

    if (otpForm) {
        otpForm.addEventListener("submit", function (e) {
            e.preventDefault();
            submitOtpVerification();
        });
    }

    if (resendOtpBtn) {
        resendOtpBtn.addEventListener("click", resendOtp);
    }
});