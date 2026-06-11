document.addEventListener('DOMContentLoaded', function () {
    const successEl = document.getElementById('academicSuccess');
    const errorEl = document.getElementById('academicError');

    if (successEl && successEl.value) {
        Swal.fire({
            icon: 'success',
            title: 'Success!',
            text: successEl.value,
            timer: 2500,
            showConfirmButton: false
        });
    }

    if (errorEl && errorEl.value) {
        Swal.fire({
            icon: 'error',
            title: 'Error!',
            text: errorEl.value,
            showConfirmButton: true
        });
    }
});
