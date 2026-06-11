package nic.meg.mcap.services.impl;

import nic.meg.mcap.entities.CommunityCategory;
import nic.meg.mcap.repositories.CommunityCategoryRepository;
import nic.meg.mcap.services.CommunityCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommunityCategoryServiceImpl implements CommunityCategoryService {

    private final CommunityCategoryRepository repository;

    public CommunityCategoryServiceImpl(CommunityCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityCategory> getAllCategories() {
        return repository.findAll();
    }
}
