package com.rishanth.flux360.controller;

import com.rishanth.flux360.service.StockNewsService;
import com.rishanth.flux360.service.StockProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stocks/proxy")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class StockPriceController {

    private final StockProxyService stockProxyService;
    private final StockNewsService  stockNewsService;

    @GetMapping("/price")
    public ResponseEntity<?> getPrice(@RequestParam String symbol) {
        try {
            Map<String, Object> data = stockProxyService.getPrice(symbol);
            if (data == null)
                return ResponseEntity.status(502).body(Map.of("error", "No data for " + symbol));
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/prices")
    public ResponseEntity<?> getPrices(@RequestParam String symbols) {
        try {
            String[] arr = symbols.split(",");
            return ResponseEntity.ok(stockProxyService.getPrices(arr));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/news")
    public ResponseEntity<?> getNews() {
        try {
            return ResponseEntity.ok(Map.of("items", stockNewsService.fetchMarketNews()));
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "Failed to fetch news"));
        }
    }
}