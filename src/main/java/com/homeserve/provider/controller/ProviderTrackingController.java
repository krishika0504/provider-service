package com.homeserve.provider.controller;

import com.homeserve.provider.dto.common.ApiResponse;
import com.homeserve.provider.dto.tracking.ProviderLiveLocation;
import com.homeserve.provider.dto.tracking.ProviderLocationUpdateRequest;
import com.homeserve.provider.service.ProviderTrackingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/providers/me/jobs"
)
@RequiredArgsConstructor
@SecurityRequirement(
        name = "bearerAuth"
)
public class ProviderTrackingController {

    private final ProviderTrackingService
            providerTrackingService;


    // =========================================================
    // START JOURNEY
    // =========================================================

    @PostMapping(
            "/{bookingId}/start-journey"
    )
    public ResponseEntity<ApiResponse<Void>>
    startJourney(

            @PathVariable
            Long bookingId
    ) {

        providerTrackingService
                .startJourney(
                        bookingId
                );


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Journey started successfully",
                        null
                )
        );
    }


    // =========================================================
    // UPDATE LOCATION
    // =========================================================

    @PostMapping(
            "/{bookingId}/location"
    )
    public ResponseEntity<
            ApiResponse<ProviderLiveLocation>>
    updateLocation(

            @PathVariable
            Long bookingId,

            @Valid
            @RequestBody
            ProviderLocationUpdateRequest request
    ) {

        ProviderLiveLocation location =
                providerTrackingService
                        .updateLocation(
                                bookingId,
                                request
                        );


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Provider location updated successfully",
                        location
                )
        );
    }
}