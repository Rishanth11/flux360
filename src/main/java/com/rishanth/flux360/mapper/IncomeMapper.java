package com.rishanth.flux360.mapper;

import com.rishanth.flux360.dto.IncomeDTO;
import com.rishanth.flux360.entity.Income;

public class IncomeMapper {

    public static IncomeDTO toDTO(Income income) {

        return new IncomeDTO(
                income.getId(),
                income.getSource(),
                income.getAmount(),
                income.getCategory().name(),
                income.getDescription(),
                income.getDate()
        );
    }
}