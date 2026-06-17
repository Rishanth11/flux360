package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.*;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.mapper.SipMapper;
import com.rishanth.flux360.entity.SipInvestment;
import com.rishanth.flux360.entity.SipTransaction;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.repository.SipInvestmentRepository;
import com.rishanth.flux360.repository.SipTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SipService {

    private final SipInvestmentRepository sipRepo;

    private final SipTransactionRepository txnRepo;

    private final NavService navService;

    private final XirrService xirrService;

    private final UserService userService;

    // ─────────────────────────────────────────────
    // CREATE SIP
    // ─────────────────────────────────────────────

    public void createSip(
            SipRequestDTO dto,
            String email
    ) {

        User user =
                userService.findByEmail(email);

        SipInvestment sip =
                SipMapper.toEntity(dto);

        sip.setUser(user);

        sipRepo.save(sip);
    }

    // ─────────────────────────────────────────────
    // OVERALL PORTFOLIO
    // ─────────────────────────────────────────────

    public SipPortfolioDTO getPortfolio(
            Long sipId,
            User user
    ) {

        SipInvestment sip =
                sipRepo.findByIdAndUserId(
                        sipId,
                        user.getId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "SIP not found"
                        )
                );

        List<SipTransaction> txns =
                txnRepo.findBySipOrderByInvestDateAsc(
                        sip
                );

        BigDecimal totalInvested =
                txns.stream()
                        .map(SipTransaction::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (txns.isEmpty()) {

            return new SipPortfolioDTO(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false,
                    null,
                    sip.getGoalName(),
                    sip.getTargetAmount()
            );
        }

        BigDecimal latestNav;

        try {

            latestNav =
                    navService.fetchLatestNav(
                            sip.getFundCode()
                    );

        } catch (Exception e) {

            latestNav =
                    txns.get(
                            txns.size() - 1
                    ).getNav();
        }

        BigDecimal totalUnits =
                txns.stream()
                        .map(SipTransaction::getUnits)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal currentValue =
                totalUnits.multiply(latestNav);

        BigDecimal returns =
                currentValue.subtract(
                        totalInvested
                );

        double xirrValue =
                xirrService.calculate(
                        txns,
                        currentValue.doubleValue()
                );

        BigDecimal xirr =
                BigDecimal.valueOf(xirrValue);

        BigDecimal inflation =
                sip.getInflationRate() != null
                        ? sip.getInflationRate()
                        : BigDecimal.valueOf(6);

        BigDecimal realReturn =
                xirr.subtract(inflation);

        BigDecimal goalProgress =
                BigDecimal.ZERO;

        if (sip.getTargetAmount() != null &&
                sip.getTargetAmount()
                        .compareTo(BigDecimal.ZERO) > 0) {

            goalProgress =
                    currentValue.multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .divide(
                                    sip.getTargetAmount(),
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        return new SipPortfolioDTO(
                totalInvested.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                currentValue.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                returns.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                totalUnits.setScale(
                        4,
                        RoundingMode.HALF_UP
                ),
                xirr.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                realReturn.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                goalProgress.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                true,
                null,
                sip.getGoalName(),
                sip.getTargetAmount()
        );
    }

    // ─────────────────────────────────────────────
    // TRANSACTIONS
    // ─────────────────────────────────────────────

    public List<SipTransactionDTO> getTransactions(
            Long sipId,
            String email
    ) {

        User user =
                userService.findByEmail(email);

        SipInvestment sip =
                getOwnedSip(sipId, user);

        return txnRepo
                .findBySipOrderByInvestDateAsc(sip)
                .stream()
                .map(SipMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // EXECUTE SIP
    // ─────────────────────────────────────────────

    public void executeSipNow(
            Long sipId,
            User user
    ) {

        SipInvestment sip =
                getOwnedSip(sipId, user);

        LocalDate today =
                LocalDate.now();

        boolean alreadyExecuted =
                txnRepo.existsBySipAndInvestDate(
                        sip,
                        today
                );

        if (alreadyExecuted) {

            log.info(
                    "SIP already executed today for SIP {}",
                    sipId
            );

            return;
        }

        BigDecimal nav =
                navService.fetchLatestNav(
                        sip.getFundCode()
                );

        BigDecimal units =
                sip.getMonthlyAmount()
                        .divide(
                                nav,
                                6,
                                RoundingMode.HALF_UP
                        );

        SipTransaction txn =
                SipTransaction.builder()
                        .sip(sip)
                        .investDate(today)
                        .nav(nav)
                        .units(units)
                        .amount(sip.getMonthlyAmount())
                        .build();

        txnRepo.save(txn);

        log.info(
                "SIP executed successfully for SIP {}",
                sipId
        );
    }

    // ─────────────────────────────────────────────
    // STOP SIP
    // ─────────────────────────────────────────────

    public void stopSip(
            Long sipId,
            String email
    ) {

        User user =
                userService.findByEmail(email);

        SipInvestment sip =
                getOwnedSip(sipId, user);

        sip.setActive(false);

        sipRepo.save(sip);
    }

    // ─────────────────────────────────────────────
    // ALL SIPS
    // ─────────────────────────────────────────────

    public List<SipListDTO> getAllSips(
            String email
    ) {

        User user =
                userService.findByEmail(email);

        return sipRepo.findByUserId(user.getId())
                .stream()
                .map(SipMapper::toListDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // SIP CHART
    // ─────────────────────────────────────────────

    public List<Map<String, Object>> getSipChart(
            Long sipId,
            User user
    ) {

        SipInvestment sip =
                getOwnedSip(sipId, user);

        List<SipTransaction> txns =
                txnRepo.findBySipOrderByInvestDateAsc(sip);

        List<Map<String, Object>> chart =
                new ArrayList<>();

        BigDecimal cumulative =
                BigDecimal.ZERO;

        for (SipTransaction txn : txns) {

            cumulative =
                    cumulative.add(
                            txn.getAmount()
                    );

            Map<String, Object> point =
                    new HashMap<>();

            point.put(
                    "date",
                    txn.getInvestDate().toString()
            );

            point.put(
                    "invested",
                    cumulative
            );

            chart.add(point);
        }

        return chart;
    }

    // ─────────────────────────────────────────────
    // PRIVATE
    // ─────────────────────────────────────────────

    private SipInvestment getOwnedSip(
            Long sipId,
            User user
    ) {

        return sipRepo
                .findByIdAndUserId(
                        sipId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "SIP not found"
                        )
                );
    }
}