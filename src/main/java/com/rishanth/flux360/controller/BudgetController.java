package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.AlertDTO;
import com.rishanth.flux360.dto.BudgetExpenseRequestDTO;
import com.rishanth.flux360.dto.BudgetRequestDTO;
import com.rishanth.flux360.dto.BudgetSummaryDTO;
import com.rishanth.flux360.entity.Budget;
import com.rishanth.flux360.entity.BudgetAlert;
import com.rishanth.flux360.entity.BudgetExpense;
import com.rishanth.flux360.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('USER')")
public class BudgetController {

    private final BudgetService budgetService;

    // ── BUDGETS ───────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Budget> createBudget(
            @Valid @RequestBody BudgetRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.createBudget(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getBudgetsByUser(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.getBudgetsByUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.getBudgetById(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.updateBudget(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long id,
            Authentication authentication
    ) {
        budgetService.deleteBudget(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // ── EXPENSES ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/expenses")
    public ResponseEntity<BudgetExpense> addExpense(
            @PathVariable Long id,
            @Valid @RequestBody BudgetExpenseRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.addExpense(id, request, authentication.getName()));
    }

    @GetMapping("/{id}/expenses")
    public ResponseEntity<List<BudgetExpense>> getExpenses(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.getExpenses(id, authentication.getName()));
    }

    // ── SUMMARY ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/summary")
    public ResponseEntity<BudgetSummaryDTO> getSummary(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.getBudgetSummary(id, authentication.getName()));
    }

    // ── ALERTS ───────────────────────────────────────────────────────────────

    /**
     * FIX — new endpoint: returns all alerts for the authenticated user across
     * every budget in a single request. Replaces the per-budget fan-out that
     * the frontend previously used (one request per budget in Promise.all).
     *
     * Each AlertDTO carries budgetName so the frontend never has to join
     * client-side.
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDTO>> getAllAlerts(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.getAllAlertsForUser(authentication.getName()));
    }

    @GetMapping("/{id}/alerts")
    public ResponseEntity<List<BudgetAlert>> getAlerts(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.getAlerts(id, authentication.getName()));
    }

    @PutMapping("/alerts/{alertId}/seen")
    public ResponseEntity<Void> markAlertSeen(
            @PathVariable Long alertId,
            Authentication authentication
    ) {
        budgetService.markAlertSeen(alertId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<BudgetSummaryDTO>> getAllSummaries(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                budgetService.getAllBudgetSummaries(authentication.getName()));
    }
}