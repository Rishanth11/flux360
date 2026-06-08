package com.rishanth.flux360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiHealthDTO {

    private String name;
    private String status;
    private String message;
}