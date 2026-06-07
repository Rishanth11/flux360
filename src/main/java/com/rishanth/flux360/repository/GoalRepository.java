package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.Goal;
import com.rishanth.flux360.entity.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserIdOrderByPriorityAscTargetDateAsc(Long userId);

    List<Goal> findByUserIdAndStatus(Long userId,
                                     GoalStatus status);

    Optional<Goal> findByIdAndUserId(Long goalId,
                                     Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId,
                                GoalStatus status);

    List<Goal> findByUserId(Long userId);
}