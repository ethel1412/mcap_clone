package nic.meg.mcap.dto.request; // or response, can be shared
import lombok.Data;

@Data
public class CategoryRelaxationDTO {
    private String categoryCode;
    private Double relaxationValue;
}