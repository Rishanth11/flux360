package com.rishanth.flux360.service;

import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.entity.ApiConfigEntity;
import com.rishanth.flux360.repository.ApiConfigRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiConfigService {

    private final ApiConfigRepository repo;

    // ─────────────────────────────────────────────
    // ENVIRONMENT FALLBACKS
    // ─────────────────────────────────────────────

    @Value("${goldapi.key:}")
    private String envGoldApiKey;

    /**
     * Map-based env fallback — avoids manual switch maintenance.
     * Populated after @Value injection via @PostConstruct.
     */
    private Map<String, String> envFallbackMap;

    // ─────────────────────────────────────────────
    // INITIAL DEFAULT SEEDING
    // ─────────────────────────────────────────────

    @PostConstruct
    public void seedDefaults() {

        // Build env fallback map after @Value fields are injected
        envFallbackMap = Map.of(
                "GOLD_API_KEY",       envGoldApiKey
        );

        seed(
                "GOLD_CACHE_TTL_MINUTES",
                "15",
                "Gold cache TTL",
                "CACHE",
                false,
                true
        );

        seed(
                "SILVER_CACHE_TTL_MINUTES",
                "15",
                "Silver cache TTL",
                "CACHE",
                false,
                true
        );

        seed(
                "GOLD_INDIA_CORRECTION",
                "1.0433",
                "India gold correction factor",
                "CORRECTION",
                false,
                true
        );

        seed(
                "SILVER_INDIA_CORRECTION",
                "1.0766",
                "India silver correction factor",
                "CORRECTION",
                false,
                true
        );

        seed(
                "USD_TO_INR_APPROX",
                "84.50",
                "Approx USD to INR conversion",
                "CORRECTION",
                false,
                true
        );

    }

    // ─────────────────────────────────────────────
    // INTERNAL SEED METHOD
    // ─────────────────────────────────────────────

    private void seed(
            String key,
            String value,
            String description,
            String category,
            boolean sensitive,
            boolean editable
    ) {

        if (!repo.existsByConfigKey(key)) {

            repo.save(
                    ApiConfigEntity.builder()
                            .configKey(key)
                            .configValue(value)
                            .description(description)
                            .category(category)
                            .sensitive(sensitive)
                            .editable(editable)
                            .build()
            );

            log.info("Seeded config: {}", key);
        }
    }

    // ─────────────────────────────────────────────
    // GET CONFIG
    // ─────────────────────────────────────────────

    /**
     * DB → ENV → EMPTY STRING
     * Result cached by key to avoid repeated DB hits.
     */
    @Cacheable(value = "apiConfig", key = "#key")
    public String get(String key) {

        Optional<ApiConfigEntity> optional =
                repo.findByConfigKey(key);

        if (optional.isPresent()) {

            String value = optional.get().getConfigValue();

            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return envFallback(key);
    }

    /**
     * DB → ENV → DEFAULT
     */
    public String get(String key, String defaultValue) {

        String value = get(key);

        return value.isBlank() ? defaultValue : value;
    }

    // ─────────────────────────────────────────────
    // TYPE SAFE ACCESSORS
    // ─────────────────────────────────────────────

    public BigDecimal getBigDecimal(
            String key,
            BigDecimal defaultValue
    ) {

        try {

            String value = get(key);

            return value.isBlank()
                    ? defaultValue
                    : new BigDecimal(value);

        } catch (Exception e) {

            log.warn("Invalid BigDecimal config for key: {}", key);

            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {

        try {

            String value = get(key);

            return value.isBlank()
                    ? defaultValue
                    : Integer.parseInt(value);

        } catch (Exception e) {

            log.warn("Invalid int config for key: {}", key);

            return defaultValue;
        }
    }

    // ─────────────────────────────────────────────
    // ENV FALLBACKS
    // ─────────────────────────────────────────────

    private String envFallback(String key) {
        return envFallbackMap.getOrDefault(key, "");
    }

    // ─────────────────────────────────────────────
    // ADMIN CRUD
    // ─────────────────────────────────────────────

    public List<ApiConfigEntity> getAll() {
        return repo.findAll();
    }

    public List<ApiConfigEntity> getByCategory(String category) {
        return repo.findByCategoryOrderByConfigKeyAsc(
                category.toUpperCase()
        );
    }

    /**
     * Update a config value.
     *
     * FIX 1: orElseThrow — unknown keys are rejected (no ghost CUSTOM entries).
     * FIX 2: editable check — non-editable keys cannot be changed at runtime.
     * FIX 3: Cache evicted so next get() reads fresh value from DB.
     */
    @Transactional
    @CacheEvict(value = "apiConfig", key = "#key")
    public ApiConfigEntity update(
            String key,
            String value,
            String updatedBy
    ) {

        ApiConfigEntity entity = repo.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Config key not found: " + key
                ));

        if (!entity.isEditable()) {
            throw new IllegalStateException(
                    "Config key is not editable: " + key
            );
        }

        entity.setConfigValue(value == null ? "" : value.trim());
        entity.setUpdatedBy(updatedBy);

        ApiConfigEntity saved = repo.save(entity);

        log.info("ApiConfig updated: {} by {}", key, updatedBy);

        return saved;
    }

    @Transactional
    public void updateBulk(
            Map<String, String> updates,
            String updatedBy
    ) {

        updates.forEach((key, value) -> update(key, value, updatedBy));

        log.info("Bulk ApiConfig update completed by {}", updatedBy);
    }

    public ApiConfigEntity getEntityByKey(String key) {

        return repo.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Config key not found: " + key
                ));
    }
}