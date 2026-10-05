package com.homeserve.provider.service;

import com.homeserve.provider.dto.notification.ProviderDeviceResponse;
import com.homeserve.provider.dto.notification.RegisterDeviceRequest;
import com.homeserve.provider.dto.notification.UnregisterDeviceRequest;
import com.homeserve.provider.entity.Provider;
import com.homeserve.provider.entity.ProviderDevice;
import com.homeserve.provider.repository.ProviderDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderDeviceService {

    private final ProviderDeviceRepository deviceRepository;

    private final CurrentProviderService currentProviderService;


    // =========================================================
    // REGISTER / REFRESH DEVICE TOKEN
    // =========================================================

    @Transactional
    public ProviderDeviceResponse registerDevice(
            RegisterDeviceRequest request
    ) {

        Provider provider =
                currentProviderService.getCurrentProvider();

        String deviceToken =
                request.getDeviceToken().trim();


        /*
         * FCM token should belong to only one provider.
         *
         * If the same mobile phone logs out from provider1
         * and logs in as provider2, Firebase may still give
         * the same token.
         *
         * Therefore we search globally by token.
         */
        ProviderDevice device =
                deviceRepository
                        .findByDeviceToken(deviceToken)
                        .orElseGet(
                                () ->
                                        ProviderDevice.builder()
                                                .deviceToken(deviceToken)
                                                .build()
                        );


        // Reassign token to currently logged-in provider
        device.setProvider(provider);

        device.setDeviceToken(deviceToken);

        device.setDeviceType(
                request.getDeviceType()
        );

        // Reactivate if it had previously been disabled
        device.setActive(true);


        ProviderDevice saved =
                deviceRepository.save(device);


        return mapToResponse(saved);
    }


    // =========================================================
    // UNREGISTER DEVICE
    // =========================================================

    @Transactional
    public void unregisterDevice(
            UnregisterDeviceRequest request
    ) {

        Provider provider =
                currentProviderService.getCurrentProvider();

        ProviderDevice device =
                deviceRepository
                        .findByProviderIdAndDeviceToken(
                                provider.getId(),
                                request.getDeviceToken().trim()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Device token not registered for this provider"
                                        )
                        );


        /*
         * Do not necessarily delete the row.
         *
         * Keeping it inactive is useful for:
         * - debugging
         * - token history
         * - reactivation
         */
        device.setActive(false);

        deviceRepository.save(device);
    }


    // =========================================================
    // GET ACTIVE DEVICES
    // Mainly useful for testing/debugging.
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProviderDeviceResponse> getMyActiveDevices() {

        Provider provider =
                currentProviderService.getCurrentProvider();


        return deviceRepository
                .findByProviderIdAndActiveTrue(
                        provider.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // MAP RESPONSE
    // =========================================================

    private ProviderDeviceResponse mapToResponse(
            ProviderDevice device
    ) {

        return ProviderDeviceResponse.builder()

                .id(
                        device.getId()
                )

                .providerId(
                        device.getProvider().getId()
                )

                .deviceType(
                        device.getDeviceType()
                )

                .active(
                        device.getActive()
                )

                .createdAt(
                        device.getCreatedAt()
                )

                .updatedAt(
                        device.getUpdatedAt()
                )

                .build();
    }
}