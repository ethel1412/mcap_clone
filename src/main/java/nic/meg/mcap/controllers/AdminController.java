package nic.meg.mcap.controllers;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.AddressDTO;
import nic.meg.mcap.dto.request.AdmissionWindowRequestDTO;
import nic.meg.mcap.dto.request.InstituteRequestDTO;
import nic.meg.mcap.dto.response.AdmissionWindowProgrammeResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.enums.InstituteStatus;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.repositories.InstituteRepository;
import nic.meg.mcap.repositories.ProgrammeRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.AdmissionWindowService;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private InstituteService instituteService;

    @Autowired
    private ProgrammeService programmeService;

    @Autowired
    private AdmissionWindowService admissionWindowService;

    @Autowired
    private InstituteRepository instituteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgrammeRepository programmeRepository;

    @InitBinder("AdmissionWindow")
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields("windowType", "streamId", "programmeLevel", "session", "startDate", "endDate",
                "programmeIds");
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        return "admin/dashboard";
    }

    /**
     * API endpoint to get dashboard statistics
     */
    @GetMapping("/api/dashboard-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalInstitutes", instituteRepository.count());
        stats.put("totalProgrammes", programmeRepository.count());

        stats.put("pendingInstitutes", instituteRepository.countByStatus(InstituteStatus.PENDING));
        stats.put("acceptedInstitutes", instituteRepository.countByStatus(InstituteStatus.ACCEPTED));
        stats.put("rejectedInstitutes", instituteRepository.countByStatus(InstituteStatus.REJECTED));
        stats.put("correctionRequiredInstitutes",
                instituteRepository.countByStatus(InstituteStatus.CORRECTION_REQUIRED));

        List<AdmissionWindow> allWindows = admissionWindowService.getAllAdmissionWindowsWithProgrammes();
        stats.put("totalAdmissionWindows", allWindows.size());

        long activeApplicationWindows = allWindows.stream().filter(w -> w.isActive()
                        && LocalDateTime.now().isAfter(w.getStartDate()) && LocalDateTime.now().isBefore(w.getEndDate()))
                .count();
        stats.put("activeAdmissionWindows", activeApplicationWindows);
        return ResponseEntity.ok(stats);
    }

    /**
     * API endpoint to get recent activity
     */
    @GetMapping("/api/recent-activity")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        List<Map<String, Object>> activities = new ArrayList<>();

        List<Institute> recentInstitutes = instituteRepository.findTop10ByOrderByInstituteIdDesc();

        for (Institute institute : recentInstitutes) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("time", java.time.LocalDateTime.now().minusDays(recentInstitutes.indexOf(institute)));
            activity.put("user", "Institute Admin");
            activity.put("action", "Institute registered: " + institute.getInstituteName());
            activity.put("status", institute.getStatus().name());
            activity.put("statusClass", getStatusBadgeClass(institute.getStatus().name()));
            activities.add(activity);
        }

        return ResponseEntity.ok(activities);
    }

    /**
     * Helper method to determine badge color based on status
     */
    private String getStatusBadgeClass(String status) {
        switch (status.toUpperCase()) {
            case "ACCEPTED":
            case "APPROVED":
            case "ACTIVE":
                return "bg-success";
            case "PENDING":
                return "bg-warning";
            case "REJECTED":
            case "FAILED":
                return "bg-danger";
            case "CORRECTION_REQUIRED":
                return "bg-info";
            default:
                return "bg-secondary";
        }
    }

    @GetMapping("/institutes-list")
    public String showAllInstitutes(@RequestParam(name = "status", required = false) String status, Model model) {
        List<Institute> institutes;
        if (StringUtils.hasText(status)) {
            institutes = instituteService.getInstitutesByStatus(status);
            model.addAttribute("filterStatus", status);
        } else {
            institutes = instituteService.getAllInstitutes();
        }

        institutes.forEach(institute -> {
            logger.info("Institute: " + institute.getInstituteName() + ", Status: [" + institute.getStatus() + "]");
        });

        model.addAttribute("institutes", institutes);
        model.addAttribute("activePage", "institutes-list");
        return "admin/institutes-list";
    }

    @PostMapping("/institute/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateStatus(@PathVariable Short id,
                                                            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        String reason = body.get("reason");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
        }

        String normalizedStatus = status.trim().toUpperCase();

        try {
            instituteService.updateStatus(id, normalizedStatus, reason);

            String message = switch (normalizedStatus) {
                case "ACCEPTED" -> "Institute application has been accepted successfully.";
                case "REJECTED" -> "Institute application has been rejected successfully.";
                case "CORRECTION_REQUIRED" -> "Application has been sent back for corrections successfully.";
                case "PENDING" -> "Institute application status has been set to pending.";
                default -> "Status updated successfully to " + status;
            };
            return ResponseEntity.ok(Map.of("message", message));

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating institute status for ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/institute/{id}/preview")
    @ResponseBody
    public Map<String, Object> previewInstitute(@PathVariable Short id) {
        try {
            Institute institute = instituteService.findById(id);
            if (institute == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute not found");
            }

            InstituteRequestDTO dto = new InstituteRequestDTO();
            dto.setInstituteId(institute.getInstituteId());
            dto.setInstituteName(institute.getInstituteName());
            dto.setAISHEId(institute.getAISHEId());
            dto.setYearEstablished(institute.getYearEstablished());
            dto.setBorderDistrictArea(institute.getBorderDistrictArea());
            dto.setUniversityName(institute.getUniversityName());
            dto.setInstitutionHeadDetails(institute.getInstitutionHeadDetails());
            dto.setInstitutionOfficialContactNumber(institute.getInstitutionOfficialContactNumber());
            dto.setInstitutionOfficialEmailId(institute.getInstitutionOfficialEmailId());
            dto.setInstitutionWebsite(institute.getInstitutionWebsite());
            dto.setStatus(institute.getStatus());
            dto.setAffiliationTypeId(institute.getAffiliationType().getAffiliationTypeId());
            dto.setManagementTypeId(institute.getManagementType().getManagementTypeId());

            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setAddressLine1(institute.getAddress().getAddressLine1());
            addressDTO.setAddressLine2(institute.getAddress().getAddressLine2());
            addressDTO.setPincode(institute.getAddress().getPincode());
            addressDTO.setStateCode(institute.getAddress().getBlock().getDistrict().getState().getStateCode());
            addressDTO.setDistrictCode(institute.getAddress().getBlock().getDistrict().getDistrictCode());
            addressDTO.setBlockCode(institute.getAddress().getBlock().getBlockCode());
            dto.setAddressDTO(addressDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("institute", dto);
            response.put("affiliationTypeName", institute.getAffiliationType().getAffiliationTypeName());
            response.put("managementTypeName", institute.getManagementType().getManagementTypeName());
            response.put("stateName", institute.getAddress().getBlock().getDistrict().getState().getStateName());
            response.put("districtName", institute.getAddress().getBlock().getDistrict().getDistrictName());
            response.put("blockName", institute.getAddress().getBlock().getBlockName());

            logger.info("Successfully generated preview data for institute ID {}: {}", id,
                    institute.getInstituteName());
            return response;

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute not found");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading institute details");
        }
    }

    @GetMapping("/manage-programmes")
    public String showManageProgrammesPage(Model model) {
        model.addAttribute("activePage", "manage-programmes");
        return "admin/manage-programmes";
    }

    @GetMapping("/applications")
    public String showApplicationsPage(Model model) {
        model.addAttribute("activePage", "applications");
        return "admin/applications";
    }

    @GetMapping("/merit-lists")
    public String showMeritListsPage(Model model) {
        model.addAttribute("activePage", "merit-lists");
        return "admin/merit-lists";
    }

    @GetMapping("/reports")
    public String showReportsPage(Model model) {
        model.addAttribute("activePage", "reports");
        return "admin/reports";
    }

    @GetMapping("/admission-management")
    public String showAdmissionManagementPage(@RequestParam(name = "status", required = false) String status,
                                              Model model) {
        List<String> sessionYears = new ArrayList<>();
        int currentYear = Year.now().getValue();
        for (int i = -1; i <= 2; i++) {
            int startYear = currentYear + i;
            sessionYears.add(startYear + "-" + (startYear + 1));
        }

        List<AdmissionWindow> windows;
        if (StringUtils.hasText(status)) {
            windows = admissionWindowService.getWindowsByStatus(status);
            model.addAttribute("filterStatus", status);
        } else {
            windows = admissionWindowService.getAllAdmissionWindowsWithProgrammes();
        }

        model.addAttribute("streams", admissionWindowService.getAllStreams());
        model.addAttribute("windows", windows);
        model.addAttribute("programmeLevels", ProgrammeLevel.values());
        model.addAttribute("sessionYears", sessionYears);
        model.addAttribute("AdmissionWindow", new AdmissionWindowRequestDTO());
        model.addAttribute("activePage", "admission-management");

        return "admin/admission-management";
    }

    @GetMapping("/api/programmes")
    @ResponseBody
    public List<Programme> getAllProgrammesApi() {
        return programmeService.getAllProgrammes();
    }

    @GetMapping("/admission-window/{admissionCode}")
    @ResponseBody
    public AdmissionWindowRequestDTO getAdmissionWindow(@PathVariable("admissionCode") String admissionCode) {
        return admissionWindowService.getAdmissionWindowForEdit(admissionCode);
    }

    @PostMapping("/save-admission-window")
    public String saveAdmissionWindow(@Valid @ModelAttribute("AdmissionWindow") AdmissionWindowRequestDTO dto,
                                      BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            logger.error("Validation Errors in admission window creation:");
            bindingResult.getAllErrors().forEach(error -> logger.error(error.getDefaultMessage()));
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            redirectAttributes.addFlashAttribute("errorMessage", "Validation failed: " + errorMessage);
            return "redirect:/admin/admission-management";
        }

        try {
            admissionWindowService.saveAdmissionWindow(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Admission window created successfully!");

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error while creating admission window: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Database error while creating admission window", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to save data. Please try again.");
        }

        return "redirect:/admin/admission-management";
    }

    @PostMapping("/update-admission-window/{admissionCode}")
    public String updateAdmissionWindow(@PathVariable("admissionCode") String admissionCode,
                                        @ModelAttribute("AdmissionWindow") AdmissionWindowRequestDTO dto,
                                        RedirectAttributes redirectAttributes) {

        try {
            admissionWindowService.updateAdmissionWindow(admissionCode, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Admission window updated successfully!");

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating admission window {}: {}", admissionCode, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Database error updating admission window {}", admissionCode, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update data. Please try again.");
        }

        return "redirect:/admin/admission-management";
    }

    @PostMapping("/delete-admission-window/{admissionCode}")
    public String deleteAdmissionWindow(@PathVariable("admissionCode") String admissionCode,
                                        RedirectAttributes redirectAttributes) {
        try {
            admissionWindowService.deleteAdmissionWindow(admissionCode);
            redirectAttributes.addFlashAttribute("successMessage", "Admission window deleted successfully!");

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error deleting admission window {}: {}", admissionCode, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (org.springframework.dao.DataAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to delete the record. Please try again.");
        }
        return "redirect:/admin/admission-management";
    }

    @PostMapping("/toggle-window-status/{admissionCode}")
    public String toggleWindowStatus(@PathVariable("admissionCode") String admissionCode,
                                     RedirectAttributes redirectAttributes) {
        try {
            admissionWindowService.toggleIsActive(admissionCode);
            redirectAttributes.addFlashAttribute("successMessage", "Window status updated.");

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error toggling window status {}: {}", admissionCode, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Database error toggling window status {}", admissionCode, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update status. Please try again.");
        }
        return "redirect:/admin/admission-management";
    }

    /**
     * Endpoint to extend the application window deadline
     */
    @PostMapping("/admission-window/{admissionCode}/extend")
    public String extendAdmissionWindow(@PathVariable("admissionCode") String admissionCode,
                                        @RequestParam("newEndDate") String newEndDateStr,
                                        RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime newEndDate = LocalDateTime.parse(newEndDateStr);
            admissionWindowService.extendWindow(admissionCode, newEndDate);
            redirectAttributes.addFlashAttribute("successMessage", "Application deadline extended successfully!");

        } catch (java.time.format.DateTimeParseException e) {
            logger.warn("Invalid date format for admission window {}: {}", admissionCode, newEndDateStr);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format. Please use the correct format.");

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error extending window {}: {}", admissionCode, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (org.springframework.dao.DataAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to extend deadline. Please try again.");
        }
        return "redirect:/admin/admission-management";
    }

    @GetMapping("/admission-window/{admissionCode}/programmes")
    @ResponseBody
    public List<AdmissionWindowProgrammeResponseDTO> getProgrammesForWindow(
            @PathVariable("admissionCode") String admissionCode) {
        return admissionWindowService.getProgrammesForWindow(admissionCode);
    }

    @PostMapping("/admission-window/programme/delete/{id}")
    public String deleteProgrammeFromWindow(@PathVariable("id") Short admissionWindowProgrammeId,
                                            RedirectAttributes redirectAttributes) {
        try {
            boolean windowWasDeleted = admissionWindowService.removeProgrammeFromWindow(admissionWindowProgrammeId);

            if (windowWasDeleted) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "The last programme was removed, and the entire admission window was deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Programme removed from window successfully.");
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error removing programme {}: {}", admissionWindowProgrammeId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Database error removing programme {}", admissionWindowProgrammeId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to remove programme. Please try again.");
        }
        return "redirect:/admin/admission-management";
    }

    @PostMapping("/admission-window/programme/toggle/{id}")
    public String toggleProgrammeInWindow(@PathVariable("id") Short admissionWindowProgrammeId,
                                          RedirectAttributes redirectAttributes) {
        try {
            admissionWindowService.toggleProgrammeStatusInWindow(admissionWindowProgrammeId);
            redirectAttributes.addFlashAttribute("successMessage", "Programme status updated successfully.");

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating programme status {}: {}", admissionWindowProgrammeId,
                    e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (org.springframework.dao.DataAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unable to update programme status. Please try again.");
        }
        return "redirect:/admin/admission-management";
    }

    @GetMapping("/count-pending-institutes")
    @ResponseBody
    public ResponseEntity<Long> getPendingInstituteCount() {
        return ResponseEntity.ok(instituteRepository.countByStatus(InstituteStatus.PENDING));
    }

    /**
     * Toggle the active/inactive status of an institute.
     * CSRF protection is provided by Spring Security (default).
     */
    @PostMapping("/institutes/{id}/toggle-active")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleInstituteActive(@PathVariable("id") Short instituteId) {
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute not found"));

        boolean newStatus = !institute.isActive();
        institute.setActive(newStatus);
        instituteRepository.saveAndFlush(institute);

        logger.info("Institute {} (ID: {}) toggled to isActive={}", institute.getInstituteName(), instituteId, newStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("isActive", newStatus);
        response.put("message", newStatus ? "Institute is now Active." : "Institute is now Inactive.");
        return ResponseEntity.ok(response);
    }
}