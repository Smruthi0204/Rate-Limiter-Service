package com.ratelimiter.rate_limiter_service.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ratelimiter.rate_limiter_service.DTO.ClientRequest;
import com.ratelimiter.rate_limiter_service.DTO.MetricsResponse;
import com.ratelimiter.rate_limiter_service.DTO.RateLimitResponse;
import com.ratelimiter.rate_limiter_service.Entity.Client;
import com.ratelimiter.rate_limiter_service.Repo.ClientRepo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor

public class RateLimiterService {
    private final ClientRepo clientrepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong( 0);
    private final AtomicLong allowedRequests = new AtomicLong(0 );
    private final AtomicLong blockedRequests = new AtomicLong(0);
    private final ConcurrentHashMap<String, Integer> capacities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> refillRates = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
    List<Client> clients = clientrepo.findAll();
    for (Client client : clients) {
        String key = "clientId:" + client.getClientId();
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForHash().put(key, "availableTokens", String.valueOf(client.getCapacity()));
            redisTemplate.opsForHash().put(key, "lastRefillTime", String.valueOf(System.currentTimeMillis()));
            redisTemplate.opsForHash().put(key, "capacity", String.valueOf(client.getCapacity()));
            redisTemplate.opsForHash().put(key, "refillRate", String.valueOf(client.getRefillRate()));
        }
        locks.put(client.getClientId(), new ReentrantLock());
        capacities.put(client.getClientId(), client.getCapacity());
        refillRates.put(client.getClientId(), client.getRefillRate());
    }
}
    
    public String register(ClientRequest request){
        if (clientrepo.findByClientId(request.getClientId()).isPresent()) {
            return "Client already exists";
        }
        Client client1 = new Client();
        client1.setCapacity(request.getCapacity());
        client1.setClientId(request.getClientId());
        client1.setRefillRate(request.getRefillRate());
        clientrepo.save(client1);
        locks.put(request.getClientId(), new ReentrantLock());
        capacities.put(request.getClientId(), request.getCapacity());
        refillRates.put(request.getClientId(), request.getRefillRate());
        String key = "clientId:" + client1.getClientId();
        redisTemplate.opsForHash().put(key, "availableTokens", String.valueOf(client1.getCapacity()));
        redisTemplate.opsForHash().put(key, "lastRefillTime", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().put(key, "capacity", String.valueOf(client1.getCapacity()));
        redisTemplate.opsForHash().put(key, "refillRate", String.valueOf(client1.getRefillRate()));
        return "Registered Successfully";
    }

    public RateLimitResponse check(String ID) {
        totalRequests.incrementAndGet();
        RateLimitResponse response = new RateLimitResponse();
        ReentrantLock lock = locks.get(ID);
        if (lock == null) {
            response.setAllowed(false);
            response.setMessage("Client not found");
            return response;
        }
        
        lock.lock();
        
        try {

            String key = "clientId:" + ID;
            if (!redisTemplate.hasKey(key)) {
                // Redis lost data, reload from PostgreSQL
                Client client = clientrepo.findByClientId(ID).orElse(null);
                if (client == null) {
                    response.setAllowed(false);
                    response.setMessage("Client not found");
                    return response;
                }
                
                redisTemplate.opsForHash().put(key, "availableTokens", String.valueOf(client.getCapacity()));
                redisTemplate.opsForHash().put(key, "lastRefillTime", String.valueOf(System.currentTimeMillis()));
                redisTemplate.opsForHash().put(key, "capacity", String.valueOf(client.getCapacity()));
                redisTemplate.opsForHash().put(key, "refillRate", String.valueOf(client.getRefillRate()));
            }

            Map<Object, Object> bucket = redisTemplate.opsForHash().entries(key);
            

            response.setClientId(ID);
            int capacity = capacities.get(ID);
            int refillRate = refillRates.get(ID);
            int availableTokens = Integer.parseInt((String) bucket.get("availableTokens"));
            long lastRefillTime = Long.parseLong((String) bucket.get("lastRefillTime"));
            
            long currentTime = System.currentTimeMillis();
            double elapsedSeconds = (currentTime - lastRefillTime) / 1000.0;
            int tokensToAdd = (int) (elapsedSeconds * refillRate);
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);

            // consume
            boolean allowed = availableTokens > 0;
            if (allowed) {
                availableTokens--;
                allowedRequests.incrementAndGet();
            }

            else blockedRequests.incrementAndGet();

            // write back to Redis
            Map<String, String> updates = new HashMap<>();
            updates.put("availableTokens", String.valueOf(availableTokens));
            updates.put("lastRefillTime", String.valueOf(currentTime));

            redisTemplate.opsForHash().putAll(key, updates);
            response.setAllowed(allowed);
            response.setRemainingTokens(availableTokens);
            if(allowed) response.setMessage("Request Allowed");
            else response.setMessage("Rate Limit Exceeded");
        } 
        
        finally {
            lock.unlock();
        }

        return response;


    }

    public Set<String> getAllClients() {
        return redisTemplate.keys("*");
    }

    
    public MetricsResponse getMetrics() {
        return new MetricsResponse(
            totalRequests.get(),
            allowedRequests.get(),
            blockedRequests.get(),
            locks.size()
        );
    }
    
    
}
