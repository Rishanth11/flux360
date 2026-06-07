package com.rishanth.flux360.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "gold",
        indexes = {
                @Index(name = "idx_gold_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal gramsPurchased;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal purchasePricePerGram;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInvested;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}