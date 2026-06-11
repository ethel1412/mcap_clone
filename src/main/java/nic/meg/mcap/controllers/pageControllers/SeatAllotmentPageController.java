package nic.meg.mcap.controllers.pageControllers;

import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.AllottedCandidateRowDTO;
import nic.meg.mcap.dto.response.ProgrammeAllocationSummaryDTO;
import nic.meg.mcap.dto.response.SeatAllocationSummaryDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.services.AdmissionWindowService;
import nic.meg.mcap.services.ProgrammeOfferedService;
import nic.meg.mcap.services.SeatAllotmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/seat-allotment/page")
@RequiredArgsConstructor
public class SeatAllotmentPageController {

    private final AdmissionWindowService admissionWindowService;
    private final ProgrammeOfferedService programmeOfferedService;
    private final SeatAllotmentService seatAllotmentService;

    private String normalizeRoundType(String roundType) {
        String rt = (roundType == null) ? "CUET" : roundType.trim().toUpperCase(Locale.ROOT);
        // Ensure match with database enums/logic
        if ("NON_CUET".equals(rt)) rt = "NONCUET";
        if (!"CUET".equals(rt) && !"NONCUET".equals(rt)) {
            rt = "CUET";
        }
        return rt;
    }

    private int normalizePhaseNo(Integer phaseNo) {
        return (phaseNo == null || phaseNo < 1) ? 1 : phaseNo;
    }

    /**
     * 1) Programme-level Summary Page
     */
    @GetMapping("/window/{admissionCode}/programme/{programmeId}")
    public String showProgrammeAllotmentSummary(
            @PathVariable String admissionCode,
            @PathVariable Short programmeId,
            @RequestParam(value = "roundType", required = false) String roundType,
            @RequestParam(value = "phaseNo", required = false) Integer phaseNo,
            Model model
    ) {
        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);
        if (window == null) {
            throw new IllegalArgumentException("Admission window not found: " + admissionCode);
        }

        String rt = normalizeRoundType(roundType);
        int ph = normalizePhaseNo(phaseNo);

        SeatAllocationSummaryDTO summary = seatAllotmentService.getAllocationSummary(admissionCode, rt, ph);

        List<ProgrammeAllocationSummaryDTO> offerings = summary.getProgrammeSummaries().stream()
                .filter(p -> {
                    ProgrammeOffered po = programmeOfferedService.findById(p.getProgrammeOfferedId()).orElse(null);
                    return po != null && po.getProgramme().getProgrammeId().equals(programmeId);
                })
                .collect(Collectors.toList());

        String programmeName = offerings.isEmpty() ? "Programme" : offerings.get(0).getProgrammeName();

        model.addAttribute("admissionWindow", window);
        model.addAttribute("programmeId", programmeId);
        model.addAttribute("programmeName", programmeName);
        model.addAttribute("offerings", offerings);
        model.addAttribute("roundType", rt);
        model.addAttribute("phaseNo", ph);

        return "seat-allotment/programme-summary";
    }

    /**
     * 2) Per-institute Applicant List Page
     */
    @GetMapping("/window/{admissionCode}/programme-offered/{programmeOfferedId}")
    public String showProgrammeOfferedAllotment(
            @PathVariable String admissionCode,
            @PathVariable Integer programmeOfferedId,
            @RequestParam(value = "roundType", required = false) String roundType,
            @RequestParam(value = "phaseNo", required = false) Integer phaseNo,
            Model model
    ) {
        AdmissionWindow window = admissionWindowService.findByCode(admissionCode);
        if (window == null) {
            throw new IllegalArgumentException("Admission window not found: " + admissionCode);
        }

        ProgrammeOffered po = programmeOfferedService.findById(programmeOfferedId)
                .orElseThrow(() -> new IllegalArgumentException("ProgrammeOffered not found: " + programmeOfferedId));

        String rt = normalizeRoundType(roundType);
        int ph = normalizePhaseNo(phaseNo);

        // Required for JS dataset: data-window-code, data-po-id, data-round-type, data-phase-no
        model.addAttribute("admissionWindow", window);
        model.addAttribute("poId", programmeOfferedId);
        model.addAttribute("roundType", rt);
        model.addAttribute("phaseNo", ph);

        List<AllottedCandidateRowDTO> allottedApplicants =
                seatAllotmentService.getAllottedCandidates(admissionCode, rt, ph, programmeOfferedId);
        model.addAttribute("allottedApplicants", allottedApplicants);

        return "seat-allotment/allocated-applicants";
    }
}