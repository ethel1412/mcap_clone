package nic.meg.mcap.controllers.pageControllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.dto.response.StudentAllotmentResponseDTO;
import nic.meg.mcap.services.SeatAllotmentService;

@Controller
@RequestMapping("/institute")
public class InstituteStudentPageController {

	private static final Logger logger = LoggerFactory.getLogger(InstituteStudentPageController.class);

	@Autowired
	private SeatAllotmentService seatAllotmentService;

	@GetMapping("/view-applications")
	public String viewStudentApplications(Model model, Authentication authentication) {

		Short instituteId = getLoggedInInstituteId(authentication);

		if (instituteId == null) {
			model.addAttribute("errorMessage", "Unable to identify institute. Please login again.");
			return "admin/applications";
		}

		try {
			List<StudentAllotmentResponseDTO> studentAllotments = seatAllotmentService
					.getStudentAllotmentsByInstitute(instituteId.intValue());
			model.addAttribute("studentAllotments", studentAllotments);

		} catch (EntityNotFoundException ex) {
			model.addAttribute("errorMessage", ex.getMessage());

		}

		return "admin/applications";
	}

	private Short getLoggedInInstituteId(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof nic.meg.mcap.entities.User) {
			nic.meg.mcap.entities.User user = (nic.meg.mcap.entities.User) principal;

			// Get the enum value
			nic.meg.mcap.enums.OrgOwnerType orgOwnerType = user.getOrgOwnerType();

			// Compare with INSTITUTE enum value
			if (orgOwnerType == nic.meg.mcap.enums.OrgOwnerType.INSTITUTE) {
				Short orgOwnerId = user.getOrgOwnerId();
				if (orgOwnerId != null) {
					return orgOwnerId;
				} else {
					logger.info("OrgOwnerId is null for user: {}", user.getUsername());
				}
			} else {
				logger.info("User {} is not an INSTITUTE user. OrgOwnerType: {}", user.getUsername(), orgOwnerType);
			}
		} else {
			logger.info("Principal is not a User entity. Class: {}",
					principal != null ? principal.getClass().getName() : "null");
		}

		return null;
	}
}
