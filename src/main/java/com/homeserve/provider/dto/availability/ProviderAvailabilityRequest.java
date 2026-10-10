package com.homeserve.provider.dto.availability;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderAvailabilityRequest {

    @NotNull(
            message = "Availability status is required"
    )
    private Boolean available;
}