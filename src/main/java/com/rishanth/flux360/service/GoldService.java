package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.GoldDTO;
import com.rishanth.flux360.dto.GoldHistoryDTO;
import com.rishanth.flux360.dto.GoldSummaryDTO;

import java.util.List;

public interface GoldService {

    GoldDTO addGold(GoldDTO dto, String email);

    GoldDTO updateGold(Long id, GoldDTO dto, String email);

    void deleteGold(Long id, String email);

    GoldSummaryDTO getSummary(String email);

    GoldSummaryDTO getFilteredSummary(
            String email,
            int year,
            int month
    );

    List<GoldHistoryDTO> getAllGold(String email);

    List<GoldHistoryDTO> getFilteredHistory(
            String email,
            int year,
            int month
    );
}