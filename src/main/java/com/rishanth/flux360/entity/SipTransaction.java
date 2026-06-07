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
        name = "sip_transactions",
        indexes = {
                @Index(
                        name = "idx_sip_txn_sip",
                        columnList = "sip_id"
                ),
                @Index(
                        name = "idx_sip_txn_date",
                        columnList = "investDate"
                )
        }
)
public class SipTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sip_id",
            nullable = false
    )
    private SipInvestment sip;

    @Column(nullable = false)
    private LocalDate investDate;

    @Column(
            nullable = false,
            precision = 19,
            scale = 6
    )
    private BigDecimal nav;

    @Column(
            nullable = false,
            precision = 19,
            scale = 6
    )
    private BigDecimal units;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;
}