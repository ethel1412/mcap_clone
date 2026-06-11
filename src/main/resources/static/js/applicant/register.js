import { showSuccess, showError, showWarning, showConfirm } from './utils.js';

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('registrationForm');
    const submitBtn = document.getElementById('submitBtn');
    const phoneInput = document.getElementById('phoneNumber');
    const countryCodeSelect = document.getElementById('countryPhoneCode');
    let mobileConfirmed = false;
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');  
    const togglePassword = document.getElementById("togglePassword");
    const togglePassword1 = document.getElementById("togglePassword1");

   if (togglePassword) {
	
	    togglePassword.addEventListener("click", function () {	
	        const passwordInput = document.getElementById("password");	
	        const icon = this.querySelector("i");
	
	        if (passwordInput.type === "password") {
	
	            passwordInput.type = "text";
	
	           icon.classList.remove("fa-eye");
			   icon.classList.add("fa-eye-slash");
	
	        } else {
	
	            passwordInput.type = "password";
	
	           icon.classList.remove("fa-eye-slash");
				icon.classList.add("fa-eye");
	        }
	
	    });
	
		}

		if (togglePassword1) {
		
		    togglePassword1.addEventListener("click", function () {
		
		        const passwordInput = document.getElementById("confirmPassword");
		
		        const icon = this.querySelector("i");
		
		        if (passwordInput.type === "password") {
		
		            passwordInput.type = "text";
		
		            icon.classList.remove("bi-eye");
		            icon.classList.add("bi-eye-slash");
		
		        } else {
		
		            passwordInput.type = "password";
		
		            icon.classList.remove("bi-eye-slash");
		            icon.classList.add("bi-eye");
		        }
		
		    });
		
		}
    
		function validatePasswords() {
		
		    const password = passwordInput.value.trim();
		    const confirmPassword = confirmPasswordInput.value.trim();
		    const passwordRegex =/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[#@$!%*?&])[A-Za-z\d#@$!%*?&]{8,}$/;		
		    // Password strength validation
		    if (!passwordRegex.test(password)) {
		
		        passwordInput.setCustomValidity(
		            "Password must contain uppercase, lowercase, number, special character and be at least 8 characters."
		        );
		
		    } else {
		
		        passwordInput.setCustomValidity("");
		    }
		
		    // Confirm password validation
		    if (confirmPassword && password !== confirmPassword) {
		
		        confirmPasswordInput.setCustomValidity(
		            "Passwords do not match."
		        );
		
		    } else {
		
		        confirmPasswordInput.setCustomValidity("");
		    }
		}
		
		// Live validation
		passwordInput.addEventListener(
		    "input",
		    validatePasswords
		);
		
		confirmPasswordInput.addEventListener(
		    "input",
		    validatePasswords
		);
    
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    axios.defaults.headers.common[csrfHeader] = csrfToken;
	
	window.addEventListener('load', function () {
	    var modal = new bootstrap.Modal(document.getElementById('registrationModal'));
	    modal.show();
  	});
  	
  	getCaptcha();

	document.getElementById("refresh-button")
	?.addEventListener("click", function () {
	
	    const captchaInput =
	        document.getElementById("captcha");
	
	    if (captchaInput) {
	        captchaInput.value = "";
	    }
	
	    getCaptcha();
	});
  	
    function updatePhoneValidation() {
        if (!countryCodeSelect || !phoneInput) return;

        const selectedCode = countryCodeSelect.value;

        if (selectedCode === '+91') {
            phoneInput.maxLength = 10;
            phoneInput.pattern = '[0-9]{10}';
            phoneInput.placeholder = '10-digit number';
        } else {
            phoneInput.maxLength = 15;
            phoneInput.pattern = '[0-9]{5,15}';
            phoneInput.placeholder = 'Up to 15 digits';
        }
    }

    if (countryCodeSelect) {
        countryCodeSelect.addEventListener('change', updatePhoneValidation);
        updatePhoneValidation();
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        event.stopPropagation();

       if (!form.checkValidity()) {
		    Array.from(form.elements).forEach(el => {
		        if (!el.checkValidity()) {
		            console.log("Invalid field:", el.name || el.id, el.validationMessage);
		        }
		    });
		
		    form.classList.add('was-validated');
		    return;
		}

		if (!mobileConfirmed) {
		    validatePasswords();
			if (!passwordInput.checkValidity()) {

		        passwordInput.reportValidity();
		        return;
		    }
		
		    if (!confirmPasswordInput.checkValidity()) {
		
		        confirmPasswordInput.reportValidity();
		        return;
		    }
		
			document.activeElement?.blur();
			
			const modalEl = document.getElementById('registrationModal');
			const modalInstance = bootstrap.Modal.getInstance(modalEl);
			modalInstance?.hide();
			submitBtn.disabled = false;
		                const modal = new bootstrap.Modal(document.getElementById('reenterMobileModal'));
		                    const input = document.getElementById('reenterMobileInput');
		                    const confirmBtn = document.getElementById('confirmReenterBtn');
		
		                    input.value = '';
		                    modal.show();
		
		                    confirmBtn.onclick = function () {
		
		                        function normalizePhone(num) {
		                            return (num || '').replace(/\D/g, '');
		                        }
		
		                        const original = normalizePhone(
		                            countryCodeSelect.value + phoneInput.value
		                        );
		
		                        const reentered = normalizePhone(input.value);
		
		                        if (!reentered) {
		                            showWarning('Mobile number is required.');
		                            return;
		                        }
		
		                        if (reentered !== original) {
		                            showError('Mobile number does not match.');
		                            return;
		                        }
		
		                       	mobileConfirmed = true;

								const modalEl = document.getElementById('reenterMobileModal');
								
								modalEl.addEventListener('hidden.bs.modal', function handler() {
								
								    modalEl.removeEventListener('hidden.bs.modal', handler);
								
								    submitRegistration();
								
								}, { once: true });
								
								modal.hide();
		                        
		                    };
		    return;
		}
		submitRegistration();

       });
       
    function submitRegistration() {
	
    countryCodeSelect.disabled = false;

    const formData = new FormData(form);

    const originalContent = Array.from(submitBtn.childNodes)
        .map(n => n.cloneNode(true));

    submitBtn.disabled = true;
    submitBtn.replaceChildren();

    const spin2 = document.createElement("span");
    spin2.className = "spinner-border spinner-border-sm";

    submitBtn.append(
        spin2,
        document.createTextNode(" Submitting...")
    );

    getPublicKey().then(function (publicKey) {

        var en = new JSEncrypt();
        en.setPublicKey(publicKey);

        var pwd = document.getElementById("password").value;
        var encryptedPassword = en.encrypt(pwd);

        var dobInput = document.getElementById("dateOfBirth");
        var encryptedDob = en.encrypt(dobInput.value);

        formData.set("password", encryptedPassword);
        formData.set("dateOfBirth", encryptedDob);

        axios.post(form.action, formData)

        .then(function (response) {

            submitBtn.replaceChildren();

            const icon2 = document.createElement("i");
            icon2.className = "bi bi-check-lg";

            submitBtn.disabled = false;

            const applicantNo = response.data.applicantNo;
            const isNew = response.data.isNewUser;

            let title = isNew
                ? 'Registration Successful!'
                : 'New Application Started!';

            let text = isNew
                ? `Please use your registered phone number to log in.`
                : `New application added: <strong>${applicantNo}</strong>`;

            showConfirm(
                title,
                text,
                'Proceed to Login',
                'btn-success'
            ).then(() => {
                window.location.href = '/login';
            });

        }).catch(function (error) {

            submitBtn.disabled = false;
            submitBtn.replaceChildren(...originalContent);

            if (error.response && error.response.status === 409) {

                showConfirm(
                    'Already Applied',
                    error.response.data.message,
                    'Go to Login',
                    'btn-info'
                ).then(() => {
                    window.location.href = '/login';
                });

            } else {

                const msg =
                    error.response?.data?.message
                    || 'Unknown error occurred.';

                showError(msg);
            }
        });
    });
}
});
