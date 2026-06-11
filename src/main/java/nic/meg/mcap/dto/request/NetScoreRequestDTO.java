package nic.meg.mcap.dto.request;

import lombok.Data;

@Data
public class NetScoreRequestDTO {
    private String applicationNumber;
    private String yearOfExam;
    private String subject;
    private Double percentile;
}