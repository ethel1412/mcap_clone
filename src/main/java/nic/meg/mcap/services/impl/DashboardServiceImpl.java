package nic.meg.mcap.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nic.meg.mcap.services.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	private nic.meg.mcap.repositories.UserRepository userRepository;

	@Override
	public long getTotalofNumberUsers() {
		return userRepository.count();

	}
}