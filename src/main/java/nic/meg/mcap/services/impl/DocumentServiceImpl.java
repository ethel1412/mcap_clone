package nic.meg.mcap.services.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import nic.meg.mcap.entities.AcademicRecord;
import nic.meg.mcap.utils.FileUploadValidator;
import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Application;
import nic.meg.mcap.entities.Document;
import nic.meg.mcap.enums.ProgrammeLevel;
import nic.meg.mcap.repositories.AcademicRecordRepository;
import nic.meg.mcap.repositories.ApplicantRepository;
import nic.meg.mcap.repositories.ApplicationRepository;
import nic.meg.mcap.repositories.DocumentRepository;
import nic.meg.mcap.services.DocumentService;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private ApplicantRepository applicantRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AcademicRecordRepository academicRecordRepository;

    @Override
    @Transactional
    public Document saveDocument(MultipartFile file, String applicantNo, String documentType) throws IOException {
        // Three-layer validation: size → extension → magic bytes
        // Fortify: Often Misused: File Upload (ApplicantDataController.java:221)
        FileUploadValidator.validate(file);

        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantNo));

        Document doc = documentRepository.findByApplicantAndDocumentType(applicant, documentType)
                .orElse(new Document());

        doc.setApplicant(applicant);
        doc.setDocumentType(documentType);
        String originalFilename = file.getOriginalFilename();
        // Use cleanPath to prevent path traversal; store only the filename, never a path
        doc.setFileName(originalFilename != null ? StringUtils.cleanPath(originalFilename) : "unknown_file");
        // Derive content type from the validated magic bytes rather than trusting the
        // browser-supplied Content-Type header
        doc.setFileType(deriveContentType(file.getOriginalFilename()));
        doc.setFileContent(file.getBytes());

        return documentRepository.save(doc);
    }

    /**
     * Derives a safe MIME type from the (already validated) file extension.
     * We deliberately do NOT use {@code file.getContentType()} here because
     * that value comes from the HTTP request and can be spoofed by an attacker.
     */
    private String deriveContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getRequiredDocumentTypes(String applicantNo, Long applicationId) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found for number: " + applicantNo));

        Application currentApplication = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));
        if (!currentApplication.getApplicant().getApplicantNo().equals(applicantNo)) {
            throw new SecurityException("Unauthorized access to application");
        }

        ProgrammeLevel applicationLevel = currentApplication.getAdmissionWindow().getProgrammeLevel();
        Map<String, String> requiredDocTypes = new LinkedHashMap<>();

        // 1. Universal documents
        requiredDocTypes.put("Photo", "Applicant Photo");
        requiredDocTypes.put("Signature", "Applicant Signature");
        requiredDocTypes.put("ClassX_Marksheet", "Class X Marksheet");

        // 2. Academic documents based on qualifications
        List<AcademicRecord> academicRecords = academicRecordRepository.findByApplicantWithDetails(applicant);
        boolean classXIIMarksheetAdded = false;
        for (AcademicRecord record : academicRecords) {
            String qualLevel = record.getQualificationLevel().toLowerCase();
            if (qualLevel.contains("class xii") && !classXIIMarksheetAdded) {
                requiredDocTypes.put("ClassXII_Marksheet", "Class XII Marksheet");
                classXIIMarksheetAdded = true;
            } else if (qualLevel.contains("diploma")) {
                if (qualLevel.contains("pg") || qualLevel.contains("post graduate")) {
                    requiredDocTypes.put("PG_Diploma_Certificate", "Post Graduate Diploma Certificate/Marksheet");
                } else {
                    requiredDocTypes.put("Diploma_Certificate", "Diploma Certificate/Marksheet");
                }
            } else if (qualLevel.contains("bachelor") || qualLevel.contains("undergraduate")) {
                requiredDocTypes.put("UG_Marksheet", "Undergraduate Marksheet/Degree");
            } else if (qualLevel.contains("master") || qualLevel.contains("pg")) {
                requiredDocTypes.put("PG_Marksheet", "Postgraduate Marksheet/Degree");
            } else if (qualLevel.contains("other")) {
                requiredDocTypes.put("Other_Certificate", "Other Qualification Certificate");
            }
        }
        if (applicationLevel == ProgrammeLevel.PG && !requiredDocTypes.containsKey("UG_Marksheet")) {
            requiredDocTypes.put("UG_Marksheet", "Undergraduate Marksheet/Degree");
        }

        // 3. Conditional documents based on applicant's personal details
        if (applicant.getCommunityCategory() != null
                && "ST".equalsIgnoreCase(applicant.getCommunityCategory().getCategoryCode().trim())) {
            requiredDocTypes.put("ST_Certificate", "ST Certificate");
        }
        if (Boolean.TRUE.equals(applicant.getHasDomicileCertificate())) {
            requiredDocTypes.put("Domicile_Certificate", "Residential Proof Certificate");
        }
        if (Boolean.TRUE.equals(applicant.getIsDifferentlyAbled())) {
            requiredDocTypes.put("Differently_Abled_Certificate", "Differently Abled Certificate");
        }
        if (Boolean.TRUE.equals(applicant.getHasNccCertificate())) {
            requiredDocTypes.put("NCC_Certificate", "NCC Certificate");
        }
        if (Boolean.TRUE.equals(applicant.getHasNssCertificate())) {
            requiredDocTypes.put("NSS_Certificate", "NSS Certificate");
        }
        if (Boolean.TRUE.equals(applicant.getHasBackwardAreaCertificate())) {
            requiredDocTypes.put("Backward_Area_Certificate", "Backward Area Certificate");
        }
        if (Boolean.TRUE.equals(applicant.getHasAnyOtherRelevantCertificate())) {
            requiredDocTypes.put("Other_Relevant_Certificate", "Any Other Relevant Certificate");
        }

        // 4. Entrance exam documents (using the boolean flags we fixed earlier)
        if (Boolean.TRUE.equals(applicant.getHasJeeScore())) {
            requiredDocTypes.put("JEE_Scorecard", "JEE (Main) Scorecard");
        }
        if (Boolean.TRUE.equals(applicant.getHasCuetScore())) {
            requiredDocTypes.put("CUET_Scorecard", "CUET Scorecard");
        }
        if (Boolean.TRUE.equals(applicant.getHasNetScore())) {
            requiredDocTypes.put("NET_Scorecard", "NET Scorecard");
        }
        if (Boolean.TRUE.equals(applicant.getHasGateScore())) {
            requiredDocTypes.put("GATE_Scorecard", "GATE Scorecard");
        }

        return requiredDocTypes;
    }

    @Override
    @Transactional(readOnly = true)
    public void validateAllRequiredDocumentsUploaded(String applicantNo, Long applicationId) {
        Map<String, String> required = getRequiredDocumentTypes(applicantNo, applicationId);
        List<Document> uploaded = getUploadedDocuments(applicantNo);

        Set<String> uploadedTypes = uploaded.stream().map(Document::getDocumentType).collect(Collectors.toSet());

        List<String> missingDocs = required.keySet().stream()
                .filter(requiredType -> !uploadedTypes.contains(requiredType)).map(required::get)
                .collect(Collectors.toList());

        if (!missingDocs.isEmpty()) {
            throw new IllegalStateException(
                    "Please upload all required documents. Missing: " + String.join(", ", missingDocs));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Long documentId) {
        return documentRepository.findById(documentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getUploadedDocuments(String applicantNo) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantNo));
        return documentRepository.findByApplicant(applicant);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentByApplicantAndType(String applicantNo, String documentType) {
        Applicant applicant = applicantRepository.findByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException("Applicant not found: " + applicantNo));
        return documentRepository.findByApplicantAndDocumentType(applicant, documentType);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, String applicantNo) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found."));

        if (!document.getApplicant().getApplicantNo().equals(applicantNo)) {
            throw new SecurityException("You do not have permission to delete this document.");
        }

        documentRepository.delete(document);
    }
}