package com.homeserve.provider.dto.tracking;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class ProviderLocationUpdateRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(
            value = "-90.0",
            message = "Latitude must be >= -90"
    )
    @DecimalMax(
            value = "90.0",
            message = "Latitude must be <= 90"
    )
    private Double latitude;


    @NotNull(message = "Longitude is required")
    @DecimalMin(
            value = "-180.0",
            message = "Longitude must be >= -180"
    )
    @DecimalMax(
            value = "180.0",
            message = "Longitude must be <= 180"
    )
    private Double longitude;


    private Double accuracy;

    private Double heading;

    private Double speed;

    private OffsetDateTime recordedAt;
}