package nic.meg.mcap.enums;

import lombok.Getter;

@Getter

public enum ReservationType {
    COMMUNITY,           // ST, SC, OBC, GENERAL
    EXAM_QUALIFIED,      // CUET or JEE based on stream
    PWD,                 // Persons with Disabilities
    DEFENCE,             // Defence Personnel
    SPORTS,              // Sports Quota
    EWS                  // Economically Weaker Section
}