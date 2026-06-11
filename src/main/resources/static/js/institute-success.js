document.addEventListener('DOMContentLoaded', function () {

    const body = document.querySelector('body');
    const isSuccess = body.dataset.submissionSuccess;

    if (isSuccess === 'true') {
        let htmlContent = 'Your institute registration has been successfully submitted.';
        htmlContent += '<br><br>Your application is now <span class="fw-bold text-warning">pending admin approval</span>.';
        htmlContent += '<br>You will receive your login credentials (username) once your registration is approved. You will then be prompted to set your password upon first login.';
        htmlContent += '<br><br>Please check your status periodically via the <a href="/institute/status" class="alert-link">Institute Status Check</a> page.';
        

        Swal.fire({
            icon: 'success',
            title: 'Registration Submitted!',
            html: htmlContent,
            confirmButtonText: 'Return to Home', 
            confirmButtonColor: '#0d6efd', // Bootstrap primary color
            showDenyButton: true,
            denyButtonText: 'Register Another Institute', 
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