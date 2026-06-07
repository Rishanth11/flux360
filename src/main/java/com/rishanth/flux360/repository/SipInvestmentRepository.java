package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.SipInvestment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SipInvestmentRepository
        extends JpaRepository<SipInvestment, Long> {

    List<SipInvestment>
    findByUserId(Long userId);

    Optional<SipInvestment>
    findByIdAndUserId(
            Long id,
            Long userId
    );

    List<SipInvestment>
    findByActiveTrue();
}