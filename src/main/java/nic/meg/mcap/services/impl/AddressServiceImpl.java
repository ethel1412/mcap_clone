package nic.meg.mcap.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import nic.meg.mcap.entities.Address;
import nic.meg.mcap.repositories.AddressRepository;
import nic.meg.mcap.services.AddressService;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<Address> getAll() {
        return addressRepository.findAll();
    }

}
