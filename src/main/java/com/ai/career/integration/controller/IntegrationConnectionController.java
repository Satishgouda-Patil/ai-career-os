package com.ai.career.integration.controller;

import com.ai.career.common.dto.ApiResponse;
import com.ai.career.integration.domain.entity.IntegrationConnection;
import com.ai.career.integration.domain.repository.IntegrationConnectionRepository;
import com.ai.career.security.UserPrincipal;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class IntegrationConnectionController {

    private final IntegrationConnectionRepository connectionRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IntegrationConnectionDto>>> getConnections(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<IntegrationConnectionDto> list = connectionRepository.findByUserId(userPrincipal.getId()).stream()
                .map(c -> IntegrationConnectionDto.builder()
                        .id(c.getId())
                        .provider(c.getProvider())
                        .status(c.getStatus())
                        .externalAccountId(c.getExternalAccountId())
                        .updatedAt(c.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(list, "Integration connections retrieved successfully"));
    }

    @Data
    @Builder
    public static class IntegrationConnectionDto {
        private Long id;
        private String provider;
        private String status;
        private String externalAccountId;
        private LocalDateTime updatedAt;
    }
}
