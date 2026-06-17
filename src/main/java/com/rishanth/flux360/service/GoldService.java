package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.GoldDTO;
import com.rishanth.flux360.dto.GoldHistoryDTO;
import com.rishanth.flux360.dto.GoldSummaryDTO;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.mapper.GoldMapper;
import com.rishanth.flux360.entity.Gold;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.repository.GoldRepository;
import com.rishanth.flux360.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoldService {

    private final GoldRepository repo;
    private final UserRepository userRepository;
    private final GoldPriceService priceService;

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────

    @Transactional
    public GoldDTO addGold(GoldDTO dto, String email) {

        User user = getUser(email);

        Gold gold = Gold.builder()
                .gramsPurchased(dto.getGramsPurchased())
                .purchasePricePerGram(dto.getPurchasePricePerGram())
                .purchaseDate(dto.getPurchaseDate())
                .totalInvested(calculateTotalInvested(dto))
                .user(user)
                .build();

        log.info("Gold added for user: {}", email);

        return GoldMapper.toDTO(
                repo.save(gold)
        );
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    @Transactional
    public GoldDTO updateGold(
            Long id,
            GoldDTO dto,
            String email
    ) {

        User user = getUser(email);

        Gold gold = getGoldById(id, user.getId());

        gold.setGramsPurchased(dto.getGramsPurchased());
        gold.setPurchasePricePerGram(dto.getPurchasePricePerGram());
        gold.setPurchaseDate(dto.getPurchaseDate());

        gold.setTotalInvested(
                calculateTotalInvested(dto)
        );

        log.info(
                "Gold updated. Id: {}, User: {}",
                id,
                email
        );

        return GoldMapper.toDTO(
                repo.save(gold)
        );
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────

    @Transactional
    public void deleteGold(Long id, String email) {

        User user = getUser(email);

        Gold gold = getGoldById(id, user.getId());

        repo.delete(gold);

        log.info(
                "Gold deleted. Id: {}, User: {}",
                id,
                email
        );
    }

    // ─────────────────────────────────────────────
    // SUMMARY
    // ─────────────────────────────────────────────

    public GoldSummaryDTO getSummary(String email) {

        User user = getUser(email);

        List<Gold> list =
                repo.findByUserIdOrderByPurchaseDateDesc(
                        user.getId()
                );

        return buildSummary(list);
    }

    public GoldSummaryDTO getFilteredSummary(
            String email,
            int year,
            int month
    ) {

        validateMonth(month);

        User user = getUser(email);

        LocalDate[] range =
                getMonthRange(year, month);

        List<Gold> list =
                repo.findByUserIdAndPurchaseDateBetweenOrderByPurchaseDateDesc(
                        user.getId(),
                        range[0],
                        range[1]
                );

        return buildSummary(list);
    }

    // ─────────────────────────────────────────────
    // HISTORY
    // ─────────────────────────────────────────────

    public List<GoldHistoryDTO> getAllGold(
            String email
    ) {

        User user = getUser(email);

        BigDecimal livePrice =
                priceService.getLiveGoldPricePerGram();

        return repo.findByUserIdOrderByPurchaseDateDesc(
                        user.getId()
                )
                .stream()
                .map(gold ->
                        buildHistoryDTO(
                                gold,
                                livePrice
                        )
                )
                .toList();
    }

    public List<GoldHistoryDTO> getFilteredHistory(
            String email,
            int year,
            int month
    ) {

        validateMonth(month);

        User user = getUser(email);

        LocalDate[] range =
                getMonthRange(year, month);

        BigDecimal livePrice =
                priceService.getLiveGoldPricePerGram();

        return repo
                .findByUserIdAndPurchaseDateBetweenOrderByPurchaseDateDesc(
                        user.getId(),
                        range[0],
                        range[1]
                )
                .stream()
                .map(gold ->
                        buildHistoryDTO(
                                gold,
                                livePrice
                        )
                )
                .toList();
    }

    // ─────────────────────────────────────────────
    // SUMMARY BUILDER
    // ─────────────────────────────────────────────

    private GoldSummaryDTO buildSummary(
            List<Gold> list
    ) {

        BigDecimal totalGrams = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;

        for (Gold gold : list) {

            totalGrams =
                    totalGrams.add(
                            gold.getGramsPurchased()
                    );

            totalInvested =
                    totalInvested.add(
                            gold.getTotalInvested()
                    );
        }

        BigDecimal livePrice =
                priceService.getLiveGoldPricePerGram();

        BigDecimal currentValue =
                totalGrams.multiply(livePrice)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal profitLoss =
                currentValue.subtract(totalInvested)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new GoldSummaryDTO(
                totalGrams,
                totalInvested,
                livePrice,
                currentValue,
                profitLoss,
                false,
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "dd-MM-yyyy HH:mm"
                                )
                        )
        );
    }

    // ─────────────────────────────────────────────
    // HISTORY DTO BUILDER
    // ─────────────────────────────────────────────

    private GoldHistoryDTO buildHistoryDTO(
            Gold gold,
            BigDecimal livePrice
    ) {

        BigDecimal currentValue =
                gold.getGramsPurchased()
                        .multiply(livePrice)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal profit =
                currentValue.subtract(
                                gold.getTotalInvested()
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new GoldHistoryDTO(
                gold.getId(),
                gold.getPurchaseDate(),
                gold.getGramsPurchased(),
                gold.getPurchasePricePerGram(),
                gold.getTotalInvested(),
                currentValue,
                profit
        );
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private BigDecimal calculateTotalInvested(
            GoldDTO dto
    ) {

        return dto.getGramsPurchased()
                .multiply(dto.getPurchasePricePerGram())
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private Gold getGoldById(
            Long id,
            Long userId
    ) {

        return repo.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Gold record not found"
                        )
                );
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private void validateMonth(int month) {

        if (month < 1 || month > 12) {

            throw new IllegalArgumentException(
                    "Invalid month"
            );
        }
    }

    private LocalDate[] getMonthRange(
            int year,
            int month
    ) {

        LocalDate start =
                LocalDate.of(year, month, 1);

        LocalDate end =
                start.withDayOfMonth(
                        start.lengthOfMonth()
                );

        return new LocalDate[]{
                start,
                end
        };
    }
}