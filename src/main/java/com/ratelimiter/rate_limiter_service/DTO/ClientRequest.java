package com.ratelimiter.rate_limiter_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientRequest {
    String clientId;
    int capacity;
    int refillRate;

}
