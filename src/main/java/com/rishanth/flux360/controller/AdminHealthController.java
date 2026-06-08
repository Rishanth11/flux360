package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.ApiHealthDTO;
import com.rishanth.flux360.service.AdminHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminHealthController {

    private final AdminHealthService adminHealthService;

    @GetMapping("/health")
    public List<ApiHealthDTO> getHealth() {
        return adminHealthService.getHealthStatus();
    }
}