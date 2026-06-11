document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("changePasswordForm");

  const publicKey = `-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn4pp9Iqmz/WZtd/nQJIoc
OsvMc0l4C+H7ex9bfyYN9pLjaXohgZub6meVawI8KNWVrRfx9psSF2V22DM88IBWO
8Fw2BSxXSKcCGffuGhycY48p2lpKdBKNVp0EFZNIf2wZjS9sF9Jz0WfepcDTCoxkx
l1p24vNHqIReEGJyVeo6i9bLjdnNgry7u7vMY+ogLeLhytdnO2lJP5aRRr8DzPvBL
wf67oC3VcgE3KZ6EMYzaAcqR36Aytci/YVt9RnQEQ65Uo8GwzGYo2kAnAEmMHBEPX
1iJwmMUuGFAMif9LdykOvhlKC808rkJgThLEHHcCtJLNWKWrJIUYsbtrYiFuQIDAQAB
-----END PUBLIC KEY-----`;



  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(form);
    const data = Object.fromEntries(formData);

    // Password match validation before encryption
    if (data.newPassword !== data.confirmPassword) {
      showAlert("danger", "New password and confirm password do not match.");
      return;
    }

    // Initialize JSEncrypt and set the public key
    const encrypt = new JSEncrypt();
    encrypt.setPublicKey(publicKey);

    // Encrypt passwords
    const encryptedOldPassword = encrypt.encrypt(data.oldPassword);
    const encryptedNewPassword = encrypt.encrypt(data.newPassword);

    // Log plaintext and encrypted values for debugging
    console.log("Plaintext oldPassword:", data.oldPassword);
    console.log("Encrypted oldPassword:", encryptedOldPassword);
    console.log("Plaintext newPassword:", data.newPassword);
    console.log("Encrypted newPassword:", encryptedNewPassword);
    if (!encryptedOldPassword || !encryptedNewPassword) {
      showAlert("danger", "Encryption failed. Please try again.");
      return;
    }

    const payload = {
      oldPassword: encryptedOldPassword,
      newPassword: encryptedNewPassword,
    };

    try {
      // Send encrypted data to backend
      const res = await axios.post(
        "/user-management/data/change-password",
        payload,
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      showAlert("success", res.data || "Password changed successfully.");
      form.reset();
    } catch (err) {
      console.error("Error changing password:", err);
      showAlert("danger", err.response?.data || "Failed to change password.");
    }
  });

  function showAlert(type, message) {
    const alertBox = document.getElementById("alert-box");
    alertBox.innerHTML = `<div class="alert alert-${type}" role="alert">${message}</div>`;
  }
});
