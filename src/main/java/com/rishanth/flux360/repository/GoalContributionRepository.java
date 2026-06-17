package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.GoalContribution;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalContributionRepository
        extends JpaRepository<GoalContribution, Long> {

    @EntityGraph(attributePaths = {"goal"})
    List<GoalContribution>
    findByGoalIdOrderByContributionDateDesc(Long goalId);

    @EntityGraph(attributePaths = {"goal"})
    Optional<GoalContribution>
    findByIdAndGoalUserId(Long contributionId,
                          Long userId);
}