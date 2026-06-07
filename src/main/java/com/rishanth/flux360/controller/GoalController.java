package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.GoalDTO;
import com.rishanth.flux360.entity.GoalStatus;
import com.rishanth.flux360.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('USER')")
public class GoalController {

    private final GoalService goalService;

    // ───────────────── GOALS ─────────────────

    @PostMapping
    public ResponseEntity<GoalDTO.Response> createGoal(
            @Valid @RequestBody GoalDTO.Request request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.createGoal(
                        request,
                        authentication.getName()
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<GoalDTO.Response>> getAllGoals(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.getAllGoals(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDTO.Response> getGoalById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.getGoalById(
                        id,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<GoalDTO.Response>> getGoalsByStatus(
            @PathVariable GoalStatus status,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.getGoalsByStatus(
                        status,
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDTO.Response> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalDTO.Request request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.updateGoal(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<GoalDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestParam GoalStatus status,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.updateStatus(
                        id,
                        status,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long id,
            Authentication authentication
    ) {

        goalService.deleteGoal(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // ───────────────── CONTRIBUTIONS ─────────────────

    @PostMapping("/contributions")
    public ResponseEntity<GoalDTO.ContributionResponse>
    addContribution(
            @Valid @RequestBody GoalDTO.ContributionRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.addContribution(
                        request,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{goalId}/contributions")
    public ResponseEntity<List<GoalDTO.ContributionResponse>>
    getContributions(
            @PathVariable Long goalId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.getContributionsByGoal(
                        goalId,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/contributions/{contributionId}")
    public ResponseEntity<Void> deleteContribution(
            @PathVariable Long contributionId,
            Authentication authentication
    ) {

        goalService.deleteContribution(
                contributionId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // ───────────────── SUMMARY ─────────────────

    @GetMapping("/summary")
    public ResponseEntity<GoalDTO.Summary> getSummary(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                goalService.getSummary(
                        authentication.getName()
                )
        );
    }
}