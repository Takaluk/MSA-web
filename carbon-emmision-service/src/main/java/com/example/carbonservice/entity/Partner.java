package com.example.carbonservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Partner {
    @Id
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "emission_year")
    private int emissionYear;  // 연도는 int 타입이 적절

    @Column(name = "carbon_emission")
    private double carbonEmission;  // 숫자 타입으로 변경

    private String region;
    private String description;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getEmissionYear() {
        return emissionYear;
    }

    public void setEmissionYear(int emissionYear) {
        this.emissionYear = emissionYear;
    }

    public double getCarbonEmission() {
        return carbonEmission;
    }

    public void setCarbonEmission(double carbonEmission) {
        this.carbonEmission = carbonEmission;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
