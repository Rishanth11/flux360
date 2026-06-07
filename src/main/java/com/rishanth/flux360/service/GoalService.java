package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.GoalDTO;
import com.rishanth.flux360.entity.*;
import com.rishanth.flux360.repository.GoalContributionRepository;
import com.rishanth.flux360.repository.GoalRepository;
import com.rishanth.flux360.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final PiggyBankService piggyBankService; // FIX: injected for salary deduction

    // ───────────────── USER ─────────────────

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ───────────────── GOALS ─────────────────

    @Transactional
    public GoalDTO.Response createGoal(GoalDTO.Request request, String email) {
        User user = getAuthenticatedUser(email);

        Goal goal = Goal.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .targetAmount(request.getTargetAmount())
                .monthlyContribution(request.getMonthlyContribution())
                .targetDate(request.getTargetDate())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .linkedAccount(request.getLinkedAccount())
                .status(GoalStatus.ACTIVE)
                .savedAmount(BigDecimal.ZERO)
                .user(user)
                .build();

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalDTO.Response updateGoal(Long id, GoalDTO.Request request, String email) {
        Goal goal = findGoalById(id, email);

        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setCategory(request.getCategory());
        goal.setPriority(request.getPriority());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setMonthlyContribution(request.getMonthlyContribution());
        goal.setTargetDate(request.getTargetDate());
        goal.setLinkedAccount(request.getLinkedAccount());

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalDTO.Response updateStatus(Long id, GoalStatus status, String email) {
        Goal goal = findGoalById(id, email);
        goal.setStatus(status);
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long id, String email) {
        Goal goal = findGoalById(id, email);
        goalRepository.delete(goal);
    }

    public GoalDTO.Response getGoalById(Long id, String email) {
        return toResponse(findGoalById(id, email));
    }

    public List<GoalDTO.Response> getAllGoals(String email) {
        User user = getAuthenticatedUser(email);
        return goalRepository
                .findByUserIdOrderByPriorityAscTargetDateAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<GoalDTO.Response> getGoalsByStatus(GoalStatus status, String email) {
        User user = getAuthenticatedUser(email);
        return goalRepository
                .findByUserIdAndStatus(user.getId(), status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ───────────────── CONTRIBUTIONS ─────────────────

    @Transactional
    public GoalDTO.ContributionResponse addContribution(
            GoalDTO.ContributionRequest request,
            String email
    ) {
        Goal goal = findGoalById(request.getGoalId(), email);
        User user = goal.getUser();

        // FIX: deduct from remaining salary via piggy bank deposit
        piggyBankService.deposit(
                request.getAmount(),
                "Goal contribution: " + goal.getName(),
                user
        );

        GoalContribution contribution = GoalContribution.builder()
                .goal(goal)
                .amount(request.getAmount())
                .contributionDate(
                        request.getContributionDate() != null
                                ? request.getContributionDate()
                                : LocalDate.now()
                )
                .note(request.getNote())
                .build();

        contributionRepository.save(contribution);

        BigDecimal newSaved = goal.getSavedAmount().add(request.getAmount());
        goal.setSavedAmount(newSaved);

        if (newSaved.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        goalRepository.save(goal);

        return toContributionResponse(contribution);
    }

    @Transactional
    public void deleteContribution(Long contributionId, String email) {
        User user = getAuthenticatedUser(email);

        GoalContribution contribution = contributionRepository
                .findByIdAndGoalUserId(contributionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Contribution not found"));

        Goal goal = contribution.getGoal();

        // FIX: reverse the piggy bank deposit to restore remaining salary
        piggyBankService.withdraw(
                contribution.getAmount(),
                "Goal contribution reversed: " + goal.getName(),
                user
        );

        goal.setSavedAmount(
                goal.getSavedAmount()
                        .subtract(contribution.getAmount())
                        .max(BigDecimal.ZERO)
        );

        if (goal.getStatus() == GoalStatus.COMPLETED
                && goal.getSavedAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(GoalStatus.ACTIVE);
        }

        goalRepository.save(goal);
        contributionRepository.delete(contribution);
    }

    public List<GoalDTO.ContributionResponse> getContributionsByGoal(Long goalId, String email) {
        findGoalById(goalId, email);
        return contributionRepository
                .findByGoalIdOrderByContributionDateDesc(goalId)
                .stream()
                .map(this::toContributionResponse)
                .collect(Collectors.toList());
    }

    // ───────────────── SUMMARY ─────────────────

    public GoalDTO.Summary getSummary(String email) {
        User user = getAuthenticatedUser(email);
        List<Goal> all = goalRepository.findByUserId(user.getId());

        long active    = all.stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).count();
        long completed = all.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();

        BigDecimal totalTarget = all.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .map(Goal::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaved = all.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .map(Goal::getSavedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalTarget.subtract(totalSaved).max(BigDecimal.ZERO);

        double overallProgress = totalTarget.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : totalSaved.multiply(BigDecimal.valueOf(100))
                .divide(totalTarget, 2, RoundingMode.HALF_UP)
                .doubleValue();

        return GoalDTO.Summary.builder()
                .totalGoals(all.size())
                .activeGoals(active)
                .completedGoals(completed)
                .totalTargetAmount(totalTarget)
                .totalSavedAmount(totalSaved)
                .totalRemainingAmount(totalRemaining)
                .overallProgress(overallProgress)
                .build();
    }

    // ───────────────── MAPPERS ─────────────────

    private GoalDTO.Response toResponse(Goal goal) {
        LocalDate today = LocalDate.now();

        long monthsRemaining = ChronoUnit.MONTHS.between(today, goal.getTargetDate());
        if (monthsRemaining < 0) monthsRemaining = 0;

        LocalDate projectedDate = null;
        boolean onTrack = false;
        BigDecimal remaining = goal.getRemainingAmount();

        if (goal.getMonthlyContribution() != null
                && goal.getMonthlyContribution().compareTo(BigDecimal.ZERO) > 0
                && remaining.compareTo(BigDecimal.ZERO) > 0) {

            long monthsNeeded = remaining
                    .divide(goal.getMonthlyContribution(), 0, RoundingMode.CEILING)
                    .longValue();

            projectedDate = today.plusMonths(monthsNeeded);
            onTrack = !projectedDate.isAfter(goal.getTargetDate());

        } else if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            projectedDate = today;
            onTrack = true;
        }

        return GoalDTO.Response.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .category(goal.getCategory())
                .priority(goal.getPriority())
                .status(goal.getStatus())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .remainingAmount(remaining)
                .monthlyContribution(goal.getMonthlyContribution())
                .progressPercentage(goal.getProgressPercentage())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .projectedCompletionDate(projectedDate)
                .linkedAccount(goal.getLinkedAccount())
                .monthsRemaining(monthsRemaining)
                .onTrack(onTrack)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    private GoalDTO.ContributionResponse toContributionResponse(GoalContribution c) {
        return GoalDTO.ContributionResponse.builder()
                .id(c.getId())
                .goalId(c.getGoal().getId())
                .goalName(c.getGoal().getName())
                .amount(c.getAmount())
                .contributionDate(c.getContributionDate())
                .note(c.getNote())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private Goal findGoalById(Long id, String email) {
        User user = getAuthenticatedUser(email);
        return goalRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));
    }
}