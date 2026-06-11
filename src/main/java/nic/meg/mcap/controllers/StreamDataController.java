package nic.meg.mcap.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import nic.meg.mcap.dto.response.StreamResponseDTO;
import nic.meg.mcap.services.StreamService;

@RestController
@RequestMapping("/stream-data")
@PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','INSTITUTE', 'APPLICANT')")
public class StreamDataController {

    @Autowired
    private StreamService streamService;

    @GetMapping
    public ResponseEntity<List<StreamResponseDTO>> getAllStreams() {
        List<StreamResponseDTO> streams = streamService.getAllStreams();
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StreamResponseDTO> getStreamById(@PathVariable Short id) {
        StreamResponseDTO stream = streamService.getStreamById(id);
        return ResponseEntity.ok(stream);
    }
}
