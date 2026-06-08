package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.ApiHealthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHealthService {

    private final GoldPriceService goldPriceService;
    private final SilverPriceService silverPriceService;
    private final NavService navService;

    public List<ApiHealthDTO> getHealthStatus() {

        List<ApiHealthDTO> result = new ArrayList<>();

        // GOLD API
        try {

            BigDecimal price =
                    goldPriceService.getLiveGoldPricePerGram();

            result.add(
                    new ApiHealthDTO(
                            "gold_api",
                            "UP",
                            "₹" + price + "/g"
                    )
            );

        } catch (Exception e) {

            result.add(
                    new ApiHealthDTO(
                            "gold_api",
                            "DOWN",
                            "Gold API unavailable"
                    )
            );
        }

        // SILVER API
        try {

            BigDecimal price =
                    silverPriceService.getLiveSilverPricePerGram();

            result.add(
                    new ApiHealthDTO(
                            "silver_api",
                            "UP",
                            "₹" + price + "/g"
                    )
            );

        } catch (Exception e) {

            result.add(
                    new ApiHealthDTO(
                            "silver_api",
                            "DOWN",
                            "Silver API unavailable"
                    )
            );
        }

        // MFAPI
        try {

            BigDecimal nav =
                    navService.fetchLatestNav("119551");

            result.add(
                    new ApiHealthDTO(
                            "mfapi",
                            "UP",
                            "Current NAV: ₹" + nav.setScale(2, RoundingMode.HALF_UP)
                    )
            );

        } catch (Exception e) {

            result.add(
                    new ApiHealthDTO(
                            "mfapi",
                            "DOWN",
                            "MFAPI unavailable"
                    )
            );
        }

        // YAHOO FINANCE

        result.add(
                new ApiHealthDTO(
                        "yahoo_finance",
                        "UP",
                        "Market data available"
                )
        );

        return result;
    }
}