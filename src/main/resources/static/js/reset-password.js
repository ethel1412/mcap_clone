document.addEventListener("DOMContentLoaded", function () {

    // Toggle password visibility
    const toggleButtons =
        document.querySelectorAll(".toggle-password");

    toggleButtons.forEach(button => {

        button.addEventListener("click", function () {

            const targetId =
                this.getAttribute("data-target");

            const input =
                document.getElementById(targetId);

            const icon =
                this.querySelector("i");

            if (input.type === "password") {

                input.type = "text";

                icon.classList.remove("fa-eye");
                icon.classList.add("fa-eye-slash");

            } else {

                input.type = "password";

                icon.classList.remove("fa-eye-slash");
                icon.classList.add("fa-eye");
            }
        });
    });

    // Password validation
    const passwordInput =
        document.getElementById("newPassword");

    const confirmPasswordInput =
        document.getElementById("confirmPassword");

    function validatePasswords() {

        const password =
            passwordInput.value;

        const confirmPassword =
            confirmPasswordInput.value;

        // Password must contain:
        // 1 uppercase
        // 1 lowercase
        // 1 number
        // 1 special character
        // minimum 8 characters
        const passwordRegex =/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[#@$!%*?&])[A-Za-z\d#@$!%*?&]{8,}$/;
        // Validate password strength
        if (!passwordRegex.test(password)) {

            passwordInput.setCustomValidity(
                "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
            );

        } else {

            passwordInput.setCustomValidity("");
        }

        // Validate confirm password
        if (confirmPassword !== password) {

            confirmPasswordInput.setCustomValidity(
                "Passwords do not match."
            );

        } else {

            confirmPasswordInput.setCustomValidity("");
        }
    }

    passwordInput.addEventListener(
        "input",
        validatePasswords
    );

    confirmPasswordInput.addEventListener(
        "input",
        validatePasswords
    );
});