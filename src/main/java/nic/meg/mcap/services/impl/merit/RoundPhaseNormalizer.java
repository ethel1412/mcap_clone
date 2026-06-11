package nic.meg.mcap.services.impl.merit;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RoundPhaseNormalizer {

    public static final String DEFAULT_ROUND_TYPE = "CUET";
    public static final Integer DEFAULT_PHASE_NO = 1;

    public String normalizeRoundType(String roundType) {
        if (roundType == null || roundType.isBlank()) return DEFAULT_ROUND_TYPE;
        String t = roundType.trim().toUpperCase(Locale.ROOT);

        // --- UPDATED: Allow "COMBINED" as a valid round type ---
        if (!t.equals("CUET") && !t.equals("NON_CUET") && !t.equals("COMBINED")) {
            throw new IllegalArgumentException("Invalid roundType: " + roundType);
        }
        return t;
    }

    public Integer normalizePhaseNo(Integer phaseNo) {
        if (phaseNo == null) return DEFAULT_PHASE_NO;
        if (phaseNo < 1) throw new IllegalArgumentException("phaseNo must be >= 1");
        return phaseNo;
    }
}