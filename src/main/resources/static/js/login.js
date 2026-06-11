document.addEventListener("DOMContentLoaded", function () {
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  axios.defaults.headers.common[csrfHeader] = csrfToken;

  const steps = document.querySelectorAll(".step-number");
  const userType = document.getElementById("userType");
  const loginForm = document.getElementById("login-form");
  const dob = document.getElementById("dob-temp");
  const dobGroup = document.getElementById("dob-group");
  let isSubmitting = false;

  window.addEventListener("pageshow", function (event) {

    const navigation =
        performance.getEntriesByType("navigation");

    if (
        event.persisted ||
        (
            navigation.length > 0 &&
            navigation[0].type === "back_forward"
        )
    ) {

        window.location.reload();
    }
});

  function handleUserTypeChange() {
    if (!userType || !dobGroup || !dob) return;

    if (userType.value === "APPLICANT") {
      dobGroup.classList.remove("d-none");
      dob.setAttribute("required", "required");
    } else {
      dobGroup.classList.add("d-none");
      dob.removeAttribute("required");
      dob.value = "";
    }
  }

  if (userType) {
    userType.addEventListener("change", handleUserTypeChange);
    handleUserTypeChange();
  }

  // Stagger bounceIn animation on step numbers.
  // Uses CSS classes .step-bounce-in and .step-delay-{index} defined in login.css
  // instead of inline style.setProperty — avoids CSP 'unsafe-inline' violation.
  steps.forEach((step, index) => {
    step.classList.add("step-bounce-in", `step-delay-${index}`);
  });

  document.getElementById("refresh-button")?.addEventListener("click", getCaptcha);
  getCaptcha();

	if (loginForm) {

loginForm.addEventListener("submit", function (e) {

if (isSubmitting) return;

e.preventDefault();

getPublicKey().then(function (publicKey) {

  if (!publicKey) return;

  const en = new JSEncrypt();

  en.setPublicKey(publicKey);

  const type = userType.value;

  // ================= ENCRYPT PASSWORD =================

		const passwordField =
		    document.getElementById("password");

		if (!passwordField.dataset.originalValue) {

		  passwordField.dataset.originalValue =
		      passwordField.value;
		}

		const encryptedPassword =
		    en.encrypt(
		      passwordField.dataset.originalValue
		    );

		if (!encryptedPassword) {

		  alert("Password encryption failed");

		  return;
		}

		passwordField.value = encryptedPassword;

  // ================= ENCRYPT DOB =================

  if (type === "APPLICANT") {

    if (!dob.value) {

      alert("Please select Date of Birth");

      return;
    }

    if (!encryptField(
          en,
          "dob-temp",
          "Date of Birth"
        )) {

      return;
    }

  } else {

    dob.value = "";
  }

  isSubmitting = true;

  const formData =
        new FormData(loginForm);

  axios.post(

    loginForm.action,

    new URLSearchParams(formData),

    {
      headers: {
        "Content-Type":
          "application/x-www-form-urlencoded"
      },

      withCredentials: true
    }

  )

  // ================= SUCCESS =================

  .then(function (response) {
    const data = response.data;

    const errorDiv =
        document.getElementById(
          "login-error"
        );

    const successDiv =
        document.getElementById(
          "login-success"
        );

    // reset alerts
    errorDiv.classList.add("d-none");
    successDiv.classList.add("d-none");

    // ================= OTP REQUIRED =================

 	if (data.status === "OTP_REQUIRED") {

	  isSubmitting = false;

	  successDiv.innerText =
	    data.message || "OTP sent successfully";

	  successDiv.classList.remove("d-none");

	  const loginBtn =
	    document.getElementById("login-submit-btn");

	  if (loginBtn) {
	    loginBtn.disabled = true;
	  }

	  const otpModalElement =
	    document.getElementById("otpModal");

	  let otpModal =
	    bootstrap.Modal.getInstance(otpModalElement);

	  if (!otpModal) {
	    otpModal = new bootstrap.Modal(
	      otpModalElement,
	      {
	        backdrop: "static",
	        keyboard: false
	      }
	    );
	  }

	  otpModal.show();

	  return;
	}


    // ================= LOGIN SUCCESS =================

    if (data.status === "LOGIN_SUCCESS") {

      const otpModalElement =
  	  document.getElementById("otpModal");

	  const otpModal =bootstrap.Modal.getInstance(otpModalElement);

		if (otpModal) {
		  otpModal.hide();
		}

		window.location.replace(data.redirectUrl);
    }

  })

  // ================= ERROR =================

  .catch(function (error) {

	 getCaptcha();

    const captchaInput = document.getElementById("captcha");

    if (captchaInput) {
      captchaInput.value = "";
    }

	const passwordField = document.getElementById("password");

	if (
	    passwordField &&
	    passwordField.dataset.originalValue
	) {

	    passwordField.value = passwordField.dataset.originalValue;
	}

    const errorDiv =
        document.getElementById(
          "login-error"
        );

    const successDiv =
        document.getElementById(
          "login-success"
        );

    errorDiv.classList.add("d-none");
    successDiv.classList.add("d-none");

    // ================= OTP REQUIRED IN CATCH =================

    if (
      error.response &&
      error.response.data &&
      error.response.data.status ===
        "OTP_REQUIRED"
    ) {

      successDiv.innerText =
          error.response.data.message ||
          "OTP sent successfully";

      successDiv.classList.remove(
          "d-none"
      );
      return;
    }

    // ================= NORMAL LOGIN ERROR =================

    errorDiv.classList.remove(
      "d-none"
    );

    if (
      error.response &&
      error.response.data
    ) {

      errorDiv.innerText =
          error.response.data.message ||
          "Invalid credentials";

    } else {

      errorDiv.innerText =
          "Login failed";
    }

    isSubmitting = false;
  });

});


});

}


  // ================= OTP VERIFY =================

  const otpForm = document.getElementById("otp-form");

if (otpForm) {

  otpForm.addEventListener("submit", function (e) {

    e.preventDefault();

    const otp = document.getElementById("otp").value;

      if (!/^\d{6}$/.test(otp)) {

		  otpError.classList.remove("d-none");

		  otpError.innerText =
		    "Please enter valid 6 digit OTP";

		  return;
		}

    const otpError = document.getElementById("otp-modal-error");
    const otpSuccess = document.getElementById("otp-modal-success");

    otpError.classList.add("d-none");
    otpSuccess.classList.add("d-none");

    const verifyBtn = document.getElementById("verifyOtpBtn");
	verifyBtn.disabled = true;
	verifyBtn.innerText = "Verifying...";

    axios.post("/otp/verify-login-otp", {
      otp: otp,
      purpose: "APPLICANT_LOGIN"
    })

    .then(function (response) {

    const data = response.data;

    const errorDiv = document.getElementById("login-error");

    const successDiv = document.getElementById("login-success");

    errorDiv.classList.add("d-none");
    successDiv.classList.add("d-none");

    // ================= OTP REQUIRED =================

    if (data.status === "OTP_REQUIRED") {

        isSubmitting = false;

        successDiv.innerText =
            data.message || "OTP sent successfully";

        successDiv.classList.remove("d-none");

        const loginBtn =
            document.getElementById("login-submit-btn");

        if (loginBtn) {
            loginBtn.disabled = true;
        }

        const otpModalElement =
            document.getElementById("otpModal");

        let otpModal =
            bootstrap.Modal.getInstance(
                otpModalElement
            );

        if (!otpModal) {

            otpModal = new bootstrap.Modal(
                otpModalElement,
                {
                    backdrop: "static",
                    keyboard: false
                }
            );
        }

        otpModal.show();

        return;
    }

    // ================= NORMAL LOGIN SUCCESS =================
	window.location.replace(data.redirectUrl);
})

    .catch(function (error) {
		verifyBtn.disabled = false;
		verifyBtn.innerText = "Verify OTP";
	      otpError.classList.remove("d-none");

	      otpError.innerText =
	        error.response?.data?.message ||
	        "Invalid OTP";

	    });

  });

}

  function encryptField(en, fieldId, fieldName) {
    const el = document.getElementById(fieldId);
    if (!el) {
      alert(fieldName + " element not found");
      return false;
    }

    const value = el.value;

    if (!value || value.trim() === "") {
      alert(fieldName + " is required");
      return false;
    }

    const encrypted = en.encrypt(value);
    if (!encrypted) return false;

    if (fieldId === "dob-temp") {
      const hiddenDob = document.getElementById("dob");
      if (!hiddenDob) return false;

      hiddenDob.value = encrypted;
    } else {
      el.value = encrypted;
    }

    return true;
  }

});
