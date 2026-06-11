// /static/js/important-instruction-modal.js

document.addEventListener("DOMContentLoaded", function () {

    // Show Important Instruction Modal
    const modalElement = document.getElementById("importantInstructionModal");

    if (modalElement) {
        const instructionModal = new bootstrap.Modal(modalElement);
        instructionModal.show();
    }

    // Initialize Bootstrap Tooltips
    const tooltipTriggerList = [].slice.call(
        document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );

    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // CSP FIX: Replaced inline styles and <style> injections with CSS classes
    const steps = document.querySelectorAll('.step-number');

    steps.forEach((step, index) => {
        step.classList.add('step-bounce-anim');
        step.classList.add(`anim-delay-${index}`);
    });

});