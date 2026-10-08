package com.homeserve.provider.client;

import com.homeserve.provider.dto.tracking.ProviderTrackingEventRequest;
import com.homeserve.provider.dto.tracking.TrackingAccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "customer-service",
        contextId = "customerTrackingClient"
)
public interface CustomerTrackingClient {

    // =========================================================
    // START JOURNEY
    // =========================================================

    @PostMapping(
            "/internal/bookings/{bookingId}/start-journey"
    )
    Map<String, Object> startJourney(

            @PathVariable("bookingId")
            Long bookingId,

            @RequestParam("providerId")
            Long providerId,

            @RequestHeader("X-Internal-Api-Key")
            String internalApiKey
    );


    // =========================================================
    // VERIFY TRACKING ACCESS
    // =========================================================

    @GetMapping(
            "/internal/bookings/{bookingId}/tracking-access"
    )
    TrackingAccessResponse verifyTrackingAccess(

            @PathVariable("bookingId")
            Long bookingId,

            @RequestParam("providerId")
            Long providerId,

            @RequestHeader("X-Internal-Api-Key")
            String internalApiKey
    );

    @PostMapping(
            "/internal/tracking/provider-location"
    )
    void publishProviderLocation(

            @RequestBody
            ProviderTrackingEventRequest request,

            @RequestHeader(
                    "X-Internal-Api-Key"
            )
            String internalApiKey
    );
}