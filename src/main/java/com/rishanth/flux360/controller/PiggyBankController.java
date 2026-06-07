package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.*;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/piggy-bank")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class PiggyBankController {

    private final PiggyBankService service;
    private final UserService userService;

    @GetMapping("/summary")
    public ResponseEntity<PiggyBankSummaryDTO> summary(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity.ok(
                service.getSummary(user)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<PiggyBankTransactionDTO>>
    history(Authentication authentication) {

        User user = getCurrentUser(authentication);

        return ResponseEntity.ok(
                service.getHistory(user)
        );
    }

    @PostMapping("/deposit")
    public ResponseEntity<PiggyBankTransactionDTO>
    deposit(
            @Valid
            @RequestBody PiggyBankRequestDTO dto,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity.ok(
                service.deposit(
                        dto.getAmount(),
                        dto.getNote(),
                        user
                )
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<PiggyBankTransactionDTO>
    withdraw(
            @Valid
            @RequestBody PiggyBankRequestDTO dto,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity.ok(
                service.withdraw(
                        dto.getAmount(),
                        dto.getNote(),
                        user
                )
        );
    }

    private User getCurrentUser(
            Authentication authentication
    ) {
        return userService.findByEmail(
                authentication.getName()
        );
    }
}