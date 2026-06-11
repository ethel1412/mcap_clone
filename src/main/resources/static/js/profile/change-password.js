document.addEventListener("DOMContentLoaded", function () {

  const form = document.getElementById("changePasswordForm");
  let isSubmitting = false;

  if (!form) return;

  form.addEventListener("submit", function (e) {

    if (isSubmitting) return;

    e.preventDefault();

    const formData = new FormData(form);
    const data = Object.fromEntries(formData);

    // ✅ Basic validation BEFORE encryption
    if (!data.currentPassword || !data.newPassword || !data.confirmPassword) {
      showAlert("danger", "All fields are required");
      return;
    }

    if (data.newPassword !== data.confirmPassword) {
      showAlert("danger", "New password and confirm password do not match");
      return;
    }

    // 🔥 GET PUBLIC KEY dynamically
    getPublicKey().then(function (publicKey) {

      if (!publicKey) {
        showAlert("danger", "Failed to load encryption key");
        return;
      }

      const en = new JSEncrypt();
      en.setPublicKey(publicKey);

      // 🔥 Encrypt ALL fields
      const encryptedCurrent = en.encrypt(data.currentPassword);
      const encryptedNew = en.encrypt(data.newPassword);
      const encryptedConfirm = en.encrypt(data.confirmPassword);

      console.log("Encrypted:", {
        encryptedCurrent,
        encryptedNew,
        encryptedConfirm
      });

      // ❌ If encryption fails
      if (!encryptedCurrent || !encryptedNew || !encryptedConfirm) {
        showAlert("danger", "Encryption failed");
        return;
      }

      const payload = {
        currentPassword: encryptedCurrent,
        newPassword: encryptedNew,
        confirmPassword: encryptedConfirm
      };

      console.log("Payload:", payload);

      isSubmitting = true;

      axios.post("/profile/change-password", payload, {
        headers: {
          "Content-Type": "application/json"
        }
      })
      .then(res => {
        showAlert("success", res.data || "Password changed successfully");
        form.reset();
        isSubmitting = false;
      })
      .catch(err => {
        console.error(err);
        showAlert("danger", err.response?.data || "Failed to change password");
        isSubmitting = false;
      });

    });

  });

  function showAlert(type, message) {
    const alertBox = document.getElementById("alert-box");
    if (alertBox) {
      alertBox.innerHTML =
        `<div class="alert alert-${type}" role="alert">${message}</div>`;
    }
  }

});