package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.ExpenseDTO;
import com.rishanth.flux360.mapper.ExpenseMapper;
import com.rishanth.flux360.entity.User;
import com.rishanth.flux360.service.ExpenseService;
import com.rishanth.flux360.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ExpenseController {

    private final ExpenseService service;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(
            @Valid @RequestBody ExpenseDTO dto,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ExpenseMapper.toDTO(
                                service.saveExpense(dto, user)
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        List<ExpenseDTO> expenses = service
                .getExpensesByUser(user)
                .stream()
                .map(ExpenseMapper::toDTO)
                .toList();

        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotal(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity.ok(
                service.getTotalExpense(user.getId())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO dto,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return ResponseEntity.ok(
                ExpenseMapper.toDTO(
                        service.updateExpense(id, dto, user)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        service.deleteExpense(id, user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByMonth(
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

        List<ExpenseDTO> expenses = service
                .getByDateRange(user, start, end)
                .stream()
                .map(ExpenseMapper::toDTO)
                .toList();

        return ResponseEntity.ok(expenses);
    }

    private User getCurrentUser(Authentication authentication) {

        return userService.findByEmail(authentication.getName());
    }
}