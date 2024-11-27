package com.example.carbonservice.controller;

import com.example.carbonservice.entity.Partner;
import com.example.carbonservice.service.CarbonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partners")
public class CarbonController {
    @Autowired
    private CarbonService carbonService;

    // 모든 사용자 조회
    @GetMapping("/carbon")
    public ResponseEntity<Iterable<Partner>> getAllPartners() {
        return ResponseEntity.ok(carbonService.getAllPartners());
    }
    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Partner Service is healthy");
    }
}
