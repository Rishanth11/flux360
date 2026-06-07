package com.rishanth.flux360.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "budget_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    private String categoryName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedAmount;

    // @Builder.Default ensures Lombok's builder sets ZERO rather than null.
    // Without this the initialiser is ignored and spentAmount.add() throws NPE.
    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Integer alertThreshold = 80;
}