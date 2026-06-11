package nic.meg.mcap.controllers.pageControllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import nic.meg.mcap.dto.request.SeatMatrixRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeOfferedResponseDTO;
import nic.meg.mcap.dto.response.SeatMatrixResponseDTO;
import nic.meg.mcap.services.InstituteService;
import nic.meg.mcap.services.ProgrammeOfferedService;
import nic.meg.mcap.services.SeatMatrixService;

@Controller
@RequestMapping("/seat-matrix/page")
public class SeatMatrixPageController {

	private final SeatMatrixService seatMatrixService;
	private final ProgrammeOfferedService programmeOfferedService;
	private final InstituteService instituteService;

	public SeatMatrixPageController(SeatMatrixService seatMatrixService,
			ProgrammeOfferedService programmeOfferedService, InstituteService instituteService) {
		this.seatMatrixService = seatMatrixService;
		this.programmeOfferedService = programmeOfferedService;
		this.instituteService = instituteService;
	}

	@GetMapping("/home") // This is the endpoint the institute admin will access
	@Transactional(readOnly = true)
	public String seatMatrixPage(Model model, Principal principal) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

		List<ProgrammeOfferedResponseDTO> programmes = programmeOfferedService.listProgrammesByInstitute(instituteId);
		List<SeatMatrixResponseDTO> seatMatricesList = seatMatrixService.getByInstitute(instituteId);

		// Convert list of seat matrices to a map for efficient lookup in Thymeleaf
		Map<Integer, SeatMatrixResponseDTO> seatMatricesMap = seatMatricesList.stream().collect(Collectors
				.toMap(SeatMatrixResponseDTO::getProgrammeOfferedId, sm -> sm, (existing, replacement) -> existing));

		model.addAttribute("programmes", programmes);
		model.addAttribute("seatMatricesMap", seatMatricesMap); // Pass the map
		model.addAttribute("seatMatrixRequest", new SeatMatrixRequestDTO()); // For the form submission in the template

		return "admin/seat-allocation";
	}

	@PostMapping("/assign")
	@Transactional
	public String assignSeats(Principal principal, @ModelAttribute("seatMatrixRequest") SeatMatrixRequestDTO request) {
		Short instituteId = instituteService.findInstituteIdByUsername(principal.getName());

		try {
			// Enforce ownership via instituteId
			seatMatrixService.createOrUpdateSeatMatrix(request, instituteId);

			return "redirect:/seat-matrix/page/home?success";

		} catch (IllegalArgumentException ex) {
			return "redirect:/seat-matrix/page/home?error=invalid";
		}
	}
}