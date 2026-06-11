package nic.meg.mcap.services;

import java.util.List;

import nic.meg.mcap.dto.response.StreamResponseDTO;
import nic.meg.mcap.entities.Stream;

public interface StreamService {

    List<StreamResponseDTO> getAllStreams();

    StreamResponseDTO getStreamById(Short id);

    StreamResponseDTO saveStream(StreamResponseDTO streamDTO);

    StreamResponseDTO updateStream(Short id, StreamResponseDTO streamDTO);

    void deleteStream(Short id);

    Stream findById(Short id);
}
