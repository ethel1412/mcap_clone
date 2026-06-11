package nic.meg.mcap.enums;

public enum AllotmentStatus {
    PENDING_VERIFICATION,
    INSTITUTE_REJECTED,
    PENDING,
    ACCEPTED,
    REJECTED,
    /**
     * Slide Up: Applicant has paid and is holding this seat,
     * but remains eligible for higher-preference seats in future rounds.
     * If no action is taken within the deadline, the allotment auto-reverts to REJECTED.
     */
    SLIDE_UP
}