package nic.meg.mcap.enums;

public enum ApplicantType {

    UNKNOWN("Not decided yet"),
    WITH_ENTRANCE("With Entrance Exam"),
    WITHOUT_ENTRANCE("Without Entrance Exam");

    private final String displayName;

    ApplicantType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
