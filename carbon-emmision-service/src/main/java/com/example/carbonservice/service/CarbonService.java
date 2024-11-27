package com.example.carbonservice.service;

import com.example.carbonservice.entity.Partner;
import com.example.carbonservice.repository.CarbonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarbonService {

    @Autowired
    private CarbonRepository carbonRepository;

    public Iterable<Partner> getAllPartners() {
        Iterable<Partner> partners = carbonRepository.findAll();
        return partners;
    }
}