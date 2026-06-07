package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.SilverDTO;
import com.rishanth.flux360.dto.SilverSummaryDTO;
import com.rishanth.flux360.service.SilverService;
import com.rishanth.flux360.service.SilverPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/silver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class SilverController {

    private final SilverPriceService silverPriceService;
    private final SilverService service;

    @GetMapping("/price")
    public ResponseEntity<BigDecimal> getSilverPrice() {

        return ResponseEntity.ok(
                silverPriceService.getLiveSilverPricePerGram()
        );
    }

    @PostMapping("/invest")
    public ResponseEntity<SilverDTO> addInvestment(
            @Valid @RequestBody SilverDTO dto,
            Principal principal
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.addInvestment(
                                principal.getName(),
                                dto
                        )
                );
    }

    @GetMapping("/portfolio")
    public ResponseEntity<SilverSummaryDTO>
    getPortfolio(
            Principal principal
    ) {

        return ResponseEntity.ok(
                service.getPortfolioSummary(
                        principal.getName()
                )
        );
    }

    @DeleteMapping("/invest/{id}")
    public ResponseEntity<Void> deleteInvestment(
            @PathVariable Long id,
            Principal principal
    ) {

        service.deleteInvestment(
                id,
                principal.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/invest/{id}")
    public ResponseEntity<SilverDTO> updateInvestment(
            @PathVariable Long id,
            @Valid @RequestBody SilverDTO dto,
            Principal principal
    ) {

        return ResponseEntity.ok(
                service.updateInvestment(
                        id,
                        dto,
                        principal.getName()
                )
        );
    }
}