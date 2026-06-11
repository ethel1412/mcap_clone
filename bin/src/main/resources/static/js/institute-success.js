document.addEventListener('DOMContentLoaded', function () {

    const body = document.querySelector('body');
    const isSuccess = body.dataset.submissionSuccess;
    // REMOVED these lines as credentials are not immediately displayed
    // const generatedUsername = body.dataset.generatedUsername;
    // const generatedPassword = body.dataset.generatedPassword;

    if (isSuccess === 'true') {
        let htmlContent = 'Your institute registration has been successfully submitted.';
        htmlContent += '<br><br>Your application is now <span class="fw-bold text-warning">pending admin approval</span>.';
        htmlContent += '<br>You will receive your login credentials (username) once your registration is approved. You will then be prompted to set your password upon first login.';
        htmlContent += '<br><br>Please check your status periodically via the <a href="/institute/status" class="alert-link">Institute Status Check</a> page.';
        // Optional: If you implement a status page that requires an ID
        // htmlContent += '<br>Your Registration ID: <code class="text-primary fs-5 fw-bold">[INSTITUTE_REGISTRATION_ID]</code>';

        Swal.fire({
            icon: 'success',
            title: 'Registration Submitted!',
            html: htmlContent,
            confirmButtonText: 'Return to Home', // Changed button text
            confirmButtonColor: '#0d6efd', // Bootstrap primary color
            showDenyButton: true, // NEW: Added a deny button
            denyButtonText: 'Register Another Institute', // NEW: Deny button text
            denyButtonColor: '#6c757d', // Bootstrap secondary color
            allowOutsideClick: false,
            allowEscapeKey: false,
        }).then((result) => {
            if (result.isConfirmed) {
                // User wants to return home (e.g., login page)
                window.location.href = '/login'; // Redirect to login page
            } else if (result.isDenied) {
                // User wants to register another institute
                window.location.href = '/institute-form';
            }
        });
    }
});