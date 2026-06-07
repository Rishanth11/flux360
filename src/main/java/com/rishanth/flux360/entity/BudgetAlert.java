package com.rishanth.flux360.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "budget_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    private Long budgetCategoryId;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    private String message;

    private LocalDateTime triggeredAt;

    // @Builder.Default so Lombok's builder sets false instead of null.
    // @Column nullable=false enforces this at the DB level.
    @Builder.Default
    @Column(nullable = false)
    private Boolean isSeen = false;

    @PrePersist
    protected void onCreate() {
        triggeredAt = LocalDateTime.now();
    }

    public enum AlertType {
        WARNING, EXCEEDED
    }
}