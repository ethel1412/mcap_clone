package nic.meg.mcap.services.impl;

import nic.meg.mcap.entities.Qualification;
import nic.meg.mcap.repositories.QualificationRepository;
import nic.meg.mcap.services.QualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QualificationServiceImpl implements QualificationService {

    private final QualificationRepository qualificationRepository;
    
    public QualificationServiceImpl(QualificationRepository qualificationRepository) {
        this.qualificationRepository = qualificationRepository;
    }
    @Override
    public List<Qualification> getAllActiveQualifications() {
        return qualificationRepository.findByIsActiveTrueOrderByNameAsc();
    }
}