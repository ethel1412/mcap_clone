package nic.meg.mcap.services;

import java.util.List;

import nic.meg.mcap.entities.Block;
import nic.meg.mcap.entities.District;
import nic.meg.mcap.entities.State;

public interface MasterService {

	public List<State> getListStates();

	public List<District> getListOfDistrict(Short stateCode);

	public List<Block> getListOfBlocks(Short districtCode);

}