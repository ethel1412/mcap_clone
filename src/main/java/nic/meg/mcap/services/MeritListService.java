package nic.meg.mcap.services;

import nic.meg.mcap.dto.response.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MeritListService {

    // -------------------------
    // NEW (Round + Phase aware)
    // -------------------------

    // CHANGED: Short admissionWindowId to String admissionWindowCode
    @Transactional(readOnly = true)
    MeritListResponseDTO getLatestMeritListByProgramme(
            String admissionWindowCode,
            Short programmeId,
            String roundType,
            Integer phaseNo);

    MeritListMetadataDTO generateUGMeritList(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo);

    MeritListMetadataDTO generatePGMeritList(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo);

    boolean hasMeritListForUG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo);

    boolean hasMeritListForPG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo);

    ApplicantCountDTO countApplicantsForUG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo);

    ApplicantCountDTO countApplicantsForPG(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo);

    // CHANGED: Added generic type and updated to String admissionWindowCode
    @Transactional(readOnly = true)
    List<MeritListResponseDTO> getAllMeritListsForUGStream(String admissionWindowCode, Short streamId, String roundType, Integer phaseNo);

    // CHANGED: Added generic type and updated to String admissionWindowCode
    @Transactional(readOnly = true)
    List<MeritListResponseDTO> getAllMeritListsForPGProgramme(String admissionWindowCode, Short programmeId, String roundType, Integer phaseNo);

    // -------------------------
    // LEGACY (defaults)
    // -------------------------

    MeritListMetadataDTO generateUGMeritList(String admissionWindowCode, Short programmeId);

    MeritListMetadataDTO generatePGMeritList(String admissionWindowCode, Short programmeId);

    boolean hasMeritListForUG(String admissionWindowCode, Short programmeId);

    boolean hasMeritListForPG(String admissionWindowCode, Short programmeId);

    ApplicantCountDTO countApplicantsForUG(String admissionWindowCode, Short programmeId);

    ApplicantCountDTO countApplicantsForPG(String admissionWindowCode, Short programmeId);

    // CHANGED: Added generic type and updated to String admissionWindowCode
    @Transactional(readOnly = true)
    List<MeritListResponseDTO> getAllMeritListsForUGStream(String admissionWindowCode, Short streamId);

    // CHANGED: Added generic type and updated to String admissionWindowCode
    @Transactional(readOnly = true)
    List<MeritListResponseDTO> getAllMeritListsForPGProgramme(String admissionWindowCode, Short programmeId);

    // -------------------------
    // Existing public APIs
    // -------------------------

    MeritListResponseDTO getMeritListById(Long meritListId);

    void publishMeritList(Long meritListId);

    PagedResponse<MeritListRowDTO> getPagedMeritListById(Long meritListId, int page, int size);
}