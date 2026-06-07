package com.rishanth.flux360.config;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StockIndexConfig {

    private static final Map<String, String[]> ETF_FALLBACKS = new LinkedHashMap<>();
    private static final Map<String, Double> MULTIPLIERS = new LinkedHashMap<>();

    static {
        ETF_FALLBACKS.put("NIFTY50",   new String[]{"NIFTYBEES.NS"});
        ETF_FALLBACKS.put("SENSEX",    new String[]{"SENSEXIETF.NS"});
        ETF_FALLBACKS.put("BANKNIFTY", new String[]{"BANKBEES.NS"});
        ETF_FALLBACKS.put("NIFTYIT",   new String[]{"ITBEES.NS"});

        MULTIPLIERS.put("NIFTY50",   100.0);
        MULTIPLIERS.put("SENSEX",    100.0);
        MULTIPLIERS.put("BANKNIFTY", 100.0);
        MULTIPLIERS.put("NIFTYIT",   1000.0);
    }

    public boolean isIndex(String symbol) {
        return ETF_FALLBACKS.containsKey(symbol);
    }

    public String[] getTickers(String symbol) {
        return ETF_FALLBACKS.getOrDefault(symbol, new String[]{symbol});
    }

    public double getMultiplier(String symbol) {
        return MULTIPLIERS.getOrDefault(symbol, 1.0);
    }
}