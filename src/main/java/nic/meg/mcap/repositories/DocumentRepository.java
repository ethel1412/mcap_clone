package nic.meg.mcap.repositories;

import nic.meg.mcap.entities.Applicant;
import nic.meg.mcap.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByApplicant(Applicant applicant);
    Optional<Document> findByApplicantAndDocumentType(Applicant applicant, String documentType);

}