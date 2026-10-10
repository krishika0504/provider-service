package com.homeserve.provider.dto.availability;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProviderAvailabilityResponse {

    private Long providerId;

    private Boolean available;

    private String providerStatus;

    private Boolean verified;
}