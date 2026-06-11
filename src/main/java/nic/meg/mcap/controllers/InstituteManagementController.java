package nic.meg.mcap.controllers;

import java.security.Principal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.enums.OrgOwnerType;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeOfferedService;
import nic.meg.mcap.services.ScheduleService;
import nic.meg.mcap.services.SeatAllotmentService;

@Controller
public class InstituteManagementController {

	private static final Logger logger = LoggerFactory.getLogger(InstituteManagementController.class);

	@Autowired
	private InstituteService instituteService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProgrammeOfferedService programmeOfferedService;

	@Autowired
	private ScheduleService scheduleService;

	@Autowired
	private SeatAllotmentService seatAllotmentService;

	@GetMapping("/institute-dashboard")
	public String showInstituteDashboard(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		}

		String username = principal.getName();
		Optional<User> userOptional = userRepository.findByUsername(username);

		if (userOptional.isEmpty()) {
			model.addAttribute("errorMessage", "Your user account could not be found.");
			return "error";
		}

		User loggedInUser = userOptional.get();

		if (loggedInUser.getOrgOwnerType() != OrgOwnerType.INSTITUTE || loggedInUser.getOrgOwnerId() == null) {
			model.addAttribute("errorMessage", "Access Denied: Your account is not associated with an institute.");
			return "access-denied";
		}
		Short instituteId = loggedInUser.getOrgOwnerId();
		Institute institute = instituteService.findById(instituteId);
		model.addAttribute("institute", institute);
		model.addAttribute("username", username);

		// Fetch dashboard statistics
		Long totalProgrammesOffered = programmeOfferedService.countByInstitute(instituteId);
		Long totalApplicants = seatAllotmentService.countAllotmentsByInstitute(instituteId);
		Long admissionsAccepted = seatAllotmentService.countAcceptedAllotmentsByInstitute(instituteId);

		model.addAttribute("totalProgrammesOffered", totalProgrammesOffered);
		model.addAttribute("totalApplicants", totalApplicants);
		model.addAttribute("admissionsAccepted", admissionsAccepted);
		return "institute-dashboard";

	}

	@GetMapping("/institute-notifications")
	public String showNotificationsPage(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		}

		String username = principal.getName();
		Optional<User> userOptional = userRepository.findByUsername(username);

		if (userOptional.isEmpty()) {
			return "redirect:/login";
		}

		User loggedInUser = userOptional.get();

		if (loggedInUser.getOrgOwnerType() != OrgOwnerType.INSTITUTE || loggedInUser.getOrgOwnerId() == null) {
			return "access-denied";
		}

		model.addAttribute("username", username);
		return "institute-notifications";
	}
}
