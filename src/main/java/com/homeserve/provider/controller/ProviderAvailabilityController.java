package com.homeserve.provider.controller;

import com.homeserve.provider.dto.availability.ProviderAvailabilityRequest;
import com.homeserve.provider.dto.availability.ProviderAvailabilityResponse;
import com.homeserve.provider.dto.common.ApiResponse;
import com.homeserve.provider.service.ProviderAvailabilityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/providers/me/availability"
)
@RequiredArgsConstructor
@SecurityRequirement(
        name = "bearerAuth"
)
public class ProviderAvailabilityController {

    private final ProviderAvailabilityService
            availabilityService;


    // =========================================================
    // GET CURRENT ONLINE/OFFLINE STATUS
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<ProviderAvailabilityResponse>>
    getAvailability() {

        ProviderAvailabilityResponse response =
                availabilityService
                        .getMyAvailability();


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Provider availability fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // ONLINE / OFFLINE
    // =========================================================

    @PutMapping
    public ResponseEntity<
            ApiResponse<ProviderAvailabilityResponse>>
    updateAvailability(

            @Valid
            @RequestBody
            ProviderAvailabilityRequest request
    ) {

        ProviderAvailabilityResponse response =
                availabilityService
                        .updateMyAvailability(
                                request
                        );


        String message =
                Boolean.TRUE.equals(
                        response.getAvailable()
                )
                        ? "Provider is now online"
                        : "Provider is now offline";


        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        response
                )
        );
    }
}