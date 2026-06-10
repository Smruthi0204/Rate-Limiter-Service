package com.ratelimiter.rate_limiter_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitResponse {
    private boolean allowed;
    private String clientId;        
    private int remainingTokens;    
    private String message;         
}
