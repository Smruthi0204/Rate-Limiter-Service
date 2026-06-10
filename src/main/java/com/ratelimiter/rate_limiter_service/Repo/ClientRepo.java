package com.ratelimiter.rate_limiter_service.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ratelimiter.rate_limiter_service.Entity.Client;

@Repository
public interface ClientRepo extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(String clientId);
}