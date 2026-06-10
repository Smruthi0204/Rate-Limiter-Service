package com.ratelimiter.rate_limiter_service.Controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ratelimiter.rate_limiter_service.DTO.ClientRequest;
import com.ratelimiter.rate_limiter_service.DTO.MetricsResponse;
import com.ratelimiter.rate_limiter_service.DTO.RateLimitResponse;
import com.ratelimiter.rate_limiter_service.Service.RateLimiterService;

import lombok.RequiredArgsConstructor;




@RestController
@RequiredArgsConstructor

public class RateLimiterController {
    private final RateLimiterService servObj;

    @PostMapping("/api/clients/register")
    public ResponseEntity<String> registerClient(@RequestBody ClientRequest request) {
       return ResponseEntity.created(null).body(servObj.register(request));
    }

   @PostMapping("/api/clients/{clientId}/check")
    public ResponseEntity<RateLimitResponse> checkEligibility(@PathVariable String clientId) {
        RateLimitResponse response = servObj.check(clientId);
        if (response.isAllowed()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(429).body(response);
        }
    }

    @GetMapping("/api/clients/details")
    public ResponseEntity<Set<String>> getAllClients() {
        return ResponseEntity.ok(servObj.getAllClients());
    }

    @GetMapping("/api/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        return ResponseEntity.ok(servObj.getMetrics());
    }
    
    
}
