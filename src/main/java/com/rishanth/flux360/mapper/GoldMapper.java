package com.rishanth.flux360.mapper;

import com.rishanth.flux360.dto.GoldDTO;
import com.rishanth.flux360.entity.Gold;

public class GoldMapper {

    private GoldMapper() {}

    public static GoldDTO toDTO(Gold gold) {

        GoldDTO dto = new GoldDTO();

        dto.setId(gold.getId());
        dto.setGramsPurchased(gold.getGramsPurchased());
        dto.setPurchasePricePerGram(gold.getPurchasePricePerGram());
        dto.setPurchaseDate(gold.getPurchaseDate());

        return dto;
    }
}