package com.rishanth.flux360.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "sip_investments",
        indexes = {
                @Index(
                        name = "idx_sip_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_sip_active",
                        columnList = "active"
                )
        }
)
public class SipInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(nullable = false)
    private String fundName;

    @Column(nullable = false)
    private String fundCode;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal monthlyAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private int sipDay;

    @Column(nullable = false)
    private boolean active = true;

    @Column(
            precision = 5,
            scale = 2
    )
    private BigDecimal inflationRate =
            BigDecimal.valueOf(6.0);

    private String goalName;

    @Column(
            precision = 19,
            scale = 4
    )
    private BigDecimal targetAmount;

    private LocalDate targetDate;
}