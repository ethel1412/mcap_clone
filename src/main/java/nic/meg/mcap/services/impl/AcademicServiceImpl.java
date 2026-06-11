package nic.meg.mcap.services.impl;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nic.meg.mcap.dto.request.AcademicDetailsDTO;
import nic.meg.mcap.dto.request.CuetScoreDTO;
import nic.meg.mcap.dto.request.CuetSubjectScoreDTO;
import nic.meg.mcap.dto.request.GateScoreRequestDTO;
import nic.meg.mcap.dto.request.JeeScoreDTO;
import nic.meg.mcap.dto.request.LatestAcademicRecordRequestDTO;
import nic.meg.mcap.dto.request.NetScoreRequestDTO;
import nic.meg.mcap.dto.request.PastAcademicRecordRequestDTO;
import nic.meg.mcap.dto.request.SubjectMarkDTO;
import nic.meg.mcap.entities.AcademicRecord;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.CuetScore;
import nic.meg.mcap.entities.CuetSubjectScore;
import nic.meg.mcap.entities.GateScore;
import nic.meg.mcap.entities.JeeScore;
import nic.meg.mcap.entities.NetScore;
import nic.meg.mcap.entities.SubjectMark;
import nic.meg.mcap.repositories.AcademicRecordRepository;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.CuetScoreRepository;
import nic.meg.mcap.repositories.GateScoreRepository;
import nic.meg.mcap.repositories.JeeScoreRepository;
import nic.meg.mcap.repositories.NetScoreRepository;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.repositories.SubjectRepository;
import nic.meg.mcap.services.AcademicService;
import nic.meg.mcap.services.QualificationService;

@Service
public class AcademicServiceImpl implements AcademicService {
    private static final Logger logger = LoggerFactory.getLogger(AcademicServiceImpl.class);
    @Autowired
    private ApplicantRepository applicantRepository;
    @Autowired
    private AcademicRecordRepository academicRecordRepository;
    @Autowired
    private JeeScoreRepository jeeScoreRepository;
    @Autowired
    private CuetScoreRepository cuetScoreRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private StreamRepository streamRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private QualificationService qualificationService;
    @Autowired
    private NetScoreRepository netScoreRepository;
    @Autowired
    private GateScoreRepository gateScoreRepository;

    @Override
    @Transactional
    public void saveOrUpdateAcademicDetails(String applicantNo, AcademicDetailsDTO detailsDTO) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantNo));

        applicant.setHasJeeScore(detailsDTO.isProvideJeeScores());
        applicant.setHasCuetScore(detailsDTO.isProvideCuetScores());
        applicant.setHasNetScore(detailsDTO.isProvideNetScores());
        applicant.setHasGateScore(detailsDTO.isProvideGateScores());

        academicRecordRepository.deleteByApplicant(applicant);

        // 1. Process and save the "Latest Qualification" records from its specific DTO
        // list.
        if (detailsDTO.getLatestRecords() != null) {
            for (LatestAcademicRecordRequestDTO recordDTO : detailsDTO.getLatestRecords()) {
                if (recordDTO.getBoardOrUniversity() != null && !recordDTO.getBoardOrUniversity().isBlank()) {
                    saveLatestAcademicRecord(applicant, recordDTO);
                }
            }
        }

        // 2. Process and save the "Past & Additional Qualifications" records from its
        // specific DTO list.
        if (detailsDTO.getPastRecords() != null) {
            for (PastAcademicRecordRequestDTO recordDTO : detailsDTO.getPastRecords()) {
                if (recordDTO.getBoardOrUniversity() != null && !recordDTO.getBoardOrUniversity().isBlank()) {
                    savePastAcademicRecord(applicant, recordDTO);
                }
            }
        }

        // 3. Save Entrance Exam Scores
        saveEntranceExamScores(applicant, detailsDTO);

        applicantRepository.save(applicant);
    }

    private void saveLatestAcademicRecord(Applicant applicant, LatestAcademicRecordRequestDTO recordDTO) {
        AcademicRecord record = new AcademicRecord();
        record.setApplicant(applicant);
        record.setLatestQualification(true);

        mapCommonFields(record, recordDTO.getQualificationLevel(), recordDTO.getBoardOrUniversity(),
                recordDTO.getSchoolOrCollege(), recordDTO.getStreamOrMajor(), recordDTO.getPercentage(),
                recordDTO.getDateOfPassing());

        AcademicRecord savedRecord = academicRecordRepository.save(record);

        if (recordDTO.getSubjectMarks() != null) {
            List<SubjectMark> subjectMarksToSave = new ArrayList<>();
            for (SubjectMarkDTO markDTO : recordDTO.getSubjectMarks()) {
                if (markDTO.getSubjectId() != null && markDTO.getMarksObtained() != null
                        && markDTO.getTotalMarks() != null) {
                    SubjectMark subjectMark = new SubjectMark();
                    subjectMark.setAcademicRecord(savedRecord);
                    subjectRepository.findById(markDTO.getSubjectId()).ifPresent(subjectMark::setSubject);
                    subjectMark.setMarksObtained(markDTO.getMarksObtained());
                    subjectMark.setTotalMarks(markDTO.getTotalMarks());
                    if (markDTO.getTotalMarks() > 0) {
                        double percentage = (markDTO.getMarksObtained() / markDTO.getTotalMarks()) * 100.0;
                        subjectMark.setPercentage(percentage);
                    } else {
                        subjectMark.setPercentage(0.0);
                    }
                    subjectMarksToSave.add(subjectMark);
                }
            }
            if (!subjectMarksToSave.isEmpty()) {
                savedRecord.getSubjectMarks().addAll(subjectMarksToSave);
                academicRecordRepository.save(savedRecord);
            }
        }
    }

    private void savePastAcademicRecord(Applicant applicant, PastAcademicRecordRequestDTO recordDTO) {
        AcademicRecord record = new AcademicRecord();
        record.setApplicant(applicant);
        record.setLatestQualification(false); // Explicitly set as PAST

        mapCommonFields(record, recordDTO.getQualificationLevel(), recordDTO.getBoardOrUniversity(),
                recordDTO.getSchoolOrCollege(), recordDTO.getStreamOrMajor(), recordDTO.getPercentage(),
                recordDTO.getDateOfPassing());

        academicRecordRepository.save(record);
    }

    private void mapCommonFields(AcademicRecord record, String qualLevel, String board, String school, String stream,
                                 Double percentage, String dateOfPassing) {
        record.setQualificationLevel(qualLevel);
        record.setBoardOrUniversity(board);
        record.setSchoolOrCollege(school);
        record.setStreamOrMajor(stream);
        record.setPercentage(percentage);
        try {
            if (dateOfPassing != null && !dateOfPassing.isEmpty()) {
                record.setDateOfPassing(LocalDate.parse(dateOfPassing));
            }
        } catch (DateTimeParseException e) {
            logger.info("Invalid date format for academic record: {}", dateOfPassing);
        }
        streamRepository.findByStreamName(stream).ifPresent(record::setStream);
    }

    private void saveEntranceExamScores(Applicant applicant, AcademicDetailsDTO detailsDTO) {
        if (detailsDTO.isProvideJeeScores()) {
            JeeScore jeeScore = jeeScoreRepository.findByApplicant(applicant).orElse(new JeeScore());
            jeeScore.setApplicant(applicant);
            JeeScoreDTO jeeDTO = detailsDTO.getJeeScore();
            jeeScore.setApplicationNumber(jeeDTO.getApplicationNumber());
            jeeScore.setRollNumber(jeeDTO.getRollNumber());
            jeeScore.setYearOfExam(jeeDTO.getYearOfExam());
            jeeScore.setSessionAppeared(jeeDTO.getSessionAppeared());
            jeeScore.setBestNtaScore(jeeDTO.getBestNtaScore());
            jeeScore.setAllIndiaRank(jeeDTO.getAllIndiaRank());
            jeeScoreRepository.save(jeeScore);
        } else {
            jeeScoreRepository.findByApplicant(applicant).ifPresent(jeeScore -> {
                applicant.setJeeScore(null); // Break the in-memory link
                jeeScoreRepository.delete(jeeScore);
            });
        }

        if (detailsDTO.isProvideCuetScores()) {
            CuetScore cuetScore = cuetScoreRepository.findByApplicant(applicant).orElse(new CuetScore());
            cuetScore.setApplicant(applicant); // Ensure link is set

            // BI-DIRECTIONAL LINK: This is often required for JPA to save correctly
            applicant.setCuetScore(cuetScore);

            CuetScoreDTO cuetDTO = detailsDTO.getCuetScore();
            cuetScore.setApplicationNumber(cuetDTO.getApplicationNumber());
            cuetScore.setYearOfExam(cuetDTO.getYearOfExam());
            cuetScore.setOverallPercentile(cuetDTO.getOverallPercentile());

            // Fix: Handle the collection safely
            cuetScore.getSubjectScores().clear();
            if (cuetDTO.getSubjectScores() != null) {
                for (CuetSubjectScoreDTO subjectDTO : cuetDTO.getSubjectScores()) {
                    if (subjectDTO.getPaperCode() != null && !subjectDTO.getPaperCode().isBlank()) {
                        CuetSubjectScore subjectScore = new CuetSubjectScore();
                        subjectScore.setCuetScore(cuetScore);
                        subjectScore.setPaperCode(subjectDTO.getPaperCode());
                        subjectScore.setSubjectName(subjectDTO.getSubjectName());
                        subjectScore.setScore(subjectDTO.getScore());
                        subjectScore.setPercentile(subjectDTO.getPercentile());
                        cuetScore.getSubjectScores().add(subjectScore);
                    }
                }
            }
            cuetScoreRepository.save(cuetScore);
        } else {
            cuetScoreRepository.findByApplicant(applicant).ifPresent(cuetScore -> {
                applicant.setCuetScore(null);
                cuetScoreRepository.delete(cuetScore);
            });
        }

        if (detailsDTO.isProvideNetScores()) {
            NetScore netScore = netScoreRepository.findByApplicant(applicant).orElse(new NetScore());
            netScore.setApplicant(applicant);
            NetScoreRequestDTO netDTO = detailsDTO.getNetScore();
            netScore.setApplicationNumber(netDTO.getApplicationNumber());
            netScore.setYearOfExam(netDTO.getYearOfExam());
            netScore.setSubject(netDTO.getSubject());
            netScore.setPercentile(netDTO.getPercentile());
            netScoreRepository.save(netScore);
        } else {
            netScoreRepository.findByApplicant(applicant).ifPresent(netScore -> {
                applicant.setNetScore(null); // Break the in-memory link
                netScoreRepository.delete(netScore);
            });
        }

        if (detailsDTO.isProvideGateScores()) {
            GateScore gateScore = gateScoreRepository.findByApplicant(applicant).orElse(new GateScore());
            gateScore.setApplicant(applicant);
            GateScoreRequestDTO gateDTO = detailsDTO.getGateScore();
            gateScore.setRegistrationNumber(gateDTO.getRegistrationNumber());
            gateScore.setYearOfExam(gateDTO.getYearOfExam());
            gateScore.setSubject(gateDTO.getSubject());
            gateScore.setScore(gateDTO.getScore());
            gateScoreRepository.save(gateScore);
        } else {
            gateScoreRepository.findByApplicant(applicant).ifPresent(gateScore -> {
                applicant.setGateScore(null); // Break the in-memory link
                gateScoreRepository.delete(gateScore);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicDetailsDTO getAcademicDetails(String applicantNo) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantNo));

        AcademicDetailsDTO dto = new AcademicDetailsDTO();
        dto.setApplicationId(null); // Will be set by the controller
        dto.setProvideJeeScores(Boolean.TRUE.equals(applicant.getHasJeeScore()));
        dto.setProvideCuetScores(Boolean.TRUE.equals(applicant.getHasCuetScore()));
        dto.setProvideNetScores(Boolean.TRUE.equals(applicant.getHasNetScore()));
        dto.setProvideGateScores(Boolean.TRUE.equals(applicant.getHasGateScore()));

        List<AcademicRecord> records = academicRecordRepository.findByApplicantWithDetails(applicant);

        records.forEach(record -> {
            if (record.isLatestQualification()) {
                dto.getLatestRecords().add(convertEntityToLatestDto(record));
            } else {
                dto.getPastRecords().add(convertEntityToPastDto(record));
            }
        });

        jeeScoreRepository.findByApplicant(applicant).ifPresent(jeeScore -> {
            JeeScoreDTO jeeDTO = new JeeScoreDTO();
            jeeDTO.setApplicationNumber(jeeScore.getApplicationNumber());
            jeeDTO.setRollNumber(jeeScore.getRollNumber());
            jeeDTO.setYearOfExam(jeeScore.getYearOfExam());
            jeeDTO.setSessionAppeared(jeeScore.getSessionAppeared());
            jeeDTO.setBestNtaScore(jeeScore.getBestNtaScore());
            jeeDTO.setAllIndiaRank(jeeScore.getAllIndiaRank());
            dto.setJeeScore(jeeDTO);
        });

// 2. Check for CUET Score
        cuetScoreRepository.findByApplicant(applicant).ifPresent(cuetScore -> {
            CuetScoreDTO cuetDTO = new CuetScoreDTO();
            cuetDTO.setApplicationNumber(cuetScore.getApplicationNumber());
            cuetDTO.setYearOfExam(cuetScore.getYearOfExam());

            if (cuetScore.getSubjectScores() != null) {
                List<CuetSubjectScoreDTO> subjectList = new ArrayList<>();
                for (CuetSubjectScore subject : cuetScore.getSubjectScores()) {
                    CuetSubjectScoreDTO subjectDTO = new CuetSubjectScoreDTO();
                    subjectDTO.setPaperCode(subject.getPaperCode());
                    subjectDTO.setSubjectName(subject.getSubjectName());
                    subjectDTO.setScore(subject.getScore());

                    // --- THE MISSING FIX ---
                    subjectDTO.setPercentile(subject.getPercentile());
                    // -----------------------

                    subjectList.add(subjectDTO);
                }
                cuetDTO.setSubjectScores(subjectList);
            }
            dto.setCuetScore(cuetDTO);
        });

        // 3. Check for NET Score
        netScoreRepository.findByApplicant(applicant).ifPresent(netScore -> {
            NetScoreRequestDTO netDTO = new NetScoreRequestDTO();
            netDTO.setApplicationNumber(netScore.getApplicationNumber());
            netDTO.setYearOfExam(netScore.getYearOfExam());
            netDTO.setSubject(netScore.getSubject());
            netDTO.setPercentile(netScore.getPercentile());
            dto.setNetScore(netDTO);
        });

        // 4. Check for GATE Score
        gateScoreRepository.findByApplicant(applicant).ifPresent(gateScore -> {
            GateScoreRequestDTO gateDTO = new GateScoreRequestDTO();
            gateDTO.setRegistrationNumber(gateScore.getRegistrationNumber());
            gateDTO.setYearOfExam(gateScore.getYearOfExam());
            gateDTO.setSubject(gateScore.getSubject());
            gateDTO.setScore(gateScore.getScore());
            dto.setGateScore(gateDTO);
        });

        return dto;
    }

    private LatestAcademicRecordRequestDTO convertEntityToLatestDto(AcademicRecord entity) {
        LatestAcademicRecordRequestDTO dto = new LatestAcademicRecordRequestDTO();
        dto.setId(entity.getId());
        dto.setQualificationLevel(entity.getQualificationLevel());
        dto.setSchoolOrCollege(entity.getSchoolOrCollege());
        dto.setBoardOrUniversity(entity.getBoardOrUniversity());
        if (entity.getDateOfPassing() != null) {
            dto.setDateOfPassing(entity.getDateOfPassing().toString());
        }
        dto.setStreamOrMajor(entity.getStreamOrMajor());
        dto.setPercentage(entity.getPercentage());
        if (entity.getSubjectMarks() != null) {
            dto.setSubjectMarks(entity.getSubjectMarks().stream().map(mark -> {
                SubjectMarkDTO markDTO = new SubjectMarkDTO();
                if (mark.getSubject() != null) {
                    markDTO.setSubjectId(mark.getSubject().getSubjectId());
                    markDTO.setSubjectName(mark.getSubject().getSubjectName());
                }
                markDTO.setMarksObtained(mark.getMarksObtained());
                markDTO.setTotalMarks(mark.getTotalMarks());
                return markDTO;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    // New converter for Past DTO
    private PastAcademicRecordRequestDTO convertEntityToPastDto(AcademicRecord entity) {
        PastAcademicRecordRequestDTO dto = new PastAcademicRecordRequestDTO();
        dto.setId(entity.getId());
        dto.setQualificationLevel(entity.getQualificationLevel());
        dto.setSchoolOrCollege(entity.getSchoolOrCollege());
        dto.setBoardOrUniversity(entity.getBoardOrUniversity());
        if (entity.getDateOfPassing() != null) {
            dto.setDateOfPassing(entity.getDateOfPassing().toString());
        }
        dto.setStreamOrMajor(entity.getStreamOrMajor());
        dto.setPercentage(entity.getPercentage());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getStudiedSubjectNames(String applicantNo) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Applicant not found"));

        return academicRecordRepository.findByApplicantWithDetails(applicant).stream()
                .filter(AcademicRecord::isLatestQualification).flatMap(record -> record.getSubjectMarks().stream())
                .map(mark -> mark.getSubject().getSubjectName()).collect(Collectors.toList());
    }
}