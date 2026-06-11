package nic.meg.mcap.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nic.meg.mcap.dto.response.AllottedCandidateRowDTO;
import nic.meg.mcap.dto.response.ProgrammeAllocationSummaryDTO;
import nic.meg.mcap.dto.response.SeatAllocationSummaryDTO;
import nic.meg.mcap.services.SeatAllotmentService;

/**
 * REST Controller for Seat Allotment Data Operations. Base path:
 * /seat-allotment-data
 */
@RestController
@RequestMapping("/seat-allotment-data")
public class SeatAllotmentDataController {

	private static final Logger logger = LoggerFactory.getLogger(SeatAllotmentDataController.class);

	@Autowired
	private SeatAllotmentService seatAllotmentService;

	/**
	 * Run allocation for an admission window + roundType + phaseNo. POST
	 * /seat-allotment-data/window/{admissionCode}/run
	 */
	@PostMapping("/window/{admissionCode}/run")
	public ResponseEntity<?> runAllocation(@PathVariable("admissionCode") String admissionCode,
			@RequestParam(value = "roundType", required = false) String roundType,
			@RequestParam(value = "phaseNo", required = false) Integer phaseNo) {
		try {
			SeatAllocationSummaryDTO summary = seatAllotmentService.runAllocationForWindow(admissionCode, roundType,
					phaseNo);

			return ResponseEntity.ok(summary);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	/**
	 * Get summary info. GET /seat-allotment-data/window/{admissionCode}/summary
	 */
	@GetMapping("/window/{admissionCode}/summary")
	public ResponseEntity<SeatAllocationSummaryDTO> getAllocationSummary(
			@PathVariable("admissionCode") String admissionCode,
			@RequestParam(value = "roundType", required = false) String roundType,
			@RequestParam(value = "phaseNo", required = false) Integer phaseNo) {
		SeatAllocationSummaryDTO summary = seatAllotmentService.getAllocationSummary(admissionCode, roundType, phaseNo);
		return ResponseEntity.ok(summary);
	}

	// =========================================================================
	// ADMIN REPORTS ENDPOINTS
	// =========================================================================

	/**
	 * Get Programme Summary
	 */
	@GetMapping("/api/admin/allotments/summary")
	public ResponseEntity<List<ProgrammeAllocationSummaryDTO>> getProgrammeSummary(
			@RequestParam("windowCode") String windowCode, @RequestParam("programmeId") Short programmeId,
			@RequestParam(value = "roundType", required = false) String roundType,
			@RequestParam(value = "phaseNo", required = false) Integer phaseNo) {

		List<ProgrammeAllocationSummaryDTO> summaryList = seatAllotmentService.getProgrammeAllocationSummary(windowCode,
				programmeId, roundType, phaseNo);
		return ResponseEntity.ok(summaryList);
	}

	/**
	 * Get allotted candidates for a specific programme inside a window. CHANGED:
	 * Updated to match the /api/admin/... path and @RequestParams called by the JS!
	 */
	@GetMapping("/api/admin/allotments/candidates")
	public ResponseEntity<List<AllottedCandidateRowDTO>> getAllottedCandidates(
			@RequestParam("windowCode") String windowCode, @RequestParam("programmeOfferedId") Integer poId,
			@RequestParam(value = "roundType", required = false) String roundType,
			@RequestParam(value = "phaseNo", required = false) Integer phaseNo) {
		List<AllottedCandidateRowDTO> list = seatAllotmentService.getAllottedCandidates(windowCode, roundType, phaseNo,
				poId);
		return ResponseEntity.ok(list);
	}

	/**
	 * Quick count endpoint.
	 */
	@GetMapping("/window/{admissionCode}/programme-offered/{programmeOfferedId}/count")
	public ResponseEntity<Integer> countAllotments(@PathVariable("admissionCode") String admissionCode,
			@PathVariable("programmeOfferedId") Integer poId,
			@RequestParam(value = "roundType", required = false) String roundType,
			@RequestParam(value = "phaseNo", required = false) Integer phaseNo) {
		int count = seatAllotmentService.countAllotments(admissionCode, roundType, phaseNo, poId);
		return ResponseEntity.ok(count);
	}
}