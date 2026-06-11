package nic.meg.mcap.controllers;

import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.AddressDTO;
import nic.meg.mcap.dto.request.InstituteRequestDTO;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.repositories.InstituteRepository;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.SeatAllotmentRepository;
import nic.meg.mcap.services.AffiliationTypeService;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ManagementTypeService;
import nic.meg.mcap.services.MasterService;
import nic.meg.mcap.services.SeatAllotmentService;

@Controller
public class InstituteController {

    private static final Logger logger = LoggerFactory.getLogger(InstituteController.class);

    @Autowired
    private AffiliationTypeService affiliationTypeService;

    @Autowired
    private ManagementTypeService managementTypeService;

    @Autowired
    private InstituteService instituteService;

    @Autowired
    private MasterService masterService;

    @Autowired
    private ProgrammesOfferedRepository programmesOfferedRepository;

    @Autowired
    private SeatAllotmentRepository seatAllotmentRepository;

    @Autowired
    private SeatAllotmentService seatAllotmentService;

    // ← NEW
    @Autowired
    private InstituteRepository instituteRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/institute-form")
    public String showInstituteForm(Model model) {
        if (!model.containsAttribute("institute")) {
            InstituteRequestDTO institute = new InstituteRequestDTO();
            institute.setAddressDTO(new AddressDTO());
            model.addAttribute("institute", institute);
        }
        model.addAttribute("affiliationTypes", affiliationTypeService.getAll());
        model.addAttribute("managementTypes", managementTypeService.getAll());
        model.addAttribute("states", masterService.getListStates());
        return "institute-form";
    }

    @PostMapping("/submit")
    public String submitInstitute(@ModelAttribute("institute") @Valid InstituteRequestDTO instituteDTO,
                                  BindingResult result, RedirectAttributes redirectAttrs, Model model) {
    	
        if (result.hasErrors()) {
            populateDropdownsForErrorState(model, instituteDTO);
            model.addAttribute("globalError", "Please correct the errors below.");
            return "institute-form";
        }
        
    	if (instituteDTO.getAISHEId() != null && !instituteDTO.getAISHEId().isBlank()) {
            instituteDTO.setAISHEId(instituteDTO.getAISHEId().trim().toUpperCase());
        }

        Short currentInstituteId = instituteDTO.getInstituteId();

        if (instituteDTO.getAISHEId() != null && !instituteDTO.getAISHEId().isBlank()
                && !instituteService.isAisheIdUnique(instituteDTO.getAISHEId(), currentInstituteId)) {
            result.rejectValue("AISHEId", "duplicate.aisheId", "This AISHE ID already exists.");
        }
        if (instituteDTO.getInstitutionOfficialEmailId() != null
                && !instituteDTO.getInstitutionOfficialEmailId().isBlank()
                && !instituteService.isEmailUnique(instituteDTO.getInstitutionOfficialEmailId(), currentInstituteId)) {
            result.rejectValue("institutionOfficialEmailId", "duplicate.email",
                    "This Email address is already registered.");
        }
        if (instituteDTO.getInstitutionOfficialContactNumber() != null
                && !instituteDTO.getInstitutionOfficialContactNumber().isBlank()
                && !instituteService.isContactNumberUnique(instituteDTO.getInstitutionOfficialContactNumber(),
                currentInstituteId)) {
            result.rejectValue("institutionOfficialContactNumber", "duplicate.contactNumber",
                    "This Contact Number is already registered.");
        }
        if (instituteDTO.getInstitutionWebsite() != null && !instituteDTO.getInstitutionWebsite().isBlank()
                && !instituteService.isWebsiteUnique(instituteDTO.getInstitutionWebsite(), currentInstituteId)) {
            result.rejectValue("institutionWebsite", "duplicate.website", "This Website is already registered.");
        }

        if (result.hasErrors()) {
            populateDropdownsForErrorState(model, instituteDTO);
            model.addAttribute("globalError", "Please correct the errors below.");
            return "institute-form";
        }
        
        try {
            instituteService.saveInstitute(instituteDTO);
            redirectAttrs.addFlashAttribute("submissionSuccess", true);

            if (currentInstituteId != null) {
                redirectAttrs.addFlashAttribute("correctionSubmitted", true);
                redirectAttrs.addFlashAttribute("successMessage",
                        "Your corrections have been submitted successfully. Your application is now pending review.");
                String identifier = (instituteDTO.getAISHEId() != null && !instituteDTO.getAISHEId().isBlank())
                        ? instituteDTO.getAISHEId()
                        : instituteDTO.getInstitutionOfficialEmailId();
                return "redirect:/institute/status?identifier=" + identifier;
            } else {
                redirectAttrs.addFlashAttribute("successMessage",
                        "Your application has been submitted successfully. You can check its status using your AISHE ID or Email.");
                return "redirect:/institute-form";
            }

        } catch (IllegalArgumentException e) {
            model.addAttribute("globalError", e.getMessage());
            populateDropdownsForErrorState(model, instituteDTO);
            return "institute-form";

        } catch (DataAccessException e) {
            redirectAttrs.addFlashAttribute("errorMessage", "A database error occurred. Please try again later.");
            return (currentInstituteId != null) ? "redirect:/institute/correction/" + currentInstituteId
                    : "redirect:/institute-form";

        } catch (Exception e) {
            if (currentInstituteId != null) {
                redirectAttrs.addFlashAttribute("errorMessage",
                        "Failed to submit your corrections due to an internal error. Please try again.");
                return "redirect:/institute/correction/" + currentInstituteId;
            } else {
                model.addAttribute("globalError",
                        "Registration failed due to an internal error. Please try again or contact support.");
                populateDropdownsForErrorState(model, instituteDTO);
                return "institute-form";
            }
        }
    }

    private void populateDropdownsForErrorState(Model model, InstituteRequestDTO instituteDTO) {
        model.addAttribute("affiliationTypes", affiliationTypeService.getAll());
        model.addAttribute("managementTypes", managementTypeService.getAll());
        model.addAttribute("states", masterService.getListStates());

        if (instituteDTO.getAddressDTO() != null) {
            Short selectedStateCode = instituteDTO.getAddressDTO().getStateCode();
            Short selectedDistrictCode = instituteDTO.getAddressDTO().getDistrictCode();
            if (selectedStateCode != null) {
                model.addAttribute("districts", masterService.getListOfDistrict(selectedStateCode));
            }
            if (selectedDistrictCode != null) {
                model.addAttribute("blocks", masterService.getListOfBlocks(selectedDistrictCode));
            }
        }

        if (instituteDTO.getInstituteId() != null) {
            model.addAttribute("correctionMode", true);
            model.addAttribute("infoMessage", "Please fix the errors below and resubmit your corrections.");
        }
    }

    @PostMapping("/api/institute/prospectus/save")
    @ResponseBody
    public ResponseEntity<?> saveProspectusLink(@RequestBody Map<String, String> payload, Principal principal) {
        String prospectusUrl = payload.get("prospectusUrl");

        if (prospectusUrl == null || prospectusUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "URL cannot be empty."));
        }

        try {
            new URL(prospectusUrl).toURI();
            if (!prospectusUrl.startsWith("http://") && !prospectusUrl.startsWith("https://")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Must be an HTTP or HTTPS link."));
            }
            String username = principal.getName();
            instituteService.updateProspectusUrl(username, prospectusUrl.trim());
            return ResponseEntity.ok().body(Map.of("message", "Prospectus link saved successfully!"));

        } catch (java.net.MalformedURLException | java.net.URISyntaxException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid URL format. Please ensure it starts with http:// or https://"));
        }
    }

    @GetMapping("/api/institute/dashboard-data")
    @ResponseBody
    public ResponseEntity<?> getDashboardData(Principal principal) {
        Short instituteId = instituteService.getInstituteIdByUsername(principal.getName());
        Institute inst = instituteService.findById(instituteId);

        long totalProgrammes = programmesOfferedRepository
                .countByInstituteDepartment_Institute_InstituteId(instituteId);
        long totalApplicants = seatAllotmentRepository.countByInstituteId(instituteId);
        long admissionsAccepted = seatAllotmentService.countAcceptedAllotmentsByInstitute(instituteId);

        Map<String, Object> data = new HashMap<>();
        data.put("institute", instituteService.convertToRequestDTO(inst));
        data.put("stats", Map.of("totalProgrammes", totalProgrammes, "totalApplicants", totalApplicants,
                "admissionsAccepted", admissionsAccepted));

        return ResponseEntity.ok(data);
    }

    @PostMapping("/institute/profile/update")
    public String updateProfile(@Valid @ModelAttribute("inst") InstituteRequestDTO dto, BindingResult result,
                                Model model) {

        if (result.hasErrors()) {
            return "institute/institute-profile";
        }
        instituteService.saveInstitute(dto);
        return "redirect:/institute/profile?success";
    }
}