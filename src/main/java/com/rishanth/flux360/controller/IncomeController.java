package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.IncomeDTO;
import com.rishanth.flux360.mapper.IncomeMapper;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.service.IncomeService;
import com.rishanth.flux360.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<IncomeDTO> createIncome(
            @Valid @RequestBody IncomeDTO dto,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        IncomeMapper.toDTO(
                                incomeService.createIncome(dto, user)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomes(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        List<IncomeDTO> incomes = incomeService
                .findByUser(user)
                .stream()
                .map(IncomeMapper::toDTO)
                .toList();

        return ResponseEntity.ok(incomes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeDTO> updateIncome(
            @PathVariable Long id,
            @Valid @RequestBody IncomeDTO dto,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity.ok(
                IncomeMapper.toDTO(
                        incomeService.updateIncome(id, dto, user)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        incomeService.deleteIncome(id, user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<IncomeDTO>> getIncomesByMonth(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month");
        }

        User user = getCurrentUser(authentication);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<IncomeDTO> incomes = incomeService
                .findByUserAndDateRange(user, start, end)
                .stream()
                .map(IncomeMapper::toDTO)
                .toList();

        return ResponseEntity.ok(incomes);
    }

    private User getCurrentUser(Authentication authentication) {

        return userService.findByEmail(authentication.getName());
    }
}