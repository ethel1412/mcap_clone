package nic.meg.mcap.enums;

public enum CriteriaApprovalStatus {
    DRAFT,      // Institute working on criteria (not yet submitted)
    PENDING,    // Submitted to Controller of Examinations, awaiting review
    APPROVED,   // Approved by Controller of Examinations
    REJECTED    // Rejected - Institute can edit and resubmit
}
