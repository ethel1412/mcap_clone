package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.AcademicDetailsDTO;

import java.util.List;

public interface AcademicService {
    void saveOrUpdateAcademicDetails(String applicantNo, AcademicDetailsDTO academicDetailsDTO);
    AcademicDetailsDTO getAcademicDetails(String applicantNo);
    List<String> getStudiedSubjectNames(String applicantNo);
}