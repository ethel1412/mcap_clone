package nic.meg.mcap.controllers;

import java.util.List;
import java.util.stream.Collectors;

import nic.meg.mcap.entities.Programme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import nic.meg.mcap.dto.request.ProgrammeRequestDTO;
import nic.meg.mcap.dto.response.ProgrammeResponseDTO;
import nic.meg.mcap.services.ProgrammeService;

@RestController
@RequestMapping("/programme-data")
//@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
public class ProgrammeDataController {

    @Autowired
    private ProgrammeService programmeService;

    private ProgrammeResponseDTO convertToDTO(Programme programme) {
        ProgrammeResponseDTO dto = new ProgrammeResponseDTO();
        dto.setProgrammeId(programme.getProgrammeId());
        dto.setProgrammeName(programme.getProgrammeName());
        dto.setProgrammeLevel(programme.getProgrammeLevel());
        dto.setStreamId(programme.getStream().getStreamId());
        dto.setStreamName(programme.getStream().getStreamName());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<ProgrammeResponseDTO>> getAllProgrammes(
            @RequestParam(required = false) String departmentId) {

        Integer deptId = null;

        // 🔍 Validate manually
        if (departmentId != null && !departmentId.isBlank()) {
            try {
                deptId = Integer.parseInt(departmentId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("departmentId must be a valid integer");
            }
        }

        List<Programme> programmes;

        if (deptId != null) {
            programmes = programmeService.getProgrammesByDepartmentId(deptId);
        } else {
            programmes = programmeService.getAllProgrammes();
        }

        List<ProgrammeResponseDTO> dtos = programmes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProgrammeResponseDTO> getProgrammeById(@PathVariable Short id) {
        Programme programme = programmeService.getProgrammeById(id);
        return ResponseEntity.ok(convertToDTO(programme));
    }

    @PostMapping
    public ResponseEntity<ProgrammeResponseDTO> createProgramme(@Valid @RequestBody ProgrammeRequestDTO requestDTO) {
        Programme created = programmeService.saveProgramme(requestDTO);
        return new ResponseEntity<>(convertToDTO(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgrammeResponseDTO> updateProgramme(@PathVariable Short id, @Valid @RequestBody ProgrammeRequestDTO requestDTO) {
        Programme updated = programmeService.updateProgramme(id, requestDTO);
        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgramme(@PathVariable Short id) {
        programmeService.deleteProgramme(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-name")
    public ResponseEntity<List<Programme>> getProgrammesByName(@RequestParam String name) {
        List<Programme> programmes = programmeService.getProgrammesByName(name);
        return ResponseEntity.ok(programmes);
    }

    @GetMapping("/by-stream/{streamId}")
    public ResponseEntity<List<ProgrammeResponseDTO>> getProgrammesByStream(@PathVariable Short streamId) {
        List<Programme> programmes = programmeService.getProgrammesByStreamId(streamId);
        List<ProgrammeResponseDTO> response = programmes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-level/{level}")
    public ResponseEntity<List<ProgrammeResponseDTO>> getProgrammesByLevel(@PathVariable String level) {
        List<Programme> programmes = programmeService.getProgrammesByLevel(level);
        List<ProgrammeResponseDTO> response = programmes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/search")
    public ResponseEntity<List<ProgrammeResponseDTO>> searchProgrammes(@RequestParam String query) {
        List<Programme> programmes = programmeService.searchProgrammes(query);
        List<ProgrammeResponseDTO> response = programmes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }


}
