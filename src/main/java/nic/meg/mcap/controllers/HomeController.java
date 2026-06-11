package nic.meg.mcap.controllers;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.response.ProgrammeOfferedResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.services.AdmissionWindowService;
import nic.meg.mcap.services.ProgrammeOfferedService;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final AdmissionWindowService admissionWindowService;
	private final ProgrammeOfferedService programmeOfferedService;

	@GetMapping("/")
	public String home(Model model) {

		List<AdmissionWindow> ongoingWindows = Optional.ofNullable(admissionWindowService.getWindowsByStatus("active"))
				.orElse(List.of());

		List<AdmissionWindow> upcomingWindows = Optional
				.ofNullable(admissionWindowService.getWindowsByStatus("upcoming")).orElse(List.of());

		model.addAttribute("ugOngoingAdmissions", filterByLevel(ongoingWindows, ProgrammeLevel.UG));

		model.addAttribute("pgOngoingAdmissions", filterByLevel(ongoingWindows, ProgrammeLevel.PG));

		model.addAttribute("ugUpcomingAdmissions", filterByLevel(upcomingWindows, ProgrammeLevel.UG));

		model.addAttribute("pgUpcomingAdmissions", filterByLevel(upcomingWindows, ProgrammeLevel.PG));

		return "home";
	}

	private List<AdmissionWindow> filterByLevel(List<AdmissionWindow> windows, ProgrammeLevel level) {

		if (windows == null || windows.isEmpty()) {
			return List.of();
		}

		return windows.stream().filter(w -> w != null && w.getProgrammeLevel() != null)
				.filter(w -> matchesLevel(w.getProgrammeLevel(), level)).toList();
	}

	private boolean matchesLevel(ProgrammeLevel actual, ProgrammeLevel expected) {
		return switch (expected) {
		case UG -> actual == ProgrammeLevel.UG || actual == ProgrammeLevel.FYUG;
		case PG -> actual == ProgrammeLevel.PG;
		default -> actual == expected;
		};
	}

	@GetMapping("/login")
	public String login(Model model, Authentication authentication, HttpServletResponse response) {

		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);

		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {

			Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

			for (GrantedAuthority authority : authorities) {
				if (authority.getAuthority().equals("ROLE_INSTITUTE")) {
					return "redirect:/institute-dashboard";
				}

				if (authority.getAuthority().equals("ROLE_ADMIN")) {
					return "redirect:/admin/dashboard";
				}

				if (authority.getAuthority().equals("ROLE_CONTROLLER")) {
					return "redirect:/control-panel/dashboard";
				}

				if (authority.getAuthority().equals("ROLE_APPLICANT")) {
					return "redirect:/applicants/dashboard";
				}
			}
		}
		model.addAttribute("maxDob", LocalDate.now().minusYears(12));
		return "login";
	}

	@GetMapping("/participating-institutes")
	public String participatingInstituesPage(Model model) {

		List<ProgrammeOfferedResponseDTO> list = programmeOfferedService.getAllProgrammesOffered();

		Map<String, Map<String, Map<String, List<ProgrammeOfferedResponseDTO>>>> grouped = list.stream()
				.collect(Collectors.groupingBy(ProgrammeOfferedResponseDTO::getInstituteName, LinkedHashMap::new,
						Collectors.groupingBy(ProgrammeOfferedResponseDTO::getDepartmentName, LinkedHashMap::new,
								Collectors.groupingBy(ProgrammeOfferedResponseDTO::getProgrammeName, LinkedHashMap::new,
										Collectors.toList()))));

		// ✅ University map (safe)
		Map<String, String> universityMap = list.stream().collect(Collectors.toMap(dto -> dto.getInstituteName().trim(),
				dto -> Optional.ofNullable(dto.getUniversityName()).orElse("NA"), (a, b) -> a));

		// ✅ FIXED: Prospectus map (handles null properly)
		Map<String, String> prospectusMap = list.stream().collect(Collectors.toMap(dto -> dto.getInstituteName().trim(),
				dto -> Optional.ofNullable(dto.getProspectusUrl()).orElse(""), (a, b) -> a));

		model.addAttribute("institutesData", grouped);
		model.addAttribute("universityMap", universityMap);
		model.addAttribute("prospectusMap", prospectusMap);

		return "participating-institutes";
	}

	@GetMapping("/admission-guidelines")
	public String admissionGuidelinesPage() {
		return "admission-guidelines";
	}

	@GetMapping("/about")
	public String aboutPage() {
		return "about";
	}

	@GetMapping("/notifications")
	public String notificationsPage() {
		return "notifications";
	}

	@GetMapping("/contact-us")
	public String contactPage() {
		return "contact-us";
	}

	@GetMapping("/faq")
	public String faqPage() {
		return "faq";
	}
}