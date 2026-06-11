package nic.meg.mcap.services;

import java.util.List;
import java.time.LocalDateTime;
import nic.meg.mcap.dto.request.AdmissionWindowRequestDTO;
import nic.meg.mcap.dto.response.ActiveAdmissionWindowResponseDTO;
import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.dto.response.AdmissionWindowProgrammeResponseDTO;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.enums.ProgrammeLevel;

public interface AdmissionWindowService {
    List<Stream> getAllStreams();
    List<AdmissionWindow> getAllAdmissionWindowsWithProgrammes();
    List<AdmissionWindow> getWindowsByStatus(String status);

    // CHANGED: Short id -> String admissionCode
    AdmissionWindowRequestDTO getAdmissionWindowForEdit(String admissionCode);

    void saveAdmissionWindow(AdmissionWindowRequestDTO dto);

    // CHANGED: findById -> findByCode, Short id -> String admissionCode
    AdmissionWindow findByCode(String admissionCode);

    // CHANGED: Short id -> String admissionCode
    void updateAdmissionWindow(String admissionCode, AdmissionWindowRequestDTO dto);

    // CHANGED: Short id -> String admissionCode
    void deleteAdmissionWindow(String admissionCode);

    // CHANGED: Short id -> String admissionCode
    AdmissionWindow toggleIsActive(String admissionCode);

    List<AdmissionWindow> getLatestAdmissionWindows();

    // CHANGED: Short admissionId -> String admissionCode
    List<AdmissionWindowProgrammeResponseDTO> getProgrammesForWindow(String admissionCode);

    boolean removeProgrammeFromWindow(Short admissionWindowProgrammeId);
    void toggleProgrammeStatusInWindow(Short admissionWindowProgrammeId);

    // CHANGED: Short excludeWindowId -> String excludeWindowCode
    String getExistingWindowSession(Short streamId, ProgrammeLevel programmeLevel, String excludeWindowCode);

    List<ActiveAdmissionWindowResponseDTO> findActiveAdmissionWindows();

    void extendWindow(String admissionCode, LocalDateTime newEndDate);
    
	boolean isDuplicateWindow(Short streamId, ProgrammeLevel level, String session, String excludeWindowCode);
    
}