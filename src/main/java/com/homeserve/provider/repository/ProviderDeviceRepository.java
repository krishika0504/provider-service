package com.homeserve.provider.repository;

import com.homeserve.provider.entity.ProviderDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderDeviceRepository
        extends JpaRepository<ProviderDevice, Long> {

    Optional<ProviderDevice> findByDeviceToken(
            String deviceToken
    );

    List<ProviderDevice> findByProviderIdAndActiveTrue(
            Long providerId
    );

    Optional<ProviderDevice>
    findByProviderIdAndDeviceToken(
            Long providerId,
            String deviceToken
    );
}