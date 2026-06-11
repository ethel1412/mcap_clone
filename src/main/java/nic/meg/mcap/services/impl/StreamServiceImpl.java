package nic.meg.mcap.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

import nic.meg.mcap.dto.response.StreamResponseDTO;
import nic.meg.mcap.entities.Stream;
import nic.meg.mcap.repositories.StreamRepository;
import nic.meg.mcap.services.StreamService;

@Service
public class StreamServiceImpl implements StreamService {

    @Autowired
    private StreamRepository streamRepository;

    @Override
    public List<StreamResponseDTO> getAllStreams() {
        return streamRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StreamResponseDTO getStreamById(Short id) {
        Stream stream = streamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stream not found with ID: " + id));
        return convertToDTO(stream);
    }

    @Override
    public StreamResponseDTO saveStream(StreamResponseDTO streamDTO) {
        Stream stream = new Stream();
        stream.setStreamName(streamDTO.getStreamName());
        Stream saved = streamRepository.save(stream);
        return convertToDTO(saved);
    }

    @Override
    public StreamResponseDTO updateStream(Short id, StreamResponseDTO streamDTO) {
        Stream existing = streamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stream not found with ID: " + id));
        existing.setStreamName(streamDTO.getStreamName());
        Stream updated = streamRepository.save(existing);
        return convertToDTO(updated);
    }

    @Override
    public void deleteStream(Short id) {
        if (!streamRepository.existsById(id)) {
            throw new EntityNotFoundException("Cannot delete. Stream not found with ID: " + id);
        }
        streamRepository.deleteById(id);
    }

    private StreamResponseDTO convertToDTO(Stream stream) {
        StreamResponseDTO dto = new StreamResponseDTO();
        dto.setStreamId(stream.getStreamId());
        dto.setStreamName(stream.getStreamName());
        return dto;
    }
    @Override
    public Stream findById(Short id) {
        return streamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stream not found"));
    }
}
