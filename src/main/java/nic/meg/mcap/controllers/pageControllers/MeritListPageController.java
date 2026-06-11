package nic.meg.mcap.controllers.pageControllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nic.meg.mcap.dto.response.*;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.Programme;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/merit-list/page")
@RequiredArgsConstructor
@Slf4j
public class MeritListPageController {

    private final MeritListService meritListService;
    private final ProgrammeOfferedService programmeOfferedService;
    private final ObjectMapper objectMapper;
    private final ProgrammeService programmeService;
    private final StreamService streamService;
    private final AdmissionWindowService admissionWindowService;

    @GetMapping("/view")
    public String viewMeritListsWithTabs(
            // CHANGED: Short admissionWindowId to String admissionWindowCode
            @RequestParam("admissionWindowCode") String admissionWindowCode,
            @RequestParam(required = false) Short streamId,
            @RequestParam(required = false) Short programmeId,
            @RequestParam ProgrammeLevel programmeLevel,
            Model model
    ) {
        try {
            List<MeritListResponseDTO> meritLists;

            if (programmeLevel == ProgrammeLevel.UG && streamId != null) {
                // CHANGED: Passed admissionWindowCode instead of ID
                meritLists = meritListService.getAllMeritListsForUGStream(admissionWindowCode, streamId);

                // Filter by programme if provided (UG generates per-programme lists)
                if (programmeId != null) {
                    meritLists = meritLists.stream()
                            .filter(ml -> ml.getMetadata().getProgrammeId() != null
                                    && ml.getMetadata().getProgrammeId().equals(programmeId))
                            .collect(Collectors.toList());
                }
            } else if (programmeLevel == ProgrammeLevel.PG && programmeId != null) {
                // CHANGED: Passed admissionWindowCode instead of ID
                meritLists = meritListService.getAllMeritListsForPGProgramme(admissionWindowCode, programmeId);
            } else {
                throw new IllegalArgumentException("Invalid parameters for merit list view");
            }

            // Separate lists by type for tab display
            MeritListResponseDTO withEntranceList = meritLists.stream()
                    .filter(ml -> "WITH_ENTRANCE".equals(ml.getMetadata().getApplicantType()))
                    .findFirst()
                    .orElse(null);

            MeritListResponseDTO withoutEntranceList = meritLists.stream()
                    .filter(ml -> "WITHOUT_ENTRANCE".equals(ml.getMetadata().getApplicantType()))
                    .findFirst()
                    .orElse(null);

            // Primary metadata for header (prefer WITH_ENTRANCE, else WITHOUT)
            MeritListMetadataDTO primaryMetadata = withEntranceList != null
                    ? withEntranceList.getMetadata()
                    : (withoutEntranceList != null ? withoutEntranceList.getMetadata() : null);

            log.info("withEntranceList present: {}", withEntranceList != null);
            log.info("withoutEntranceList present: {}", withoutEntranceList != null);
            log.info("primaryMetadata: {}", primaryMetadata);

            // Institutes offering this programme
            List<ProgrammeOfferedResponseDTO> institutes = null;
            if (programmeId != null) {
                institutes = programmeOfferedService.findInstitutesByProgramme(programmeId);
            }

            Programme programme = programmeService.getProgrammeById(programmeId);
            StreamResponseDTO stream = streamService.getStreamById(streamId);

            // CHANGED: Fetch by code instead of ID
            AdmissionWindow window = admissionWindowService.findByCode(admissionWindowCode);

            model.addAttribute("headerProgrammeName", programme.getProgrammeName());
            model.addAttribute("headerStreamName", stream != null ? stream.getStreamName() : null);
            model.addAttribute("headerAdmissionWindowName", window.getSession());
            model.addAttribute("withEntranceList", withEntranceList);
            model.addAttribute("withoutEntranceList", withoutEntranceList);
            model.addAttribute("programmeLevel", programmeLevel);

            // CHANGED: Added admissionWindowCode to the model so the template has it
            model.addAttribute("admissionWindowCode", admissionWindowCode);

            model.addAttribute("streamId", streamId);
            model.addAttribute("programmeId", programmeId);
            model.addAttribute("primaryMetadata", primaryMetadata);
            model.addAttribute("institutes", institutes);

            // Serialize entries to JSON for JavaScript
            if (withEntranceList != null) {
                try {
                    String entriesJson = objectMapper.writeValueAsString(withEntranceList.getEntries());
                    model.addAttribute("withEntranceEntriesJson", entriesJson);
                } catch (Exception e) {
                    log.error("Failed to serialize WITH_ENTRANCE entries", e);
                    model.addAttribute("withEntranceEntriesJson", "[]");
                }
            }

            if (withoutEntranceList != null) {
                try {
                    String entriesJson = objectMapper.writeValueAsString(withoutEntranceList.getEntries());
                    model.addAttribute("withoutEntranceEntriesJson", entriesJson);
                } catch (Exception e) {
                    log.error("Failed to serialize WITHOUT_ENTRANCE entries", e);
                    model.addAttribute("withoutEntranceEntriesJson", "[]");
                }
            }

            return "merit-list/view-merit-list";
        } catch (Exception e) {
            log.error("Error loading merit lists: {}", e.getMessage(), e);
            model.addAttribute("error", "Merit lists not found");
            return "error/404";
        }
    }
}