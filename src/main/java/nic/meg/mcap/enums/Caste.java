package nic.meg.mcap.enums;

import lombok.Getter;

@Getter
public enum Caste {
    GENERAL("General", "GEN", 5),
    OBC("OBC", "OBC", 3),
    SC("SC", "SC", 2),
    ST("ST", "ST", 1),
    EWS("EWS", "EWS", 4); // Added with reasonable priority

    private final String displayName;
    private final String categoryCode; // matches community_category.category_code
    private final int priority;        // lower = higher priority in tie-breaker

    Caste(String displayName, String categoryCode, int priority) {
        this.displayName = displayName;
        this.categoryCode = categoryCode;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public int getPriority() {
        return priority;
    }

    public static Caste fromCategoryCode(String code) {
        if (code == null) return null;
        for (Caste c : values()) {
            if (c.categoryCode.equalsIgnoreCase(code)) {
                return c;
            }
        }
        return null;
    }
}