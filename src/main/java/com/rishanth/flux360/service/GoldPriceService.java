package com.rishanth.flux360.service;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class GoldPriceService {

    private final ApiConfigService apiConfig;

    @Value("${gold.api.url}")
    private String goldApiUrl;

    @Value("${gold.fallback.url}")
    private String goldFallbackUrl;

    @Value("${goldapi.key}")
    private String goldApiKey;
    // ─────────────────────────────────────────────
    // HTTP CLIENT
    // ─────────────────────────────────────────────

    private final RestTemplate restTemplate;

    public GoldPriceService(ApiConfigService apiConfig) {

        this.apiConfig = apiConfig;

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(12_000);
        factory.setReadTimeout(12_000);

        this.restTemplate = new RestTemplate(factory);
    }

    // ─────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────



    @Cacheable("goldPrice")
    public BigDecimal getLiveGoldPricePerGram() {

        BigDecimal result = fetchFromGoldApiInr();

        if (result == null) {

            log.warn(
                    "Primary gold API failed. Trying fallback API..."
            );

            result = fetchFallbackPrice();
        }

        if (result != null) {
            return result;
        }

        log.error(
                "No cache and all APIs failed. Returning hardcoded fallback price."
        );

        return new BigDecimal("14000.00");
    }

    // ─────────────────────────────────────────────
    // PRIMARY API
    // ─────────────────────────────────────────────

    private BigDecimal fetchFromGoldApiInr() {

        try {

            String url = goldApiUrl;

            String apiKey = goldApiKey;

            if (apiKey.isBlank()) {

                log.error(
                        "goldapi.key is empty. Skipping primary gold fetch."
                );

                return null;
            }

            HttpHeaders headers = new HttpHeaders();

            headers.set("x-access-token", apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> request =
                    new HttpEntity<>(headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            request,
                            Map.class
                    );

            Map body = response.getBody();

            if (body == null ||
                    !body.containsKey("price")) {

                log.error(
                        "Unexpected response from goldapi.io: {}",
                        body
                );

                return null;
            }

            BigDecimal priceInrPerOz =
                    new BigDecimal(
                            body.get("price").toString()
                    );

            BigDecimal gramsPerOz =
                    new BigDecimal("31.1035");

            BigDecimal correctionFactor =
                    apiConfig.getBigDecimal(
                            "GOLD_INDIA_CORRECTION",
                            new BigDecimal("1.0433")
                    );

            BigDecimal adjusted =
                    priceInrPerOz
                            .divide(
                                    gramsPerOz,
                                    4,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(correctionFactor)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            log.info(
                    "Gold price fetched from goldapi.io: ₹{}/g",
                    adjusted
            );

            return adjusted;

        } catch (Exception e) {

            log.error(
                    "goldapi.io fetch failed: {}",
                    e.getMessage()
            );

            return null;
        }
    }

    // ─────────────────────────────────────────────
    // FALLBACK API
    // ─────────────────────────────────────────────

    private BigDecimal fetchFallbackPrice() {

        try {

            String url = goldFallbackUrl;

            Map body =
                    restTemplate.getForObject(
                            url,
                            Map.class
                    );

            if (body == null ||
                    !body.containsKey("price")) {

                log.error(
                        "Unexpected fallback API response."
                );

                return null;
            }

            BigDecimal priceUsdPerOz =
                    new BigDecimal(
                            body.get("price").toString()
                    );

            BigDecimal usdToInr =
                    apiConfig.getBigDecimal(
                            "USD_TO_INR_APPROX",
                            new BigDecimal("84.50")
                    );

            BigDecimal gramsPerOz =
                    new BigDecimal("31.1035");

            BigDecimal correctionFactor =
                    apiConfig.getBigDecimal(
                            "GOLD_INDIA_CORRECTION",
                            new BigDecimal("1.0433")
                    );

            BigDecimal adjusted =
                    priceUsdPerOz
                            .multiply(usdToInr)
                            .divide(
                                    gramsPerOz,
                                    4,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(correctionFactor)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            log.warn(
                    "Fallback gold API used. Price: ₹{}/g",
                    adjusted
            );

            return adjusted;

        } catch (Exception e) {

            log.error(
                    "Fallback gold API failed: {}",
                    e.getMessage()
            );

            return null;
        }
    }

    // ─────────────────────────────────────────────
    // CACHE MANAGEMENT
    // ─────────────────────────────────────────────

    @CacheEvict(value = "goldPrice", allEntries = true)
    public void evictCache() {

        log.info("Gold cache evicted.");
    }
}