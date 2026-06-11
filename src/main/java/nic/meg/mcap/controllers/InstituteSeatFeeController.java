package nic.meg.mcap.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nic.meg.mcap.dto.request.InstituteSeatFeeStructureRequestDTO;
import nic.meg.mcap.dto.response.InstituteSeatFeeStructureResponseDTO;
import nic.meg.mcap.entities.ProgrammeOffered;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.entities.User;
import nic.meg.mcap.repositories.ProgrammesOfferedRepository;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.repositories.UserRepository;
import nic.meg.mcap.services.InstituteSeatFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/institute/seat-fee")
@PreAuthorize("hasRole('INSTITUTE')")
@RequiredArgsConstructor
public class InstituteSeatFeeController {

    private final InstituteSeatFeeService seatFeeService;
    private final UserRepository userRepository;
    private final ProgrammesOfferedRepository programmesOfferedRepository;
    private final StreamRepository streamRepository;

    /** Main page: table of all fee structures */
    @GetMapping
    public String showSeatFeePage(Principal principal, Model model, HttpServletRequest request) {
        User user = getUser(principal);
        List<InstituteSeatFeeStructureResponseDTO> structures =
                seatFeeService.getStructuresByUserId(user.getUserId());
        model.addAttribute("structures", structures);

        List<ProgrammeOffered> programmes = getProgrammesForUser(user);
        model.addAttribute("programmes", programmes);

        List<Stream> streams = streamRepository.findAll();
        model.addAttribute("streams", streams);

        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            model.addAttribute("_csrf", csrf);
            model.addAttribute("_csrf_header", csrf.getHeaderName());
        }

        return "institute/seat-fee-management";
    }

    /** REST: list all structures as JSON */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> list(Principal principal) {
        User user = getUser(principal);
        return ok(seatFeeService.getStructuresByUserId(user.getUserId()));
    }

    /** REST: get one structure */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOne(@PathVariable Long id, Principal principal) {
        User user = getUser(principal);
        return ok(seatFeeService.getStructureById(id, user.getUserId()));
    }

    /** REST: create or update */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> save(
            Principal principal,
            @Valid @RequestBody InstituteSeatFeeStructureRequestDTO dto,
            BindingResult br) {
        if (br.hasErrors()) {
            String msg = br.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(error(msg));
        }
        User user = getUser(principal);
        InstituteSeatFeeStructureResponseDTO saved;
        if (dto.getFeeStructureId() != null) {
            saved = seatFeeService.updateStructure(dto.getFeeStructureId(), user.getUserId(), dto);
        } else {
            saved = seatFeeService.createStructure(user.getUserId(), dto);
        }
        return ok(saved);
    }

    /** REST: delete (soft) */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, Principal principal) {
        User user = getUser(principal);
        seatFeeService.deleteStructure(id, user.getUserId());
        return ok(null);
    }

    /** REST: programmes offered by this institute (for modal dropdown) */
    @GetMapping("/programmes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProgrammes(Principal principal) {
        User user = getUser(principal);
        List<Map<String, Object>> result = getProgrammesForUser(user).stream()
                .map(po -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", po.getProgrammeOfferedId());
                    m.put("name", po.getProgramme().getProgrammeName());
                    m.put("streamId", po.getProgramme().getStream().getStreamId());
                    m.put("streamName", po.getProgramme().getStream().getStreamName());
                    return m;
                })
                .collect(Collectors.toList());
        return ok(result);
    }

    /** REST: all streams */
    @GetMapping("/streams")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStreams() {
        List<Map<String, Object>> result = streamRepository.findAll().stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getStreamId());
                    m.put("name", s.getStreamName());
                    return m;
                })
                .collect(Collectors.toList());
        return ok(result);
    }

    // ---- helpers ----

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Resolves the list of ProgrammeOffered rows for the logged-in institute user.
     * User.orgOwnerId holds the instituteId directly (OrgOwnerType = INSTITUTE).
     */
    private List<ProgrammeOffered> getProgrammesForUser(User user) {
        if (user.getOrgOwnerId() == null) return List.of();
        return programmesOfferedRepository
                .findByInstituteDepartment_Institute_InstituteId(user.getOrgOwnerId());
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        return body;
    }
}
