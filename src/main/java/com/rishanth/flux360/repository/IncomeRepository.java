package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.Income;
import com.rishanth.flux360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUserOrderByDateDesc(User user);

    Optional<Income> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT COALESCE(SUM(i.amount), 0)
            FROM Income i
            WHERE i.user.id = :userId
            """)
    BigDecimal getTotalIncome(Long userId);

    List<Income> findByUserAndDateBetweenOrderByDateDesc(
            User user,
            LocalDate start,
            LocalDate end
    );
}