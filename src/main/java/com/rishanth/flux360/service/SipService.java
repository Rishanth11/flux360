package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.SipListDTO;
import com.rishanth.flux360.dto.SipPortfolioDTO;
import com.rishanth.flux360.dto.SipRequestDTO;
import com.rishanth.flux360.dto.SipTransactionDTO;
import com.rishanth.flux360.entity.User;

import java.util.List;
import java.util.Map;

public interface SipService {

    void createSip(
            SipRequestDTO dto,
            String email
    );

    // OVERALL PORTFOLIO
    SipPortfolioDTO getPortfolio(
            Long sipId,
            User user
    );


    List<SipTransactionDTO> getTransactions(
            Long sipId,
            String email
    );

    void executeSipNow(
            Long sipId,
            User user
    );

    void stopSip(
            Long sipId,
            String email
    );

    List<SipListDTO> getAllSips(
            String email
    );

    // SIP CHART
    List<Map<String, Object>> getSipChart(
            Long sipId,
            User user
    );
}