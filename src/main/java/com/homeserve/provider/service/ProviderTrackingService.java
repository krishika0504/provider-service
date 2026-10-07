package com.homeserve.provider.service;

import com.homeserve.provider.client.CustomerTrackingClient;
import com.homeserve.provider.dto.tracking.ProviderLiveLocation;
import com.homeserve.provider.dto.tracking.ProviderLocationUpdateRequest;
import com.homeserve.provider.dto.tracking.ProviderTrackingEventRequest;
import com.homeserve.provider.dto.tracking.TrackingAccessResponse;
import com.homeserve.provider.entity.Provider;
import com.homeserve.provider.repository.ProviderTrackingRedisRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderTrackingService {

    private final CurrentProviderService currentProviderService;

    private final CustomerTrackingClient customerTrackingClient;

    private final ProviderTrackingRedisRepository
            trackingRedisRepository;


    @Value("${homeserve.internal.api-key}")
    private String internalApiKey;


    // =========================================================
    // START JOURNEY
    // =========================================================

    public void startJourney(
            Long bookingId
    ) {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        try {

            customerTrackingClient
                    .startJourney(
                            bookingId,
                            provider.getId(),
                            internalApiKey
                    );


            log.info(
                    "Provider journey started: bookingId={}, providerId={}",
                    bookingId,
                    provider.getId()
            );


        } catch (FeignException exception) {

            throw mapCustomerServiceError(
                    exception
            );
        }
    }


    // =========================================================
    // UPDATE LIVE LOCATION
    // =========================================================

    public ProviderLiveLocation updateLocation(
            Long bookingId,
            ProviderLocationUpdateRequest request
    ) {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        TrackingAccessResponse access;


        try {

            access =
                    customerTrackingClient
                            .verifyTrackingAccess(
                                    bookingId,
                                    provider.getId(),
                                    internalApiKey
                            );


        } catch (FeignException exception) {

            throw mapCustomerServiceError(
                    exception
            );
        }


        if (!access.isTrackingAllowed()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Live tracking is not active for this booking"
            );
        }


        OffsetDateTime now =
                OffsetDateTime.now();


        ProviderLiveLocation location =
                ProviderLiveLocation.builder()

                        .bookingId(
                                bookingId
                        )

                        .providerId(
                                provider.getId()
                        )

                        .latitude(
                                request.getLatitude()
                        )

                        .longitude(
                                request.getLongitude()
                        )

                        .accuracy(
                                request.getAccuracy()
                        )

                        .heading(
                                request.getHeading()
                        )

                        .speed(
                                request.getSpeed()
                        )

                        .recordedAt(
                                request.getRecordedAt() != null
                                        ? request.getRecordedAt()
                                        : now
                        )

                        .serverReceivedAt(
                                now
                        )

                        .build();


        trackingRedisRepository.save(
                location
        );
        ProviderTrackingEventRequest event =
                ProviderTrackingEventRequest.builder()

                        .bookingId(
                                location.getBookingId()
                        )

                        .providerId(
                                location.getProviderId()
                        )

                        .latitude(
                                location.getLatitude()
                        )

                        .longitude(
                                location.getLongitude()
                        )

                        .accuracy(
                                location.getAccuracy()
                        )

                        .heading(
                                location.getHeading()
                        )

                        .speed(
                                location.getSpeed()
                        )

                        .recordedAt(
                                location.getRecordedAt()
                        )

                        .serverReceivedAt(
                                location.getServerReceivedAt()
                        )

                        .build();


        try {

            customerTrackingClient
                    .publishProviderLocation(
                            event,
                            internalApiKey
                    );

        } catch (Exception exception) {

            /*
             * IMPORTANT:
             *
             * Do not reject/provider GPS update just because
             * customer-service WebSocket forwarding temporarily fails.
             *
             * Redis still contains the latest location.
             */

            log.warn(
                    "Unable to broadcast provider location: bookingId={}, providerId={}",
                    location.getBookingId(),
                    location.getProviderId()
            );
        }


        log.debug(
                "Live location stored: bookingId={}, providerId={}, latitude={}, longitude={}",
                bookingId,
                provider.getId(),
                request.getLatitude(),
                request.getLongitude()
        );


        return location;
    }


    // =========================================================
    // MAP CUSTOMER-SERVICE ERROR
    // =========================================================

    private ResponseStatusException mapCustomerServiceError(
            FeignException exception
    ) {

        int status =
                exception.status();


        if (status == 403) {

            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Provider is not assigned to this booking"
            );
        }


        if (status == 404) {

            return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found"
            );
        }


        if (status == 409) {

            return new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Booking is not in a valid tracking state"
            );
        }


        if (status == 401) {

            return new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Internal authentication with customer-service failed"
            );
        }


        if (status == 503 || status == -1) {

            return new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer-service is currently unavailable"
            );
        }


        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Unable to verify booking tracking state"
        );
    }
}