package com.rishanth.flux360.service;

import com.rishanth.flux360.config.StockIndexConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockProxyService {

    private final RestTemplate restTemplate;
    private final StockIndexConfig indexConfig;

    public Map<String, Object> getPrice(String symbol) {
        if (indexConfig.isIndex(symbol)) {
            return fetchWithFallback(indexConfig.getTickers(symbol), indexConfig.getMultiplier(symbol));
        }
        return fetchWithFallback(new String[]{symbol}, 1.0);
    }

    public Map<String, Object> getPrices(String[] symbols) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String symbol : symbols) {
            Map<String, Object> data = getPrice(symbol);
            result.put(symbol, data != null ? data : Map.of("error", "No data"));
        }
        return result;
    }

    // ── INTERNAL ──

    private Map<String, Object> fetchWithFallback(String[] tickers, double multiplier) {
        for (String ticker : tickers) {
            Map<String, Object> data = fetchFromYahoo(ticker, multiplier);
            if (data != null) return data;
        }
        return null;
    }

    private Map<String, Object> fetchFromYahoo(String ticker, double multiplier) {
        try {
            String url = "https://query2.finance.yahoo.com/v8/finance/chart/"
                    + ticker + "?interval=1d&range=1d";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    Map.class
            );

            return extractPriceData(response.getBody(), multiplier);

        } catch (Exception e) {
            System.out.println("Yahoo fetch failed for: " + ticker);
            return null;
        }
    }

    private Map<String, Object> extractPriceData(Map<?, ?> body, double multiplier) {
        try {
            if (body == null) return null;

            if (!(body.get("chart") instanceof Map<?, ?> chart)) return null;
            if (chart.get("error") != null) return null;
            if (!(chart.get("result") instanceof List<?> resultList)) return null;
            if (resultList.isEmpty()) return null;
            if (!(resultList.get(0) instanceof Map<?, ?> first)) return null;
            if (!(first.get("meta") instanceof Map<?, ?> meta)) return null;

            double rawPrice = toDouble(meta.get("regularMarketPrice"));
            if (rawPrice == 0) rawPrice = toDouble(meta.get("previousClose"));
            if (rawPrice == 0) return null;

            double rawPrev = toDouble(meta.get("previousClose"));
            if (rawPrev == 0) rawPrev = toDouble(meta.get("chartPreviousClose"));
            if (rawPrev == 0) rawPrev = rawPrice;

            double price     = rawPrice * multiplier;
            double prevClose = rawPrev  * multiplier;
            double change    = price - prevClose;
            double changePct = prevClose > 0 ? (change / prevClose) * 100 : 0;

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("price",     round2(price));
            out.put("change",    round2(change));
            out.put("changePct", round2(changePct));
            out.put("prevClose", round2(prevClose));
            return out;

        } catch (Exception e) {
            return null;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.USER_AGENT,       "Mozilla/5.0");
        h.set(HttpHeaders.ACCEPT,           "application/json");
        h.set(HttpHeaders.ACCEPT_LANGUAGE,  "en-US,en;q=0.9");
        h.set(HttpHeaders.CACHE_CONTROL,    "no-cache");
        h.set(HttpHeaders.CONNECTION,       "keep-alive");
        h.set("Referer",                    "https://finance.yahoo.com/");
        return h;
    }

    private double toDouble(Object value) {
        if (value == null) return 0;
        try { return Double.parseDouble(value.toString()); }
        catch (Exception e) { return 0; }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}