package nic.meg.mcap.controllers;

import java.util.Collections;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import nic.meg.mcap.dto.response.DistrictDTO;
import nic.meg.mcap.entities.Block;
import nic.meg.mcap.entities.District;
import nic.meg.mcap.entities.State;
import nic.meg.mcap.services.MasterService;

// === ADDED YOUR CUSTOM BEAN NAME BACK IN ===
@Validated
@RestController("Master Data")
@RequestMapping("/master")
public class MasterDataController {

	@Autowired
	private MasterService masterService;

	@Autowired
	private ModelMapper modelMapper;

	@GetMapping("/get-list-states")
	public List<State> getStates() {
		return masterService.getListStates();
	}

	@GetMapping("/get-list-districts/{stateCode}")
	public ResponseEntity<List<DistrictDTO>> getDistricts(
			@PathVariable("stateCode") @Positive(message = "Invalid state code") Short stateCode) {

		if (stateCode == null || stateCode >= 50) {

			return ResponseEntity.badRequest().build();
		}
		List<District> districts = masterService.getListOfDistrict(stateCode);
		List<DistrictDTO> districtDTOs = districts.stream().map(d -> modelMapper.map(d, DistrictDTO.class)).toList();

		if (districts == null || districts.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(districtDTOs);
	}

	@GetMapping("/get-list-blocks/{districtCode}")
	public List<Block> getBlocks(@PathVariable("districtCode") Short districtCode) {
		if (districtCode == null) {
			return Collections.emptyList();
		}
		return masterService.getListOfBlocks(districtCode);
	}
}