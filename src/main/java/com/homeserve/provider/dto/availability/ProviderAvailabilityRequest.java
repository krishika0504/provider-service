package com.homeserve.provider.dto.availability;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ProviderAvailabilityRequest {

    @NotNull(message = "Available date is required")
    @FutureOrPresent(
            message = "Availability date cannot be in the past"
    )
    private LocalDate availableDate;


    @NotNull(message = "Start time is required")
    private LocalTime startTime;


    @NotNull(message = "End time is required")
    private LocalTime endTime;
}