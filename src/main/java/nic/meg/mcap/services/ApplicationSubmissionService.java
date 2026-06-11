package nic.meg.mcap.services;

import nic.meg.mcap.entities.Application;

public interface ApplicationSubmissionService {
    void finalizeApplicationSubmission(Application app);
}