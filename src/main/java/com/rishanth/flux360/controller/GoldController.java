package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.GoldDTO;
import com.rishanth.flux360.dto.GoldHistoryDTO;
import com.rishanth.flux360.dto.GoldSummaryDTO;
import com.rishanth.flux360.service.GoldPriceService;
import com.rishanth.flux360.service.GoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/gold")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class GoldController {

    private final GoldService service;
    private final GoldPriceService goldPriceService;

    @PostMapping
    public ResponseEntity<GoldDTO> addGold(
            @Valid @RequestBody GoldDTO dto,
            Principal principal
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.addGold(
                                dto,
                                principal.getName()
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoldDTO> updateGold(
            @PathVariable Long id,
            @Valid @RequestBody GoldDTO dto,
            Principal principal
    ) {

        return ResponseEntity.ok(
                service.updateGold(
                        id,
                        dto,
                        principal.getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGold(
            @PathVariable Long id,
            Principal principal
    ) {

        service.deleteGold(id, principal.getName());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GoldHistoryDTO>> getAllGold(
            Principal principal
    ) {

        return ResponseEntity.ok(
                service.getAllGold(principal.getName())
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<GoldSummaryDTO> getSummary(
            Principal principal
    ) {

        return ResponseEntity.ok(
                service.getSummary(principal.getName())
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<List<GoldHistoryDTO>> getFilteredHistory(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal
    ) {

        validateMonth(month);

        return ResponseEntity.ok(
                service.getFilteredHistory(
                        principal.getName(),
                        year,
                        month
                )
        );
    }

    @GetMapping("/summary/filter")
    public ResponseEntity<GoldSummaryDTO> getFilteredSummary(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal
    ) {

        validateMonth(month);

        return ResponseEntity.ok(
                service.getFilteredSummary(
                        principal.getName(),
                        year,
                        month
                )
        );
    }

    @GetMapping("/price")
    public ResponseEntity<BigDecimal> getGoldPrice() {

        return ResponseEntity.ok(
                goldPriceService.getLiveGoldPricePerGram()
        );
    }

    private void validateMonth(int month) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Invalid month"
            );
        }
    }
}