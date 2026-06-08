package com.rishanth.flux360.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;


@Service
public class SilverPriceService {

    private final ApiConfigService apiConfig;

    @Value("${silver.api.url}")
    private String silverApiUrl;

    @Value("${silver.fallback.url}")
    private String silverFallbackUrl;

    @Value("${goldapi.key}")
    private String goldApiKey;

    // ── RestTemplate with generous timeouts ───────────────────────────────────
    private final RestTemplate restTemplate;

    public SilverPriceService(ApiConfigService apiConfig) {
        this.apiConfig = apiConfig;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(12_000);
        factory.setReadTimeout(12_000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Cacheable("silverPrice")
    public BigDecimal getLiveSilverPricePerGram() {

        BigDecimal result = fetchFromGoldApiInr();

        if (result == null) {
            System.out.println("⚠️ Primary failed, trying fallback...");
            result = fetchFallbackPrice();
        }

        if (result != null) {
            return result;
        }

        System.out.println("❌ No cache and all APIs failed — returning hardcoded approximate");
        return new BigDecimal("95.00");
    }

    private BigDecimal fetchFromGoldApiInr() {
        try {
            String url = silverApiUrl;
            String apiKey = goldApiKey;

            if (apiKey.isBlank()) {
                System.out.println("❌ goldapi.key is empty — skipping silver primary fetch");
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ goldapi.io XAG/INR: unexpected response: " + body);
                return null;
            }

            BigDecimal priceInrPerOz = new BigDecimal(body.get("price").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal correctionFactor = apiConfig.getBigDecimal(
                    "SILVER_INDIA_CORRECTION", new BigDecimal("1.0766"));

            BigDecimal adjusted = priceInrPerOz
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(correctionFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("✅ Silver (goldapi.io): ₹" + priceInrPerOz + "/oz → ₹" + adjusted + "/g (factor: " + correctionFactor + ")");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ goldapi.io XAG/INR fetch failed: " + e.getMessage());
            return null;
        }
    }

    private BigDecimal fetchFallbackPrice() {
        try {
            String url = silverFallbackUrl;
            Map body   = restTemplate.getForObject(url, Map.class);

            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ Fallback gold-api.com (silver): unexpected response");
                return null;
            }

            BigDecimal priceUsdPerOz    = new BigDecimal(body.get("price").toString());
            BigDecimal usdToInr         = apiConfig.getBigDecimal("USD_TO_INR_APPROX", new BigDecimal("84.50"));
            BigDecimal gramsPerOz       = new BigDecimal("31.1035");
            BigDecimal correctionFactor = apiConfig.getBigDecimal("SILVER_INDIA_CORRECTION", new BigDecimal("1.0766"));

            BigDecimal adjusted = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(correctionFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("⚠️ Fallback silver (gold-api.com): ₹" + adjusted + "/g");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ Fallback silver (gold-api.com) failed: " + e.getMessage());
            return null;
        }
    }

    @CacheEvict(value = "silverPrice", allEntries = true)
    public void evictCache() {
        System.out.println("🔄 Silver price cache evicted by admin");
    }
}