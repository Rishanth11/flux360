package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.SilverDTO;
import com.rishanth.flux360.dto.SilverHistoryDTO;
import com.rishanth.flux360.dto.SilverSummaryDTO;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.mapper.SilverMapper;
import com.rishanth.flux360.entity.Silver;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.repository.SilverRepository;
import com.rishanth.flux360.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SilverService {

    private final SilverRepository repo;
    private final SilverPriceService silverPriceService;
    private final UserRepository userRepository;

    @Transactional
    public SilverDTO addInvestment(
            String username,
            SilverDTO dto
    ) {

        User user = getUser(username);

        Silver investment =
                Silver.builder()
                        .grams(dto.getGrams())
                        .pricePerGram(dto.getPricePerGram())
                        .purchaseDate(dto.getPurchaseDate())
                        .user(user)
                        .build();

        log.info(
                "Silver investment added for user: {}",
                username
        );

        return SilverMapper.toDTO(
                repo.save(investment)
        );
    }

    public SilverSummaryDTO getPortfolioSummary(
            String username
    ) {

        User user = getUser(username);

        List<Silver> records =
                repo.findByUserIdOrderByPurchaseDateDesc(
                        user.getId()
                );

        if (records.isEmpty()) {

            return new SilverSummaryDTO(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false,
                    null,
                    Collections.emptyList()
            );
        }

        BigDecimal currentPrice =
                silverPriceService.getLiveSilverPricePerGram();

        List<SilverHistoryDTO> investments =
                records.stream()
                        .map(inv ->
                                mapToPnLDTO(
                                        inv,
                                        currentPrice
                                )
                        )
                        .toList();

        BigDecimal totalInvested =
                investments.stream()
                        .map(
                                SilverHistoryDTO::getInvestedAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalCurrentValue =
                investments.stream()
                        .map(
                                SilverHistoryDTO::getCurrentValue
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalGrams =
                investments.stream()
                        .map(
                                SilverHistoryDTO::getGrams
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalPnL =
                totalCurrentValue.subtract(totalInvested);

        BigDecimal totalPnLPercent =
                totalInvested.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : totalPnL.divide(
                                totalInvested,
                                4,
                                RoundingMode.HALF_UP
                        )
                        .multiply(new BigDecimal("100"))
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new SilverSummaryDTO(
                totalGrams,
                totalInvested.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                totalCurrentValue.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                totalPnL.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                totalPnLPercent,
                false,
                java.time.LocalDateTime.now().toString(),
                investments
        );
    }

    @Transactional
    public void deleteInvestment(
            Long investmentId,
            String username
    ) {

        User user = getUser(username);

        Silver inv =
                getInvestmentById(
                        investmentId,
                        user.getId()
                );

        repo.delete(inv);

        log.info(
                "Silver investment deleted. Id: {}",
                investmentId
        );
    }

    @Transactional
    public SilverDTO updateInvestment(
            Long id,
            SilverDTO dto,
            String username
    ) {

        User user = getUser(username);

        Silver inv =
                getInvestmentById(
                        id,
                        user.getId()
                );

        inv.setGrams(dto.getGrams());
        inv.setPricePerGram(dto.getPricePerGram());
        inv.setPurchaseDate(dto.getPurchaseDate());

        log.info(
                "Silver investment updated. Id: {}",
                id
        );

        return SilverMapper.toDTO(
                repo.save(inv)
        );
    }

    // ─────────────────────────────────────────────

    private SilverHistoryDTO mapToPnLDTO(
            Silver inv,
            BigDecimal currentPrice
    ) {

        BigDecimal invested =
                inv.getGrams()
                        .multiply(inv.getPricePerGram())
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal currentValue =
                inv.getGrams()
                        .multiply(currentPrice)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal pnl =
                currentValue.subtract(invested);

        BigDecimal pnlPercent =
                invested.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : pnl.divide(
                                invested,
                                4,
                                RoundingMode.HALF_UP
                        )
                        .multiply(new BigDecimal("100"))
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new SilverHistoryDTO(
                inv.getId(),
                inv.getGrams(),
                inv.getPricePerGram(),
                inv.getPurchaseDate(),
                invested,
                currentPrice,
                currentValue,
                pnl,
                pnlPercent
        );
    }

    private User getUser(String username) {

        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private Silver getInvestmentById(
            Long id,
            Long userId
    ) {

        return repo.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Silver investment not found"
                        )
                );
    }
}