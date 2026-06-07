package com.rishanth.flux360.service;

import org.springframework.beans.factory.annotation.Value;
import com.rishanth.flux360.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NavService {

    @Value("${mfapi.base.url}")
    private String mfApiBaseUrl;

    private final RestTemplate restTemplate;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    public NavService() {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(15_000);
        this.restTemplate = new RestTemplate(factory);
    }

    // ─────────────────────────────────────────────
    // FETCH LATEST NAV (with retry)
    // ─────────────────────────────────────────────

    public BigDecimal fetchLatestNav(String fundCode) {

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            try {
                String url = mfApiBaseUrl + "/" + fundCode;

                log.info("Fetching NAV for fund: {} (attempt {}/{})",
                        fundCode, attempt, MAX_RETRIES);

                Map<String, Object> response =
                        restTemplate.getForObject(url, Map.class);

                if (response == null || !response.containsKey("data"))
                    throw new ResourceNotFoundException("NAV response invalid");

                List<Map<String, Object>> data =
                        (List<Map<String, Object>>) response.get("data");

                if (data == null || data.isEmpty())
                    throw new ResourceNotFoundException("NAV data not found");

                Object navValue = data.get(0).get("nav");

                if (navValue == null)
                    throw new ResourceNotFoundException("NAV value missing");

                BigDecimal nav = new BigDecimal(navValue.toString());

                log.info("NAV fetched for {}: {}", fundCode, nav);

                return nav;

            } catch (ResourceNotFoundException e) {
                // data issue — no point retrying
                throw new RuntimeException(
                        "NAV data unavailable for fund: " + fundCode, e);

            } catch (Exception e) {
                lastException = e;
                log.warn("NAV fetch attempt {}/{} failed for {}: {}",
                        attempt, MAX_RETRIES, fundCode, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("All {} attempts failed for fund {}", MAX_RETRIES, fundCode);
        throw new RuntimeException(
                "Failed to fetch NAV for fund: " + fundCode, lastException);
    }
}