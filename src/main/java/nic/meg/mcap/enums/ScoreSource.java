package nic.meg.mcap.enums;

public enum ScoreSource {
    QUALIFICATION_EXAM,   // marks from qualification
    NON_CUET, 
    CUET,                // CUET (UG/PG – you can distinguish via program level)
    JEE,                 // JEE Main
    GATE,                // GATE
    NET,                 // NET
    OTHER                // any other exam
}