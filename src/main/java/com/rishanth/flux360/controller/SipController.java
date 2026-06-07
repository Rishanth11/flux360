package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.SipListDTO;
import com.rishanth.flux360.dto.SipPortfolioDTO;
import com.rishanth.flux360.dto.SipRequestDTO;
import com.rishanth.flux360.dto.SipTransactionDTO;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.service.SipService;
import com.rishanth.flux360.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sip")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class SipController {

    private final SipService sipService;

    private final UserService userService;

    // ─────────────────────────────────────────────
    // CREATE SIP
    // ─────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<String> addSip(
            @Valid @RequestBody SipRequestDTO dto,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        sipService.createSip(
                dto,
                email
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("SIP created successfully");
    }

    // ─────────────────────────────────────────────
    // ALL SIPS
    // ─────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<SipListDTO>>
    getAllSips(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(
                sipService.getAllSips(email)
        );
    }

    // ─────────────────────────────────────────────
    // PORTFOLIO
    // ─────────────────────────────────────────────

    @GetMapping("/{sipId}/portfolio")
    public ResponseEntity<SipPortfolioDTO>
    getPortfolio(
            @PathVariable Long sipId,
            Authentication authentication
    ) {

        User user =
                userService.findByEmail(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                sipService.getPortfolio(
                        sipId,
                        user
                )
        );
    }
    // ─────────────────────────────────────────────
    // SIP TRANSACTIONS
    // ─────────────────────────────────────────────

    @GetMapping("/{sipId}/transactions")
    public ResponseEntity<List<SipTransactionDTO>>
    getTransactions(
            @PathVariable Long sipId,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(
                sipService.getTransactions(
                        sipId,
                        email
                )
        );
    }

    // ─────────────────────────────────────────────
    // EXECUTE SIP
    // ─────────────────────────────────────────────

    @PostMapping("/{sipId}/execute")
    public ResponseEntity<String>
    executeSip(
            @PathVariable Long sipId,
            Authentication authentication
    ) {

        User user =
                userService.findByEmail(
                        authentication.getName()
                );

        sipService.executeSipNow(
                sipId,
                user
        );

        return ResponseEntity.ok(
                "SIP executed successfully"
        );
    }

    // ─────────────────────────────────────────────
    // SIP CHART
    // ─────────────────────────────────────────────

    @GetMapping("/{sipId}/chart")
    public ResponseEntity<List<Map<String, Object>>>
    getSipChart(
            @PathVariable Long sipId,
            Authentication authentication
    ) {

        User user =
                userService.findByEmail(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                sipService.getSipChart(
                        sipId,
                        user
                )
        );
    }

    // ─────────────────────────────────────────────
    // STOP SIP
    // ─────────────────────────────────────────────

    @PutMapping("/{sipId}/stop")
    public ResponseEntity<String> stopSip(
            @PathVariable Long sipId,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        sipService.stopSip(
                sipId,
                email
        );

        return ResponseEntity.ok(
                "SIP stopped successfully"
        );
    }
}