package nic.meg.mcap.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller; // Use @Controller for Thymeleaf
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.VerificationRequestDTO;
import nic.meg.mcap.dto.response.ApplicationStatusResponseDTO;
import nic.meg.mcap.dto.response.InstituteAllotmentDTO;
import nic.meg.mcap.dto.response.PagedResponse;
import nic.meg.mcap.dto.response.ProgrammeAllocationSummaryDTO;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.Document;
import nic.meg.mcap.entities.SeatAllotment;
import nic.meg.mcap.enums.AllotmentStatus;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.services.CounselingService;
import nic.meg.mcap.services.DocumentService;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.SeatAllotmentService;

@Controller
@RequestMapping("/api/institute/allotments")
@RequiredArgsConstructor
public class InstituteDataController {

    private static final Logger logger = LoggerFactory.getLogger(InstituteDataController.class);

    private final CounselingService counselingService;
    private final InstituteService instituteService;
    private final DocumentService documentService;
    private final SeatAllotmentRepository seatAllotmentRepository;
    private final ApplicationRepository applicationRepository;
    private final SeatAllotmentService seatAllotmentService;

    // =========================================================================
    // LEVEL 1: PROGRAMME SUMMARY STATS
    // =========================================================================
    @GetMapping("/programme-summary")
    @ResponseBody
    public ResponseEntity<List<ProgrammeAllocationSummaryDTO>> getProgrammeSummary(@RequestParam("shift") String shift,
                                                                                   Authentication auth) {

        Short instituteId = instituteService.getInstituteIdByUsername(auth.getName());
        List<ProgrammeAllocationSummaryDTO> summary = seatAllotmentService.getInstituteProgrammeSummary(instituteId,
                shift);

        return ResponseEntity.ok(summary);
    }

    // =========================================================================
    // LEVEL 2: SPECIFIC APPLICANT LIST
    // =========================================================================
    @GetMapping("/allotments-by-programme")
    @ResponseBody
    public ResponseEntity<List<InstituteAllotmentDTO>> getApplicantsByProgramme(
            @RequestParam("programmeOfferedId") Integer poId, @RequestParam("status") AllotmentStatus status) {

        List<InstituteAllotmentDTO> applicants = counselingService.getApplicantsForProgrammeAndStatus(poId, status);
        return ResponseEntity.ok(applicants);
    }

    // =========================================================================
    // VERIFICATION SUBMISSION
    // =========================================================================
    @PostMapping("/{allotmentId}/verify")
    @ResponseBody
    public ResponseEntity<Map<String, String>> performVerification(@PathVariable Long allotmentId,
                                                                   @Valid @RequestBody VerificationRequestDTO request, Authentication authentication) {
        String username = authentication.getName();
        Short instituteId = instituteService.getInstituteIdByUsername(username);

        // Ensure the allotmentId in the path matches the DTO
        request.setAllotmentId(allotmentId);

        counselingService.performVerification(allotmentId, request, instituteId);
        return ResponseEntity.ok(Map.of("message", "Verification decision has been recorded successfully."));
    }

    // =========================================================================
    // DOCUMENT REVIEW FRAGMENT (EXISTING)
    // =========================================================================
    @GetMapping("/{allotmentId}/document-review")
    public String getDocumentReviewFragment(Model model, @PathVariable Long allotmentId, Authentication auth) {
        String username = auth.getName();
        Short instituteId = instituteService.getInstituteIdByUsername(username);
        try {
            SeatAllotment allotment = seatAllotmentRepository.findById(allotmentId)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                            "Allotment not found with ID: " + allotmentId));

            Short actualInstituteId = allotment.getProgrammeOffered().getInstituteDepartment().getInstitute()
                    .getInstituteId();

            if (!actualInstituteId.equals(instituteId)) {
                throw new SecurityException("Unauthorized access to allotment documents.");
            }

            Application application = allotment.getApplication();
            String applicantNo = application.getApplicant().getApplicantNo();
            Long applicationId = application.getApplicationId();

            ApplicationStatusResponseDTO status = new ApplicationStatusResponseDTO();
            status.setFormLocked(true);
            model.addAttribute("status", status);

            Map<String, String> requiredDocTypes = documentService.getRequiredDocumentTypes(applicantNo, applicationId);

            List<Document> uploadedDocuments = documentService.getUploadedDocuments(applicantNo);

            Map<String, Document> uploadedDocsMap = uploadedDocuments.stream()
                    .collect(Collectors.toMap(Document::getDocumentType, doc -> doc, (doc1, doc2) -> doc1));

            model.addAttribute("requiredDocTypes", requiredDocTypes);
            model.addAttribute("uploadedDocsMap", uploadedDocsMap);
            model.addAttribute("isDocumentsFinalized", true);

            return "applicant/fragments/document-review";

        } catch (jakarta.persistence.EntityNotFoundException e) {
            model.addAttribute("errorMessage", "Requested record not found.");
            return "fragments/error-message";

        } catch (SecurityException e) {
            model.addAttribute("errorMessage", "You are not authorized to view these documents.");
            return "fragments/error-message";

        } catch (org.springframework.dao.DataAccessException e) {
            model.addAttribute("errorMessage", "Unable to load documents. Please try again.");
            return "fragments/error-message";
        }
    }

    // =========================================================================
    // PAGED ALLOTMENTS (UPDATED WITH PROGRAMME FILTER)
    // =========================================================================
    @GetMapping("/paged")
    @ResponseBody
    public ResponseEntity<PagedResponse<InstituteAllotmentDTO>> getPagedAllotments(
            @RequestParam("statuses") List<AllotmentStatus> statuses,
            @RequestParam(value = "programmeId", required = false) Short programmeId, // <-- NEW PARAMETER
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        Short instituteId = instituteService.getInstituteIdByUsername(auth.getName());

        // Passed the programmeId into the counseling service
        PagedResponse<InstituteAllotmentDTO> response = counselingService.getPagedAllotmentsByStatus(instituteId,
                statuses, programmeId, page, size);

        return ResponseEntity.ok(response);
    }
}