package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.*;
import com.rishanth.flux360.entity.*;
import com.rishanth.flux360.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rishanth.flux360.entity.Expense;
import com.rishanth.flux360.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;
    private final BudgetExpenseRepository expenseRepository;
    private final BudgetAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseMainRepository;

    // ── AUTH HELPERS ──────────────────────────────────────────────────────────

    private User getAuthenticatedUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ROLE_USER) {
            throw new RuntimeException(
                    "Only users can access budget features"
            );
        }

        return user;
    }
    /**
     * Loads a budget by ID and verifies it belongs to the requesting user.
     * A single call is stored in a local variable wherever both the budget
     * object and its user ID are needed — no redundant DB round-trips.
     */
    private Budget findBudgetByIdAndUser(Long budgetId, String email) {
        User user = getAuthenticatedUser(email);
        return budgetRepository
                .findByIdAndUserId(budgetId, user.getId())
                .orElseThrow(() -> new RuntimeException("Budget not found"));
    }

    // ── BUDGETS ───────────────────────────────────────────────────────────────

    @Transactional
    public Budget createBudget(BudgetRequestDTO request, String email) {
        User user = getAuthenticatedUser(email);
        validateCategoryAllocations(request.getCategories(), request.getTotalAmount());

        Budget budget = Budget.builder()
                .user(user)
                .name(request.getName())
                .budgetType(request.getBudgetType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(request.getTotalAmount())
                .build();

        Budget savedBudget = budgetRepository.save(budget);

        if (request.getCategories() != null) {
            request.getCategories().forEach(catDTO ->
                    categoryRepository.save(buildCategory(savedBudget, catDTO))
            );
        }

        // FIX: reload so categories are populated in the response
        return budgetRepository.findByIdAndUserId(savedBudget.getId(), user.getId())
                .orElse(savedBudget);
    }

    public List<Budget> getBudgetsByUser(String email) {
        User user = getAuthenticatedUser(email);
        return budgetRepository.findByUserIdWithCategories(user.getId());
    }

    public Budget getBudgetById(Long id, String email) {
        return findBudgetByIdAndUser(id, email);
    }

    /**
     * FIX — updateBudget no longer blindly deletes and recreates all categories.
     *
     * Strategy:
     *   - Categories present in the request AND already in the DB → update
     *     allocatedAmount and alertThreshold; preserve spentAmount.
     *   - Categories in the request but not in the DB → insert.
     *   - Categories in the DB but absent from the request → delete.
     *
     * Matching is done by categoryName (case-insensitive). This prevents
     * spentAmount history being silently wiped on every update and avoids
     * creating orphaned expense FK references.
     */
    @Transactional
    public Budget updateBudget(Long id, BudgetRequestDTO request, String email) {
        Budget budget = findBudgetByIdAndUser(id, email);

        budget.setName(request.getName());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setTotalAmount(request.getTotalAmount());

        validateCategoryAllocations(request.getCategories(), request.getTotalAmount());

        List<BudgetCategory> existingCategories =
                categoryRepository.findByBudgetId(budget.getId());

        List<BudgetCategoryDTO> incomingCategories =
                request.getCategories() != null ? request.getCategories() : List.of();

        // Build a lookup map of existing categories by name (lower-case key)
        Map<String, BudgetCategory> existingByName = existingCategories.stream()
                .collect(Collectors.toMap(
                        c -> c.getCategoryName().toLowerCase(),
                        c -> c
                ));

        // Names present in the incoming request
        Set<String> incomingNames = incomingCategories.stream()
                .map(d -> d.getCategoryName().toLowerCase())
                .collect(Collectors.toSet());

        // Delete categories that are no longer in the request
        existingCategories.stream()
                .filter(c -> !incomingNames.contains(c.getCategoryName().toLowerCase()))
                .forEach(categoryRepository::delete);

        // Update or insert
        for (BudgetCategoryDTO dto : incomingCategories) {
            String key = dto.getCategoryName().toLowerCase();
            if (existingByName.containsKey(key)) {
                // Update in-place — spentAmount is preserved
                BudgetCategory existing = existingByName.get(key);
                existing.setAllocatedAmount(dto.getAllocatedAmount());
                existing.setAlertThreshold(
                        dto.getAlertThreshold() != null ? dto.getAlertThreshold() : 80);
                categoryRepository.save(existing);
            } else {
                // New category — starts at zero spent
                categoryRepository.save(buildCategory(budget, dto));
            }
        }

        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long id, String email) {
        Budget budget = findBudgetByIdAndUser(id, email);
        budgetRepository.delete(budget);
    }

    // ── EXPENSES ─────────────────────────────────────────────────────────────

    @Transactional
    public BudgetExpense addExpense(
            Long budgetId,
            BudgetExpenseRequestDTO request,
            String email
    ) {

        Budget budget = findBudgetByIdAndUser(
                budgetId,
                email
        );

        BudgetCategory category = null;

        if (request.getBudgetCategoryId() != null) {

            category = categoryRepository
                    .findByIdAndBudgetId(
                            request.getBudgetCategoryId(),
                            budgetId
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Category not found"
                            ));

            BigDecimal currentSpent =
                    category.getSpentAmount() != null
                            ? category.getSpentAmount()
                            : BigDecimal.ZERO;

            category.setSpentAmount(
                    currentSpent.add(
                            request.getAmount()
                    )
            );

            categoryRepository.save(category);

            checkAndTriggerAlerts(
                    budget,
                    category
            );
        }

        BudgetExpense budgetExpense =
                expenseRepository.save(
                        BudgetExpense.builder()
                                .budget(budget)
                                .budgetCategory(category)
                                .description(
                                        request.getDescription()
                                )
                                .amount(
                                        request.getAmount()
                                )
                                .expenseDate(
                                        request.getExpenseDate()
                                )
                                .build()
                );

        ExpenseCategory expenseCategory =
                ExpenseCategory.OTHER;

        if (category != null) {

            try {

                expenseCategory =
                        ExpenseCategory.valueOf(
                                category.getCategoryName()
                                        .trim()
                                        .toUpperCase()
                        );

            } catch (Exception ignored) {
                expenseCategory =
                        ExpenseCategory.OTHER;
            }
        }

        Expense expense = Expense.builder()
                .user(budget.getUser())
                .category(expenseCategory)
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .description(
                        "[Budget] " +
                                (
                                        request.getDescription() == null
                                                ? ""
                                                : request.getDescription()
                                )
                )
                .createdFromBudget(true)
                .budgetExpenseId(budgetExpense.getId())
                .build();
        expenseMainRepository.save(expense);
        return budgetExpense;
    }

    public List<BudgetExpense> getExpenses(Long budgetId, String email) {
        // FIX — single findBudgetByIdAndUser call; reuse the result for the
        // user ID instead of calling the method a second time.
        Budget budget = findBudgetByIdAndUser(budgetId, email);
        return expenseRepository.findByBudgetIdAndBudgetUserId(
                budgetId, budget.getUser().getId());
    }

    // ── ALERTS ───────────────────────────────────────────────────────────────

    /**
     * Deduplicate alerts: only fire a new alert if no unseen alert of the same
     * type already exists for this category.
     *
     * Once an EXCEEDED alert has been seen (dismissed), a fresh expense that
     * still sits above 100% will generate a new one — intentional behaviour so
     * the user is re-notified after they acknowledge the previous alert.
     */
    private void checkAndTriggerAlerts(Budget budget, BudgetCategory category) {
        if (category.getAllocatedAmount().compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal spent = category.getSpentAmount() != null
                ? category.getSpentAmount()
                : BigDecimal.ZERO;

        double percentage = spent
                .divide(category.getAllocatedAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        BudgetAlert.AlertType alertType = null;
        String message = null;

        if (percentage >= 100) {
            alertType = BudgetAlert.AlertType.EXCEEDED;
            message = "Budget EXCEEDED for category: " + category.getCategoryName();
        } else if (percentage >= category.getAlertThreshold()) {
            alertType = BudgetAlert.AlertType.WARNING;
            message = String.format(
                    "%.0f%% of budget used for category: %s",
                    percentage, category.getCategoryName()
            );
        }

        if (alertType != null) {
            boolean alreadyAlerted = alertRepository
                    .existsByBudgetIdAndBudgetCategoryIdAndAlertTypeAndIsSeenFalse(
                            budget.getId(), category.getId(), alertType);
            if (!alreadyAlerted) {
                alertRepository.save(BudgetAlert.builder()
                        .budget(budget)
                        .budgetCategoryId(category.getId())
                        .alertType(alertType)
                        .message(message)
                        .isSeen(false)
                        .build());
            }
        }
    }

    public List<BudgetAlert> getAlerts(Long budgetId, String email) {
        // FIX — single call; reuse result for user ID lookup.
        Budget budget = findBudgetByIdAndUser(budgetId, email);
        return alertRepository.findByBudgetIdAndBudgetUserId(
                budgetId, budget.getUser().getId());
    }

    /**
     * FIX — new method: returns ALL alerts for the authenticated user across
     * every budget in one query. Replaces the N-request fan-out in the frontend.
     */
    @Transactional(readOnly = true)
    public List<AlertDTO> getAllAlertsForUser(String email) {
        User user = getAuthenticatedUser(email);
        return alertRepository.findByBudgetUserId(user.getId())
                .stream()
                .map(a -> AlertDTO.builder()
                        .id(a.getId())
                        .budgetId(a.getBudget().getId())
                        .budgetName(a.getBudget().getName())
                        .budgetCategoryId(a.getBudgetCategoryId())
                        .alertType(a.getAlertType())
                        .message(a.getMessage())
                        .triggeredAt(a.getTriggeredAt())
                        .isSeen(a.getIsSeen())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAlertSeen(Long alertId, String email) {
        User user = getAuthenticatedUser(email);
        BudgetAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        // Ownership check — return the same message to avoid enumeration
        if (!alert.getBudget().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Alert not found");
        }

        alert.setIsSeen(true);
        alertRepository.save(alert);
    }

    // ── SUMMARY ───────────────────────────────────────────────────────────────

    /**
     * FIX — totalSpent is now sourced exclusively from raw expense records
     * (the authoritative ledger). category.spentAmount is used only for the
     * per-category breakdown. uncategorizedSpent is shown separately so the
     * caller can tell users about expenses not tied to any category.
     */
    public BudgetSummaryDTO getBudgetSummary(Long budgetId, String email) {
        Budget budget = findBudgetByIdAndUser(budgetId, email);

        List<BudgetExpense> expenses = expenseRepository
                .findByBudgetIdAndBudgetUserId(budgetId, budget.getUser().getId());

        BigDecimal totalSpent = expenses.stream()
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal uncategorizedSpent = expenses.stream()
                .filter(e -> e.getBudgetCategory() == null)
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = budget.getTotalAmount().subtract(totalSpent);

        double spentPct = budget.getTotalAmount().compareTo(BigDecimal.ZERO) == 0
                ? 0
                : totalSpent.divide(budget.getTotalAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        List<BudgetCategory> categories =
                categoryRepository.findByBudgetId(budgetId);

        List<CategorySummaryDTO> catSummaries = categories.stream().map(cat -> {
            BigDecimal spent     = cat.getSpentAmount() != null
                    ? cat.getSpentAmount() : BigDecimal.ZERO;
            BigDecimal remaining = cat.getAllocatedAmount().subtract(spent);
            double catPct = cat.getAllocatedAmount().compareTo(BigDecimal.ZERO) == 0
                    ? 0
                    : spent.divide(cat.getAllocatedAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            String status = catPct >= 100 ? "EXCEEDED"
                    : catPct >= cat.getAlertThreshold() ? "WARNING"
                    : "SAFE";

            return CategorySummaryDTO.builder()
                    .categoryId(cat.getId())
                    .categoryName(cat.getCategoryName())
                    .allocatedAmount(cat.getAllocatedAmount())
                    .spentAmount(spent)
                    .remainingAmount(remaining)
                    .spentPercentage(catPct)
                    .status(status)
                    .build();
        }).collect(Collectors.toList());

        long unreadAlerts = alertRepository
                .countByBudgetIdAndBudgetUserIdAndIsSeenFalse(
                        budgetId, budget.getUser().getId());

        return BudgetSummaryDTO.builder()
                .budgetId(budget.getId())
                .budgetName(budget.getName())
                .totalBudget(budget.getTotalAmount())
                .totalSpent(totalSpent)
                .uncategorizedSpent(uncategorizedSpent)
                .totalRemaining(totalRemaining)
                .spentPercentage(spentPct)
                .categorySummaries(catSummaries)
                .unreadAlerts(unreadAlerts)
                .build();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    /** Shared allocation validation used by both create and update paths. */
    private void validateCategoryAllocations(
            List<BudgetCategoryDTO> categories,
            BigDecimal totalAmount
    ) {
        if (categories == null || categories.isEmpty()) return;

        BigDecimal totalAllocated = categories.stream()
                .map(BudgetCategoryDTO::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAllocated.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException(
                    "Category allocations exceed total budget amount");
        }
    }

    /** Builds a new BudgetCategory with spentAmount initialised to ZERO. */
    private BudgetCategory buildCategory(Budget budget, BudgetCategoryDTO dto) {
        return BudgetCategory.builder()
                .budget(budget)
                .categoryName(dto.getCategoryName())
                .allocatedAmount(dto.getAllocatedAmount())
                .alertThreshold(dto.getAlertThreshold() != null
                        ? dto.getAlertThreshold() : 80)
                .spentAmount(BigDecimal.ZERO)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BudgetSummaryDTO> getAllBudgetSummaries(String email) {
        User user = getAuthenticatedUser(email);
        List<Budget> budgets = budgetRepository.findByUserIdWithCategories(user.getId());
        return budgets.stream()
                .map(b -> getBudgetSummary(b.getId(), email))
                .collect(Collectors.toList());
    }
}