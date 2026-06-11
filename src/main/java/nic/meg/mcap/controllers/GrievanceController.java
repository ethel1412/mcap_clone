package nic.meg.mcap.controllers;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.entities.Grievance;
import nic.meg.mcap.entities.GrievanceCategory;
import nic.meg.mcap.entities.Institute;
import nic.meg.mcap.entities.Role;
import nic.meg.mcap.repositories.GrievanceCategoryRepository;
import nic.meg.mcap.repositories.GrievanceRepository;
import nic.meg.mcap.repositories.RoleRepository;
import nic.meg.mcap.services.InstituteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class GrievanceController {

    private static final Logger logger = LoggerFactory.getLogger(GrievanceController.class);

    @Autowired private GrievanceRepository grievanceRepository;
    @Autowired private GrievanceCategoryRepository categoryRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private InstituteService instituteService;

    // ─────────────────────────────────────────────────────────────────
    //  Expose Categories securely to frontend
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/applicants/grievances/categories")
    @ResponseBody
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<?> getCategories() {
        try {
            List<Map<String, Object>> list = categoryRepository.findAll().stream()
                    .map(c -> Map.<String, Object>of(
                            "code", c.getCode(),
                            "name", c.getName(),
                            "requiresInstitute", c.isRequiresInstitute()
                    )).toList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Applicant submits a grievance (POST, JSON)
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/applicants/grievances/submit")
    @ResponseBody
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<?> submit(@RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            String categoryCode = (String) payload.get("categoryCode");
            String message = (String) payload.get("message");
            Object instituteRaw = payload.get("instituteId");

            if (categoryCode == null || categoryCode.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Category is required."));
            if (message == null || message.isBlank() || message.trim().split("\\s+").length > 120)
                return ResponseEntity.badRequest().body(Map.of("error", "Message must be between 1 and ~100 words."));

            // 1. Dynamic DB Fetch for Category
            GrievanceCategory category = categoryRepository.findByCode(categoryCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category code."));

            Grievance g = new Grievance();
            g.setCategory(category);
            g.setMessage(message.trim());
            g.setSubmittedBy(auth.getName());
            g.setSubmittedAt(LocalDateTime.now());
            g.setStatus("OPEN");

            // 2. Assign Role based on DB Category config
            Role role = roleRepository.findById(category.getConcernedRoleId())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + category.getConcernedRoleId()));
            g.setConcernedRole(role);

            // 3. Attach Institute if required
            if (category.isRequiresInstitute()) {
                if (instituteRaw == null || String.valueOf(instituteRaw).isBlank())
                    return ResponseEntity.badRequest().body(Map.of("error", "Please select the concerned institute."));
                Short instituteId = Short.parseShort(String.valueOf(instituteRaw));
                Institute institute = instituteService.findById(instituteId);
                g.setConcernedInstitute(institute);
            }

            // 4. Use PostgreSQL Sequence to guarantee a unique, race-condition-free Ticket Code
            int year = LocalDateTime.now().getYear();
            long safeSequenceNumber = grievanceRepository.getNextTicketSequence();
            g.setTicketCode("GRV-" + year + "-" + safeSequenceNumber);

            grievanceRepository.save(g);
            return ResponseEntity.ok(Map.of("message", "Grievance submitted successfully.", "ticketCode", g.getTicketCode()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to submit grievance. Please try again."));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Applicant: View My Grievances (Fragment)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/applicants/fragments/my-grievances")
    @PreAuthorize("hasRole('APPLICANT')")
    public String myGrievances(Authentication auth, Model model) {
        List<Grievance> grievances = grievanceRepository.findBySubmittedByOrderBySubmittedAtDesc(auth.getName());
        model.addAttribute("grievances", grievances);
        return "grievances/my-grievances";
    }

    // ─────────────────────────────────────────────────────────────────
    //  Grievance list page — shown to Admins, Controllers, etc.
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/grievances/view")
    @PreAuthorize("hasAnyRole('CONTROLLER','INSTITUTE','ADMIN', 'DEPT ADMIN')")
    public String viewGrievances(Authentication auth, Model model) {
        try {
            List<Grievance> grievances;

            // Check roles dynamically
            boolean isInstitute = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTE"));
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Checking for both underscore and space versions depending on how Spring Security mapped it
            boolean isDeptAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_DEPT ADMIN") || a.getAuthority().equals("ROLE_DEPT_ADMIN"));

            if (isInstitute) {
                Short instituteId = getInstituteIdForUser(auth.getName());
                grievances = grievanceRepository.findByRoleAndInstitute("3", instituteId); // Spaceless '3'
            } else if (isAdmin) {
                grievances = grievanceRepository.findByConcernedRoleRoleIdOrderBySubmittedAtDesc("4"); // Spaceless '4'
            } else if (isDeptAdmin) {
                grievances = grievanceRepository.findByConcernedRoleRoleIdOrderBySubmittedAtDesc("6"); // Handling Role '6'
            } else {
                // CONTROLLER fallback
                grievances = grievanceRepository.findByConcernedRoleRoleIdOrderBySubmittedAtDesc("1"); // Spaceless '1'
            }

            model.addAttribute("grievances", grievances);
            return "grievances/grievance-list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Could not load grievances.");
            return "grievances/grievance-list";
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Detail view for one grievance (AJAX)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/grievances/{id}/detail")
    @PreAuthorize("hasAnyRole('CONTROLLER','INSTITUTE','ADMIN', 'DEPT ADMIN')")
    @ResponseBody
    public ResponseEntity<?> detail(@PathVariable Long id, Authentication auth) {
        try {
            Grievance g = grievanceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Grievance not found"));

            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTE"))) {
                Short myInstituteId = getInstituteIdForUser(auth.getName());
                if (g.getConcernedInstitute() == null || !g.getConcernedInstitute().getInstituteId().equals(myInstituteId)) {
                    return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
                }
            }

            Map<String, Object> dto = new java.util.LinkedHashMap<>();
            dto.put("id", g.getId());
            dto.put("ticketCode", g.getTicketCode());
            dto.put("submittedBy", g.getSubmittedBy());
            dto.put("category", g.getCategory().getName());
            dto.put("message", g.getMessage());
            dto.put("submittedAt", g.getSubmittedAt().toString());
            dto.put("status", g.getStatus());
            dto.put("institute", g.getConcernedInstitute() != null ? g.getConcernedInstitute().getInstituteName() : null);
            return ResponseEntity.ok(dto);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Staff: Change Grievance Status
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/grievances/{id}/status")
    @PreAuthorize("hasAnyRole('CONTROLLER','INSTITUTE','ADMIN', 'DEPT ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload, Authentication auth) {
        try {
            Grievance g = grievanceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Grievance not found"));

            // Re-verify institute permission to prevent URL tampering
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUTE"))) {
                Short myInstituteId = getInstituteIdForUser(auth.getName());
                if (g.getConcernedInstitute() == null || !g.getConcernedInstitute().getInstituteId().equals(myInstituteId)) {
                    return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
                }
            }

            String newStatus = payload.get("status");
            if (newStatus != null && (newStatus.equals("OPEN") || newStatus.equals("RESOLVED"))) {
                g.setStatus(newStatus);
                grievanceRepository.save(g);
                return ResponseEntity.ok(Map.of("message", "Status updated to " + newStatus));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Update failed"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Helper: fetch institutes
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/applicants/grievances/institutes")
    @ResponseBody
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<?> getInstitutes() {
        try {
            List<Map<String, Object>> list = instituteService.getAllInstitutes().stream()
                    .map(i -> Map.<String, Object>of("id", i.getInstituteId(), "name", i.getInstituteName())).toList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Short getInstituteIdForUser(String username) {
        return instituteService.getInstituteIdByUsername(username);
    }
}