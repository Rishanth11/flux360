package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.SilverDTO;
import com.rishanth.flux360.dto.SilverSummaryDTO;

public interface SilverService {

    SilverDTO addInvestment(
            String username,
            SilverDTO dto
    );

    SilverDTO updateInvestment(
            Long id,
            SilverDTO dto,
            String username
    );

    void deleteInvestment(
            Long id,
            String username
    );

    SilverSummaryDTO getPortfolioSummary(
            String username
    );
}