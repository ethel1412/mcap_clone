package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.ScheduleStepTemplateRequestDTO;
import nic.meg.mcap.dto.response.ScheduleStepTemplateDTO;

import java.util.List;

public interface ScheduleStepTemplateService {

    // Get all active templates
    List<ScheduleStepTemplateDTO> getAllActiveTemplates();

    // Filter by Category
    List<ScheduleStepTemplateDTO> getTemplatesByCategory(String category);

    // Get template by ID
    ScheduleStepTemplateDTO getTemplateById(Long templateId);

    // Get template by step order
    ScheduleStepTemplateDTO getTemplateByOrder(Integer stepOrder);

    // Create a new template manually
    ScheduleStepTemplateDTO createTemplate(ScheduleStepTemplateRequestDTO dto);

    // Update an existing template manually
    ScheduleStepTemplateDTO updateTemplate(Long templateId, ScheduleStepTemplateRequestDTO dto);

    // Delete a template (hard delete & resequence)
    void deleteTemplate(Long templateId);

    // --- SMART GENERATORS ---

    // Auto-generate the standard Pre-Admission steps
    void autoGeneratePreAdmissionSteps();

    // Auto-generate a Counselling Phase cluster (Merit -> Allotment -> Acceptance -> Payment)
    void addCounsellingPhaseCluster(String admissionRoute);
}