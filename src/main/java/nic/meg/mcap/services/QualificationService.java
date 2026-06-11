package nic.meg.mcap.services;

import nic.meg.mcap.entities.Qualification;
import java.util.List;

public interface QualificationService {
    List<Qualification> getAllActiveQualifications();

}