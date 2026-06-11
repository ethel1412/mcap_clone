package nic.meg.mcap.services.impl;

import nic.meg.mcap.dto.request.ScheduleStepTemplateRequestDTO;
import nic.meg.mcap.dto.response.ScheduleStepTemplateDTO;
import nic.meg.mcap.entities.ScheduleStepTemplate;
import nic.meg.mcap.enums.ScheduleActorRole;
import nic.meg.mcap.repositories.ScheduleStepTemplateRepository;
import nic.meg.mcap.services.ScheduleStepTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleStepTemplateServiceImpl implements ScheduleStepTemplateService {

    @Autowired
    private ScheduleStepTemplateRepository templateRepository;

    @Override
    public List<ScheduleStepTemplateDTO> getAllActiveTemplates() {
        return templateRepository.findByIsActiveTrueOrderByStepOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleStepTemplateDTO> getTemplatesByCategory(String category) {
        return templateRepository.findByCategoryAndIsActiveTrueOrderByStepOrderAsc(category)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleStepTemplateDTO getTemplateById(Long templateId) {
        ScheduleStepTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));
        return convertToDTO(template);
    }

    @Override
    public ScheduleStepTemplateDTO getTemplateByOrder(Integer stepOrder) {
        ScheduleStepTemplate template = templateRepository.findByStepOrder(stepOrder)
                .orElseThrow(() -> new RuntimeException("Template not found for step order: " + stepOrder));
        return convertToDTO(template);
    }

    @Override
    public ScheduleStepTemplateDTO createTemplate(ScheduleStepTemplateRequestDTO dto) {
        if (templateRepository.existsByStepOrder(dto.getStepOrder())) {
            throw new RuntimeException("A template with step order " + dto.getStepOrder() + " already exists");
        }

        ScheduleStepTemplate template = new ScheduleStepTemplate();
        template.setStepOrder(dto.getStepOrder());
        template.setStepName(dto.getStepName());
        template.setCategory(dto.getCategory());
        template.setAdmissionRoute(dto.getAdmissionRoute());
        template.setDescription(dto.getDescription());
        template.setPhaseNumber(dto.getPhaseNumber());
        template.setIsActive(true);

        template.setDefaultActorRole(
                ScheduleActorRole.valueOf(dto.getDefaultActorRole())
        );

        ScheduleStepTemplate saved = templateRepository.save(template);
        return convertToDTO(saved);
    }

    @Override
    public ScheduleStepTemplateDTO updateTemplate(Long templateId, ScheduleStepTemplateRequestDTO dto) {
        ScheduleStepTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));

        if (!template.getStepOrder().equals(dto.getStepOrder())
                && templateRepository.existsByStepOrder(dto.getStepOrder())) {
            throw new RuntimeException("Another template with step order " + dto.getStepOrder() + " already exists");
        }

        template.setStepOrder(dto.getStepOrder());
        template.setStepName(dto.getStepName());
        template.setCategory(dto.getCategory());
        template.setAdmissionRoute(dto.getAdmissionRoute());
        template.setDescription(dto.getDescription());
        template.setPhaseNumber(dto.getPhaseNumber());

        template.setDefaultActorRole(
                ScheduleActorRole.valueOf(dto.getDefaultActorRole())
        );

        ScheduleStepTemplate updated = templateRepository.save(template);
        return convertToDTO(updated);
    }

    @Override
    public void deleteTemplate(Long templateId) {
        templateRepository.deleteById(templateId);
        resequenceStepOrders();
    }

    private void resequenceStepOrders() {
        List<ScheduleStepTemplate> templates = templateRepository.findAllByOrderByStepOrderAsc();

        int order = 1;
        for (ScheduleStepTemplate t : templates) {
            t.setStepOrder(order++);
        }
        templateRepository.saveAll(templates);
    }
    // =========================================================================
    // SMART GENERATORS
    // =========================================================================

    @Override
    public void autoGeneratePreAdmissionSteps() {
        if (!templateRepository.findByCategoryAndIsActiveTrueOrderByStepOrderAsc("PRE_ADMISSION").isEmpty()) {
            throw new RuntimeException("Pre-Admission steps have already been initialized.");
        }

        int nextOrder = templateRepository.findAbsoluteMaxStepOrder().orElse(0) + 1;

        createAndSaveStep(nextOrder++, "Institutes Lock Seats & Programmes", "PRE_ADMISSION", "GENERAL", null, "Institutes finalize and lock their seat matrices.", ScheduleActorRole.INSTITUTE);
        createAndSaveStep(nextOrder++, "Set Eligibility Rules", "PRE_ADMISSION", "GENERAL", null, "Controller defines eligibility criteria for programmes.", ScheduleActorRole.CONTROLLER);
        createAndSaveStep(nextOrder++, "Controller Final Approval", "PRE_ADMISSION", "GENERAL", null, "Controller approves all institute configurations before the window opens.", ScheduleActorRole.CONTROLLER);

        //  Dedicated CORRECTION category so it renders below the Application Window!
        createAndSaveStep(nextOrder, "Application Correction Window", "CORRECTION", "GENERAL", null, "Applicants can edit and correct their submitted applications.", ScheduleActorRole.APPLICANT);
    }

    @Override
    public void addCounsellingPhaseCluster(String admissionRoute) {
        int nextOrder = templateRepository.findAbsoluteMaxStepOrder().orElse(0) + 1;
        int nextPhase;

        if (admissionRoute.equals("COMBINED")) {
            // A new COMBINED phase looks at the global highest number so it always moves forward
            nextPhase = templateRepository.findGlobalMaxPhaseNumber().orElse(0) + 1;
        } else {
            // For CUET or NON_CUET, we count exactly how many rounds they have actually participated in!
            Long routeCount = templateRepository.countDistinctPhasesByRoute(admissionRoute);
            Long combinedCount = templateRepository.countDistinctCombinedPhases();

            // Total historical rounds + 1
            nextPhase = (int) (routeCount + combinedCount) + 1;
        }

        // Clean formatting for the step names
        String prefix = admissionRoute.equals("COMBINED") ? "Combined Phase " + nextPhase : admissionRoute + " Phase " + nextPhase;

        // 1. Merit List & Allotment
        createAndSaveStep(nextOrder++, prefix + ": Generation of Merit List and Seat Allotment", "COUNSELLING", admissionRoute, nextPhase, "Generate merit list and allocate seats based on reservations for the active route.", ScheduleActorRole.CONTROLLER);

        // 2. Institute Verification
        createAndSaveStep(nextOrder++, prefix + ": Institute Verification of Applicants", "COUNSELLING", admissionRoute, nextPhase, "Institutes review and verify the documents of allotted candidates.", ScheduleActorRole.INSTITUTE);

        // 3. Acceptance & Payment (Combined)
        createAndSaveStep(nextOrder, prefix + ": Seat Acceptance and Admission Fee Payment", "COUNSELLING", admissionRoute, nextPhase, "Applicants must accept their allocated seats and pay the admission fee.", ScheduleActorRole.APPLICANT);
    }

    // Helper method to rapidly build and save Steps
    private void createAndSaveStep(Integer order, String name, String category, String route, Integer phase, String desc, ScheduleActorRole role) {
        ScheduleStepTemplate step = new ScheduleStepTemplate();
        step.setStepOrder(order);
        step.setStepName(name);
        step.setCategory(category);
        step.setAdmissionRoute(route);
        step.setPhaseNumber(phase);
        step.setDescription(desc);
        step.setDefaultActorRole(role);
        step.setIsActive(true);
        templateRepository.save(step);
    }

    private ScheduleStepTemplateDTO convertToDTO(ScheduleStepTemplate template) {
        return new ScheduleStepTemplateDTO(
                template.getTemplateId(),
                template.getStepOrder(),
                template.getStepName(),
                template.getCategory(),
                template.getAdmissionRoute(),
                template.getDescription(),
                template.getPhaseNumber(),
                template.getIsActive(),
                template.getDefaultActorRole().name()
        );
    }
}