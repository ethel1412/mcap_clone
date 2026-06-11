package nic.meg.mcap.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import nic.meg.mcap.entities.AffiliationType;
import nic.meg.mcap.repositories.AffiliationTypeRepository;
import nic.meg.mcap.services.AffiliationTypeService;

@Service
public class AffiliationTypeServiceImpl implements AffiliationTypeService {

    private final AffiliationTypeRepository affiliationTypeRepository;

    public AffiliationTypeServiceImpl(AffiliationTypeRepository affiliationTypeRepository) {
        this.affiliationTypeRepository = affiliationTypeRepository;
    }

    @Override
    public List<AffiliationType> getAll() {
        return affiliationTypeRepository.findAll();
    }
}
