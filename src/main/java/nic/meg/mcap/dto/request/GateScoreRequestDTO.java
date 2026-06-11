package nic.meg.mcap.dto.request;

import lombok.Data;

@Data
public class GateScoreRequestDTO {
    private String registrationNumber;
    private String yearOfExam;
    private String subject;
    private Double score;
}