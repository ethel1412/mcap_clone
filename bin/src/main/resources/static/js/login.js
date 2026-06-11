document.addEventListener("DOMContentLoaded", function () {
  /* ---------------- Step Number Animation ---------------- */
  const steps = document.querySelectorAll(".step-number");
  steps.forEach((step, index) => {
    step.style.animation = `bounceIn 0.6s ${index * 0.2}s forwards`;
    step.style.opacity = "0";
  });

  // Add CSS animation dynamically
  const style = document.createElement("style");
  style.textContent = `
    @keyframes bounceIn {
      0% { transform: scale(0.1); opacity: 0; }
      60% { transform: scale(1.2); opacity: 1; }
      100% { transform: scale(1); }
    }
  `;
  document.head.appendChild(style);

  /* ---------------- Accessibility Trigger ---------------- */
  const customButton = document.getElementById("custom-accessibility-trigger");
  if (customButton) {
    customButton.addEventListener("click", function () {
      const widgetButton = document.getElementById("uw-widget-custom-trigger");
      if (widgetButton) {
        widgetButton.click(); // Trigger the official widget
      } else {
        console.warn("Accessibility widget trigger not yet loaded.");
      }
    });
  }

  /* ---------------- Login + Captcha + Encryption ---------------- */
  $(document).ready(function () {
    var publicKey;

    // Refresh captcha
    $("#refresh-button").click(function () {
      getCaptcha();
    });

    // Load captcha first time
    getCaptcha();

    // Handle login form submit
    var loginForm = document.getElementById("login-form");
    if (loginForm) {
      loginForm.onsubmit = function (e) {
        e.preventDefault(); // prevent double submit

        getPublicKey(function (d) {
          publicKey = d;

          var en = new JSEncrypt();
          en.setPublicKey(publicKey);

          var pwd = $("#password").val();
          var encryptedPassword = en.encrypt(pwd);

          $("#password").val(encryptedPassword);
          loginForm.submit();
        });

        return false;
      };
    }
  });

  /* ---------------- Captcha Loader ---------------- */
  function getCaptcha() {
    $.ajax({
      type: "GET",
      url: "/captcha/get-captcha",
      contentType: "application/json; charset=utf-8",
      async: false,
      success: function (res) {
        $("#captchaImage").html(
          "<img alt='CAPTCHA' src='data:image/jpeg;base64," + res + "' >"
        );
      },
    });
  }
});