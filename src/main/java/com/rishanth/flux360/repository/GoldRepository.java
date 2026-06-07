package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.Gold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoldRepository extends JpaRepository<Gold, Long> {

    List<Gold> findByUserIdOrderByPurchaseDateDesc(Long userId);

    Optional<Gold> findByIdAndUserId(Long id, Long userId);

    List<Gold> findByUserIdAndPurchaseDateBetweenOrderByPurchaseDateDesc(
            Long userId,
            LocalDate start,
            LocalDate end
    );
}