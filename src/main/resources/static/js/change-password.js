document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("change-password-form");
    let isSubmitting = false;

    if (!form) return;

    form.addEventListener("submit", function (e) {

        if (isSubmitting) return;

        e.preventDefault();

        getPublicKey().then(function (publicKey) {

            if (!publicKey) return;

            const en = new JSEncrypt();
            en.setPublicKey(publicKey);

            // Validate before encrypting
            const newPassword = document.getElementById("newPassword").value;
            const confirmPassword = document.getElementById("confirmPassword").value;

            if (!newPassword || !confirmPassword) {
                alert("Both password fields are required");
                return;
            }

            if (newPassword !== confirmPassword) {
                alert("New password and confirmation do not match");
                return;
            }

            // Encrypt both fields
            if (!encryptField(en, "newPassword", "New Password")) return;
            if (!encryptField(en, "confirmPassword", "Confirm Password")) return;

            isSubmitting = true;
            form.submit();
        });
    });

    function encryptField(en, fieldId, fieldName) {
        const el = document.getElementById(fieldId);

        if (!el || !el.value.trim()) {
            return false;
        }

        const encrypted = en.encrypt(el.value);

        if (!encrypted) {
            alert("Encryption failed for " + fieldName);
            return false;
        }

        el.value = encrypted;
        return true;
    }

});