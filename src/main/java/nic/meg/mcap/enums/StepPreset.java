package nic.meg.mcap.enums;

import lombok.Getter;

@Getter
public enum StepPreset {

    LOCK_SEATS(
            "Locking of seats and programmes for institutes",
            "Institutes lock offered seats and programmes before admission starts.",
            ScheduleActorRole.INSTITUTE
    ),
    SET_ELIGIBILITY_RULES(
            "Set the eligibility rules for each programme",
            "Controller sets the eligibility rules for each programme",
            ScheduleActorRole.CONTROLLER
    ),
    CONTROLLER_APPROVAL(
            "Controller approval of institute admission settings",
            "Controller reviews and approves institute seat matrices and admission rules.",
            ScheduleActorRole.CONTROLLER
    ),
    APPLICATION_PERIOD(
            "Application period for applicants",
            "Applicants can submit new applications and choose their preferred programmes.",
            ScheduleActorRole.APPLICANT
    ),
    FEE_PAYMENT(
            "Fee payment period",
            "Applicants pay required admission fees for their allocated seats.",
            ScheduleActorRole.APPLICANT
    ),
    APPLICATION_CORRECTION(
            "Application correction period for applicants",
            "Applicants can edit or correct submitted application details within this window.",
            ScheduleActorRole.APPLICANT
    ),
    MERIT_GENERATION(
            "Merit list generation by controller",
            "Controller generates and publishes merit lists based on eligibility and rules.",
            ScheduleActorRole.CONTROLLER
    ),
    ADMISSION_ALLOCATION(
            "Admission allocation period",
            "Controller allocates seats to applicants as per merit, reservation, and preferences.",
            ScheduleActorRole.CONTROLLER
    ),
    SEAT_ACCEPTANCE(
            "Seat acceptance period",
            "Applicants confirm or reject their allocated seats within the given deadline.",
            ScheduleActorRole.APPLICANT
    ),
    ALLOCATION_ROUNDS(
            "Allocation and acceptance rounds",
            "Multiple rounds of allocation and seat acceptance continue until seats are filled.",
            ScheduleActorRole.CONTROLLER
    ),
    ADMISSION_REPORT(
            "Admission report generation by controller",
            "Controller generates final admission reports and statistics for the completed cycle.",
            ScheduleActorRole.CONTROLLER
    );

    private final String label;
    private final String description;
    private final ScheduleActorRole defaultRole;

    StepPreset(String label, String description, ScheduleActorRole defaultRole) {
        this.label = label;
        this.description = description;
        this.defaultRole = defaultRole;
    }
}
