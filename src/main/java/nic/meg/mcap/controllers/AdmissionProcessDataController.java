package nic.meg.mcap.controllers;

import nic.meg.mcap.dto.response.SeatAllocationSummaryDTO;
import nic.meg.mcap.services.SeatAllotmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for running seat allocation for an admission window.
 * Called from the controller's UI (e.g., Start Admission Process button).
 */
@RestController
@RequestMapping("/admission-process")
public class AdmissionProcessDataController {

    private final SeatAllotmentService seatAllotmentService;

    public AdmissionProcessDataController(SeatAllotmentService seatAllotmentService) {
        this.seatAllotmentService = seatAllotmentService;
    }

    /**
     * Run seat allocation for all programmes in the given admission window for a given roundType + phaseNo.
     * POST /admission-process/{admissionCode}/run-allocation?roundType=CUET&phaseNo=1
     */
    @PostMapping("/{admissionCode}/run-allocation")
    public ResponseEntity<SeatAllocationSummaryDTO> runAllocation(
            @PathVariable("admissionCode") String admissionCode,
            @RequestParam(value = "roundType", required = false) String roundType,
            @RequestParam(value = "phaseNo", required = false) Integer phaseNo
    ) {
        SeatAllocationSummaryDTO summary =
                seatAllotmentService.runAllocationForWindow(admissionCode, roundType, phaseNo);

        return ResponseEntity.ok(summary);
    }
}