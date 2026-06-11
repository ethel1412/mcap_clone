package nic.meg.mcap.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import nic.meg.mcap.dto.request.InstituteRequestDTO;
import nic.meg.mcap.dto.request.InstituteStatusRequestDTO;
import nic.meg.mcap.dto.response.InstituteStatusResponseDTO;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.exception.InstituteNotFoundException;
import nic.meg.mcap.services.AffiliationTypeService;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ManagementTypeService;
import nic.meg.mcap.services.MasterService;

@Controller
@RequestMapping("/institute")
public class InstituteStatusController {

	@Autowired
	private InstituteService instituteService;

	@Autowired
	private AffiliationTypeService affiliationTypeService;

	@Autowired
	private ManagementTypeService managementTypeService;

	@Autowired
	private MasterService masterService;

	private static final Logger logger = LoggerFactory.getLogger(InstituteStatusController.class);

	@GetMapping("/status")
	public String showStatusForm(Model model) {
		model.addAttribute("statusResult", new InstituteStatusResponseDTO());
		model.addAttribute("identifier", ""); // Initialize identifier for the input field
		return "institute-status";
	}

	@PostMapping("/status")
	public String checkStatus(
	        @Valid @ModelAttribute("request")
	        InstituteStatusRequestDTO request,
	        BindingResult result,
	        Model model) {

	    // Always keep statusResult to avoid null issues in Thymeleaf
	    InstituteStatusResponseDTO statusResult =
	            new InstituteStatusResponseDTO();

	    model.addAttribute("statusResult", statusResult);

	    // Additional SQLi heuristic protection
	    if (containsSqlMetaCharacters(
	            request.getIdentifier(),
	            request.getEmail(),
	            request.getMobile())) {

	        model.addAttribute(
	                "errorMessage",
	                "Invalid input provided.");

	        return "institute-status";
	    }

	    // Validation failure
	    if (result.hasErrors()) {

	        model.addAttribute(
	                "errorMessage",
	                "Invalid input provided.");

	        return "institute-status";
	    }

	    try {

	        statusResult =
	                instituteService.checkInstituteStatus(request);

	        model.addAttribute("statusResult", statusResult);

	        String status = statusResult.getStatus();

	        switch (status.toUpperCase()) {

	        case "NOT_FOUND":

	            model.addAttribute(
	                    "errorMessage",
	                    statusResult.getMessage());

	            break;

	        case "REJECTED":

	            model.addAttribute(
	                    "warningMessage",
	                    statusResult.getMessage());

	            break;

	        case "CORRECTION_REQUIRED":

	            model.addAttribute(
	                    "correctionMessage",
	                    statusResult.getMessage());

	            break;

	        case "PENDING":

	            model.addAttribute(
	                    "infoMessage",
	                    statusResult.getMessage());

	            break;

	        case "ACCEPTED":

	            model.addAttribute(
	                    "successMessage",
	                    statusResult.getMessage());

	            break;

	        default:

	            model.addAttribute(
	                    "infoMessage",
	                    "Request processed.");

	            break;
	        }

	    } catch (InstituteNotFoundException ex) {

	        model.addAttribute(
	                "errorMessage",
	                "Unable to process request.");

	    } catch (DataAccessException ex) {

	        model.addAttribute(
	                "errorMessage",
	                "Unable to process request.");

	    } catch (IllegalArgumentException ex) {

	        model.addAttribute(
	                "errorMessage",
	                "Invalid input provided.");
	    }

	    return "institute-status";
	}

	private boolean containsSqlMetaCharacters(String... values) {

	    for (String value : values) {

	        if (value == null) {
	            continue;
	        }

	        String lower = value.toLowerCase();

	        if (lower.contains("'")
	                || lower.contains("\"")
	                || lower.contains("--")
	                || lower.contains(";")
	                || lower.contains("/*")
	                || lower.contains("*/")
	                || lower.contains(" union ")
	                || lower.contains(" select ")
	                || lower.contains(" insert ")
	                || lower.contains(" delete ")
	                || lower.contains(" drop ")
	                || lower.contains(" xp_")) {

	            return true;
	        }
	    }

	    return false;
	}

	@GetMapping("/correction/{id}")
	public String showCorrectionForm(@PathVariable("id") Short instituteId, Model model) {
		try {
			// Check if institute exists first
			if (instituteId == null || instituteId <= 0) {
				model.addAttribute("errorMessage", "Invalid institute ID provided.");
				model.addAttribute("statusResult", new InstituteStatusResponseDTO());
				return "institute-status";
			}

			Institute institute = instituteService.findById(instituteId);
			if (institute == null) {
				model.addAttribute("errorMessage", "Institute not found with the provided ID.");
				model.addAttribute("statusResult", new InstituteStatusResponseDTO());
				return "institute-status";
			}

			InstituteRequestDTO dto = instituteService.convertToRequestDTO(institute);

			// Must match th:object in template
			model.addAttribute("institute", dto);
			model.addAttribute("correctionMode", true);
			model.addAttribute("infoMessage", "Please update the highlighted fields and resubmit your application.");

			// Populate dropdowns
			model.addAttribute("affiliationTypes", affiliationTypeService.getAll());
			model.addAttribute("managementTypes", managementTypeService.getAll());
			model.addAttribute("states", masterService.getListStates());

			if (dto.getAddressDTO() != null) {
				Short state = dto.getAddressDTO().getStateCode();
				Short district = dto.getAddressDTO().getDistrictCode();
				if (state != null) {
					model.addAttribute("districts", masterService.getListOfDistrict(state));
				}
				if (district != null) {
					model.addAttribute("blocks", masterService.getListOfBlocks(district));
				}
			}
			
			return "institute-form";

		} catch (IllegalArgumentException e) {
			model.addAttribute("errorMessage", "Institute not found. Please check the ID and try again.");
			model.addAttribute("statusResult", new InstituteStatusResponseDTO());
			return "institute-status";
		} catch (Exception e) {
			model.addAttribute("errorMessage", "Unable to load correction form. Please contact support.");
			model.addAttribute("statusResult", new InstituteStatusResponseDTO());
			return "institute-status";
		}
	}

}