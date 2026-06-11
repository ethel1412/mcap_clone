package nic.meg.mcap.services.impl;

import nic.meg.mcap.entities.SubjectCombinationHeader;
import nic.meg.mcap.repositories.SubjectCombinationHeaderRepository;
import nic.meg.mcap.services.SubjectCombinationHeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubjectCombinationHeaderServiceImpl implements SubjectCombinationHeaderService {

    @Autowired
    private final SubjectCombinationHeaderRepository repository;
    @Override
    public SubjectCombinationHeader findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Combination header not found"));
    }
}
