package com.rishanth.flux360.mapper;

import com.rishanth.flux360.dto.SipListDTO;
import com.rishanth.flux360.dto.SipRequestDTO;
import com.rishanth.flux360.dto.SipTransactionDTO;
import com.rishanth.flux360.entity.SipInvestment;
import com.rishanth.flux360.entity.SipTransaction;

import java.math.BigDecimal;

public class SipMapper {

    private SipMapper() {}

    // ─────────────────────────────────────────────

    public static SipInvestment toEntity(
            SipRequestDTO dto
    ) {

        return SipInvestment.builder()
                .fundName(dto.getFundName())
                .fundCode(dto.getFundCode())
                .monthlyAmount(dto.getMonthlyAmount())
                .startDate(dto.getStartDate())
                .sipDay(dto.getSipDay())
                .goalName(dto.getGoalName())
                .targetAmount(dto.getTargetAmount())
                .inflationRate(
                        dto.getInflationRate() != null
                                ? dto.getInflationRate()
                                : BigDecimal.valueOf(6.0)
                )
                .active(true)
                .build();
    }

    // ─────────────────────────────────────────────

    public static SipTransactionDTO toDTO(
            SipTransaction txn
    ) {

        return new SipTransactionDTO(
                txn.getId(),
                txn.getInvestDate(),
                txn.getNav(),
                txn.getUnits(),
                txn.getAmount(),
                txn.getUnits().multiply(txn.getNav())
        );
    }

    public static SipListDTO toListDTO(
            SipInvestment sip
    ) {

        return SipListDTO.builder()
                .id(sip.getId())
                .fundName(sip.getFundName())
                .fundCode(sip.getFundCode())
                .monthlyAmount(sip.getMonthlyAmount())
                .startDate(sip.getStartDate())
                .sipDay(sip.getSipDay())
                .active(sip.isActive())
                .goalName(sip.getGoalName())
                .targetAmount(sip.getTargetAmount())
                .build();
    }
}