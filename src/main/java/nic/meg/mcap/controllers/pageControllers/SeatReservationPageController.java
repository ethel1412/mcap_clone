package nic.meg.mcap.controllers.pageControllers;

import nic.meg.mcap.dto.response.SeatReservationResponseDTO;
import nic.meg.mcap.entities.*;
import nic.meg.mcap.repositories.CommunityCategoryRepository;
import nic.meg.mcap.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.persistence.EntityNotFoundException;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/seat-reservations/page")
@PreAuthorize("hasRole('INSTITUTE')")
public class SeatReservationPageController {

    @Autowired
    private ProgrammeOfferedService programmeOfferedService;

    @Autowired
    private SeatReservationService seatReservationService;

    @Autowired
    private SeatMatrixService seatMatrixService;

    @Autowired
    private InstituteService instituteService;

    @Autowired
    private CommunityCategoryRepository communityCategoryRepository;

    @Autowired
    private AdmissionWindowService admissionWindowService;

    // CHANGED: {admissionWindowId} to {admissionCode}
    @GetMapping("/{programmeOfferedId}/{admissionCode}")
    public String showSeatReservationPage(
            @PathVariable Integer programmeOfferedId,
            // CHANGED: Short admissionWindowId to String admissionCode
            @PathVariable("admissionCode") String admissionCode,
            Model model,
            Principal principal) {

        // Verify security
        Short loggedInInstituteId = instituteService.findInstituteIdByUsername(principal.getName());

        // Load the specific programme
        ProgrammeOffered programmeOffered = programmeOfferedService.findById(programmeOfferedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Programme not found with ID: " + programmeOfferedId));

        // Security check
        if (!programmeOffered.getInstituteDepartment().getInstitute().getInstituteId()
                .equals(loggedInInstituteId)) {
            throw new SecurityException("Unauthorized access to this programme");
        }

        // Load seat matrix
        SeatMatrix seatMatrix = seatMatrixService.getSeatMatrixByProgrammeOfferedId(programmeOfferedId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Seat matrix not found for programme ID: " + programmeOfferedId));

        // CHANGED: Fetch the admission window by code
        AdmissionWindow admissionWindow = admissionWindowService.findByCode(admissionCode);
        if (admissionWindow == null) {
            throw new EntityNotFoundException("Admission Window not found with Code: " + admissionCode);
        }

        // EXTRACT internal ID for service calls
        Short admissionWindowId = admissionWindow.getAdmissionId();

        // Fetch DTOs (now include applicantType & reservedPercentage) using internal ID
        List<SeatReservationResponseDTO> reservations = seatReservationService
                .getReservationsByProgrammeAndWindow(programmeOfferedId, admissionWindowId);

        // Load community categories for dropdown
        List<CommunityCategory> communityCategories = communityCategoryRepository.findAll();

        // Calculate totals
        int totalReservedSeats = seatReservationService.getTotalReservedSeats(programmeOfferedId);
        int availableSeats = seatMatrix.getTotalSeats() - totalReservedSeats;

        model.addAttribute("admissionWindow", admissionWindow);

        // CHANGED: Add the admissionCode to the model so the template can use it
        model.addAttribute("admissionCode", admissionCode);

        model.addAttribute("programmeOffered", programmeOffered);
        model.addAttribute("seatMatrix", seatMatrix);
        model.addAttribute("reservations", reservations);
        model.addAttribute("communityCategories", communityCategories);
        model.addAttribute("totalSeats", seatMatrix.getTotalSeats());
        model.addAttribute("totalReservedSeats", totalReservedSeats);
        model.addAttribute("availableSeats", availableSeats);

        return "manage-programmes/seat-reservations";
    }
}