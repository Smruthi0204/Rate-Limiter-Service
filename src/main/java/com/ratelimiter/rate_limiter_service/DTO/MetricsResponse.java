package com.ratelimiter.rate_limiter_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsResponse {
    Long totalRequests;
    Long allowedRequests;
    Long blockedRequests;
    int activeClients;
}
