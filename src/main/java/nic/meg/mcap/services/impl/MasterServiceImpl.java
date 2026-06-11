package nic.meg.mcap.services.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nic.meg.mcap.entities.Block;
import nic.meg.mcap.entities.District;
import nic.meg.mcap.entities.State;
import nic.meg.mcap.repositories.BlockRepository;
import nic.meg.mcap.repositories.DistrictRepository;
import nic.meg.mcap.repositories.StateRepository;
import nic.meg.mcap.services.MasterService;

@Service
public class MasterServiceImpl implements MasterService {

    @Autowired
    private StateRepository stateRepository;
    
    @Autowired
    private DistrictRepository districtRepository;
    
    @Autowired
    private BlockRepository blocksRepository;

    @Override
    public List<State> getListStates() {
        return stateRepository.findAll();
    }

    @Override
    public List<District> getListOfDistrict(Short stateCode) {
        return districtRepository.findByState_StateCode(stateCode);
    }

    @Override
    public List<Block> getListOfBlocks(Short districtCode) {
        return blocksRepository.findByDistrict_DistrictCode(districtCode);
    }
}