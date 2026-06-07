package com.rishanth.flux360.mapper;

import com.rishanth.flux360.dto.SilverDTO;
import com.rishanth.flux360.entity.Silver;

public class SilverMapper {

    private SilverMapper() {}

    public static SilverDTO toDTO(
            Silver investment
    ) {

        return new SilverDTO(
                investment.getId(),
                investment.getGrams(),
                investment.getPricePerGram(),
                investment.getPurchaseDate()
        );
    }
}