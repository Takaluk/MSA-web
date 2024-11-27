package com.example.carbonservice.repository;

import com.example.carbonservice.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarbonRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByCompanyName(String companyName);  // 필드명과 일치하도록 수정
}
