package com.skillbridge.backend.user.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {
    Optional<ClientProfile> findByUserId(UUID userId);
}