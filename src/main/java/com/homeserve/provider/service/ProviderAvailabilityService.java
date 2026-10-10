package com.homeserve.provider.service;

import com.homeserve.provider.dto.availability.ProviderAvailabilityRequest;
import com.homeserve.provider.dto.availability.ProviderAvailabilityResponse;
import com.homeserve.provider.entity.Provider;
import com.homeserve.provider.entity.ProviderStatus;
import com.homeserve.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProviderAvailabilityService {

    private final CurrentProviderService
            currentProviderService;

    private final ProviderRepository
            providerRepository;


    // =========================================================
    // GET CURRENT AVAILABILITY
    // =========================================================

    @Transactional(readOnly = true)
    public ProviderAvailabilityResponse
    getMyAvailability() {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        return mapToResponse(
                provider
        );
    }


    // =========================================================
    // UPDATE ONLINE / OFFLINE
    // =========================================================

    @Transactional
    public ProviderAvailabilityResponse
    updateMyAvailability(
            ProviderAvailabilityRequest request
    ) {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        if (request == null
                || request.getAvailable() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Availability status is required"
            );
        }


        // -----------------------------------------------------
        // Provider cannot go online unless account is ACTIVE
        // -----------------------------------------------------

        if (Boolean.TRUE.equals(
                request.getAvailable()
        )) {

            if (provider.getStatus()
                    != ProviderStatus.ACTIVE) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Provider account must be ACTIVE before going online"
                );
            }


            if (!Boolean.TRUE.equals(
                    provider.getVerified()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Provider must be verified before going online"
                );
            }
        }


        provider.setAvailableForJobs(
                request.getAvailable()
        );


        Provider updatedProvider =
                providerRepository.save(
                        provider
                );


        return mapToResponse(
                updatedProvider
        );
    }


    // =========================================================
    // MAP RESPONSE
    // =========================================================

    private ProviderAvailabilityResponse
    mapToResponse(
            Provider provider
    ) {

        return ProviderAvailabilityResponse
                .builder()

                .providerId(
                        provider.getId()
                )

                .available(
                        Boolean.TRUE.equals(
                                provider.getAvailableForJobs()
                        )
                )

                .providerStatus(
                        provider
                                .getStatus()
                                .name()
                )

                .verified(
                        Boolean.TRUE.equals(
                                provider.getVerified()
                        )
                )

                .build();
    }
}