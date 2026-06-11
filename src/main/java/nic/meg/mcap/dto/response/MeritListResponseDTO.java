package nic.meg.mcap.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MeritListResponseDTO {

    private MeritListMetadataDTO metadata;

    private List<MeritListRowDTO> entries;
}