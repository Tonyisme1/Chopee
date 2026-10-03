package com.chopee.common.controller;

import com.chopee.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Kiểm tra trạng thái hoạt động của hệ thống Chopee API")
public class HealthController {

    @GetMapping
    @Operation(summary = "Kiểm tra tình trạng máy chủ")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        return ResponseEntity.ok(ApiResponse.success("Chopee API Server đang hoạt động ổn định", Map.of(
                "status", "UP",
                "version", "v1.0.0",
                "timestamp", System.currentTimeMillis()
        )));
    }
}

