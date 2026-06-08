package com.rishanth.flux360.service;

import com.rishanth.flux360.entity.ApiConfigEntity;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import com.rishanth.flux360.repository.ApiConfigRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AuditLogService auditLogService;

    private static final List<String> ALLOWED_SETTINGS = List.of(
            "GOLD_CACHE_TTL_MINUTES",
            "SILVER_CACHE_TTL_MINUTES",
            "GOLD_INDIA_CORRECTION",
            "SILVER_INDIA_CORRECTION",
            "USD_TO_INR_APPROX"
    );

    // ─────────────────────────────────────────────
    // INITIAL DEFAULT SEEDING
    // ─────────────────────────────────────────────

    @PostConstruct
    public void seedDefaults() {

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

        return "";
    }

    public String get(String key, String defaultValue) {

        String value = get(key);

        return value.isBlank()
                ? defaultValue
                : value;
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
    // ADMIN CRUD
    // ─────────────────────────────────────────────

    public List<ApiConfigEntity> getAll() {

        return repo.findAll()
                .stream()
                .filter(config ->
                        ALLOWED_SETTINGS.contains(config.getConfigKey()))
                .toList();
    }

    public List<ApiConfigEntity> getByCategory(String category) {

        return repo.findByCategoryOrderByConfigKeyAsc(
                category.toUpperCase()
        );
    }

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

        if (!ALLOWED_SETTINGS.contains(key)) {
            throw new IllegalStateException(
                    "Setting cannot be modified from admin panel: " + key
            );
        }

        if (!entity.isEditable()) {
            throw new IllegalStateException(
                    "Config key is not editable: " + key
            );
        }

        String oldValue = entity.getConfigValue();

        entity.setConfigValue(
                value == null ? "" : value.trim()
        );

        entity.setUpdatedBy(updatedBy);

        ApiConfigEntity saved = repo.save(entity);

        auditLogService.log(
                "SYSTEM_SETTING_UPDATED",
                updatedBy,
                "Setting: " + key +
                        " | Old: " + oldValue +
                        " | New: " + saved.getConfigValue()
        );

        log.info(
                "System setting updated: {} by {}",
                key,
                updatedBy
        );

        return saved;
    }

    @Transactional
    public void updateBulk(
            Map<String, String> updates,
            String updatedBy
    ) {

        updates.forEach(
                (key, value) ->
                        update(key, value, updatedBy)
        );

        log.info(
                "Bulk system settings update completed by {}",
                updatedBy
        );
    }

    public ApiConfigEntity getEntityByKey(String key) {

        return repo.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Config key not found: " + key
                ));
    }
}