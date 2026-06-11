package nic.meg.mcap.services;

import nic.meg.mcap.entities.Document;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map; // Import Map
import java.util.Optional;

public interface DocumentService {
    Document saveDocument(MultipartFile file, String applicantNo, String documentType) throws IOException;

    Optional<Document> getDocumentById(Long documentId);

    List<Document> getUploadedDocuments(String applicantNo);

    Optional<Document> getDocumentByApplicantAndType(String applicantNo, String documentType);

    void deleteDocument(Long documentId, String applicantNo);

    Map<String, String> getRequiredDocumentTypes(String applicantNo, Long applicationId);

    void validateAllRequiredDocumentsUploaded(String applicantNo, Long applicationId);

}