package nic.meg.mcap.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import nic.meg.mcap.entities.ManagementType;
import nic.meg.mcap.repositories.ManagementTypeRepository;
import nic.meg.mcap.services.ManagementTypeService;

@Service
public class ManagementTypeServiceImpl implements ManagementTypeService {

    private final ManagementTypeRepository managementTypeRepository;

    public ManagementTypeServiceImpl(ManagementTypeRepository managementTypeRepository) {
        this.managementTypeRepository = managementTypeRepository;
    }

    @Override
    public List<ManagementType> getAll() {
        return managementTypeRepository.findAll();
    }
}
