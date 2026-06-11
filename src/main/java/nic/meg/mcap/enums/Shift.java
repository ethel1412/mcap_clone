package nic.meg.mcap.enums;

import lombok.Getter;

@Getter
public enum Shift {
    MORNING("Morning"),
    DAY("Day"),
    EVENING("Evening"),
    NIGHT("Night"),
    NA("Not Applicable"); // Default or for programs that don't use shifts

    private final String displayName;

    Shift(String displayName) {
        this.displayName = displayName;
    }
}