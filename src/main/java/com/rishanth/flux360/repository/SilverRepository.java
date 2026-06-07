package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.Silver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SilverRepository
        extends JpaRepository<Silver, Long> {

    List<Silver>
    findByUserIdOrderByPurchaseDateDesc(Long userId);

    Optional<Silver>
    findByIdAndUserId(Long id, Long userId);
}