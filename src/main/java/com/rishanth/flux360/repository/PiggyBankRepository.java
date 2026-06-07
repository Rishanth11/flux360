package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PiggyBankRepository
        extends JpaRepository<PiggyBankTransaction, Long> {

    List<PiggyBankTransaction>
    findByUserOrderByTransactionDateDesc(User user);

    @Query("""
            SELECT COALESCE(SUM(p.amount),0)
            FROM PiggyBankTransaction p
            WHERE p.user.id=:userId
            AND p.type='DEPOSIT'
            """)
    BigDecimal totalDeposits(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COALESCE(SUM(p.amount),0)
            FROM PiggyBankTransaction p
            WHERE p.user.id=:userId
            AND p.type='WITHDRAW'
            """)
    BigDecimal totalWithdrawals(
            @Param("userId") Long userId
    );
}