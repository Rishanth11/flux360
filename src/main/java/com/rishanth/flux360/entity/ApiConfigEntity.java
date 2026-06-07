package com.rishanth.flux360.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores runtime API keys, URLs, cache settings,
 * correction factors, and external integration configs.
 */
@Entity
@Table(
        name = "api_config",
        indexes = {
                @Index(
                        name = "idx_api_config_key",
                        columnList = "config_key"
                ),
                @Index(
                        name = "idx_api_config_category",
                        columnList = "category"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique config key.
     *
     * Examples:
     * GOLD_API_KEY
     * GOLD_API_URL
     * MFAPI_BASE_URL
     */
    @Column(
            name = "config_key",
            nullable = false,
            unique = true,
            length = 100
    )
    private String configKey;

    /**
     * Actual config value.
     *
     * API keys, URLs, TTL values, correction factors, etc.
     */
    @Column(
            name = "config_value",
            nullable = false,
            length = 500
    )
    private String configValue;

    /**
     * Admin-friendly description.
     */
    @Column(
            name = "description",
            length = 300
    )
    private String description;

    /**
     * Grouping category.
     *
     * Examples:
     * GOLD
     * SILVER
     * SIP
     * CACHE
     * KEYS
     */
    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private String category;

    /**
     * Whether value should be masked in admin UI.
     */
    @Column(name = "is_sensitive")
    private boolean sensitive = false;

    /**
     * Whether this config can be edited via admin UI.
     * Set false for system-critical keys that should never change at runtime.
     */
    @Column(nullable = false)
    private boolean editable = true;

    /**
     * When this config was first created.
     */
    @Column(
            name = "created_at",
            updatable = false
    )
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Admin/user who last updated the value.
     */
    @Column(
            name = "updated_by",
            length = 100
    )
    private String updatedBy;

    // ─────────────────────────────────────────────
    // AUTO TIMESTAMPS
    // ─────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}