package com.homeserve.provider.service;

import com.homeserve.provider.dto.matching.ProviderCandidateResponse;
import com.homeserve.provider.entity.Provider;
import com.homeserve.provider.entity.ProviderLocation;
import com.homeserve.provider.entity.ProviderPerformance;
import com.homeserve.provider.entity.ProviderStatus;
import com.homeserve.provider.repository.ProviderMatchingRepository;
import com.homeserve.provider.repository.ProviderPerformanceRepository;
import com.homeserve.provider.repository.ProviderRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderMatchingService {

    private final ProviderMatchingRepository matchingRepository;

    private final ProviderRatingRepository ratingRepository;

    private final ProviderPerformanceRepository performanceRepository;


    /**
     * Finds providers eligible for a customer service request.
     *
     * Provider Service is responsible only for eligibility.
     *
     * Customer Service is responsible for:
     * - calculating ranking
     * - selecting top providers
     * - sending offers
     * - assigning provider
     */
    @Transactional(readOnly = true)
    public List<ProviderCandidateResponse> getMatchingCandidates(
            Long serviceId,
            Double customerLatitude,
            Double customerLongitude,
            Double radiusKm,
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            LocalTime requestedEndTime) {

        // -----------------------------------------
        // 1. Validate input
        // -----------------------------------------

        validateInput(
                serviceId,
                customerLatitude,
                customerLongitude,
                radiusKm,
                requestedDate,
                requestedStartTime,
                requestedEndTime
        );


        // -----------------------------------------
        // 2. Find eligible providers from database
        // -----------------------------------------

        List<Provider> providers =
                matchingRepository.findEligibleProviders(
                        serviceId,
                        ProviderStatus.ACTIVE,
                        true
                );


        // -----------------------------------------
        // 3. Convert providers to candidates
        // -----------------------------------------

        return providers.stream()

                // Provider must have location
                .filter(Objects::nonNull)

                .map(provider ->
                        toCandidate(
                                provider,
                                customerLatitude,
                                customerLongitude,
                                radiusKm
                        )
                )

                // Remove providers outside radius
                .filter(Objects::nonNull)

                .collect(Collectors.toList());
    }


    /**
     * Converts Provider entity into ProviderCandidateResponse.
     *
     * Distance is calculated here only for eligibility.
     * Provider Service does NOT rank candidates.
     */
    private ProviderCandidateResponse toCandidate(
            Provider provider,
            Double customerLatitude,
            Double customerLongitude,
            Double radiusKm) {

        // -----------------------------------------
        // 1. Get provider location
        // -----------------------------------------

        ProviderLocation location = provider.getLocation();

        if (location == null) {
            return null;
        }

        if (location.getLatitude() == null ||
                location.getLongitude() == null) {
            return null;
        }


        // -----------------------------------------
        // 2. Convert provider coordinates
        // -----------------------------------------

        double providerLatitude =
                location.getLatitude().doubleValue();

        double providerLongitude =
                location.getLongitude().doubleValue();


        // -----------------------------------------
        // 3. Calculate distance
        // -----------------------------------------

        double distanceKm =
                calculateDistance(
                        customerLatitude,
                        customerLongitude,
                        providerLatitude,
                        providerLongitude
                );


        // -----------------------------------------
        // 4. Check radius
        // -----------------------------------------

        if (distanceKm > radiusKm) {
            return null;
        }


        // -----------------------------------------
        // 5. Get average rating
        // -----------------------------------------

        BigDecimal averageRating =
                ratingRepository.findAverageRating(
                        provider.getId()
                );

        double rating = 0.0;

        if (averageRating != null) {
            rating = averageRating.doubleValue();
        }


        // -----------------------------------------
        // 6. Get provider performance
        // -----------------------------------------

        ProviderPerformance performance =
                performanceRepository
                        .findByProviderId(provider.getId())
                        .orElse(null);


        // -----------------------------------------
        // 7. Default performance values
        // -----------------------------------------

        double acceptanceRate = 0.0;

        double completionRate = 0.0;

        double cancellationRate = 0.0;

        double averageResponseTimeSeconds = 0.0;

        int activeJobs = 0;


        // -----------------------------------------
        // 8. Calculate performance metrics
        // -----------------------------------------

        if (performance != null) {

            int totalOffers =
                    performance.getTotalOffers() != null
                            ? performance.getTotalOffers()
                            : 0;

            int acceptedOffers =
                    performance.getAcceptedOffers() != null
                            ? performance.getAcceptedOffers()
                            : 0;

            int totalJobs =
                    performance.getTotalJobs() != null
                            ? performance.getTotalJobs()
                            : 0;

            int completedJobs =
                    performance.getCompletedJobs() != null
                            ? performance.getCompletedJobs()
                            : 0;

            int cancelledJobs =
                    performance.getCancelledJobs() != null
                            ? performance.getCancelledJobs()
                            : 0;


            // -----------------------------------------
            // Acceptance Rate
            // -----------------------------------------

            if (totalOffers > 0) {

                acceptanceRate =
                        ((double) acceptedOffers / totalOffers)
                                * 100.0;
            }


            // -----------------------------------------
            // Completion Rate
            // -----------------------------------------

            if (totalJobs > 0) {

                completionRate =
                        ((double) completedJobs / totalJobs)
                                * 100.0;
            }


            // -----------------------------------------
            // Cancellation Rate
            // -----------------------------------------

            if (totalJobs > 0) {

                cancellationRate =
                        ((double) cancelledJobs / totalJobs)
                                * 100.0;
            }


            // -----------------------------------------
            // Average Response Time
            // -----------------------------------------

            if (performance.getAverageResponseTimeSeconds() != null) {

                averageResponseTimeSeconds =
                        performance.getAverageResponseTimeSeconds();
            }


            // -----------------------------------------
            // Active Jobs
            // -----------------------------------------

            if (performance.getActiveJobs() != null) {

                activeJobs =
                        performance.getActiveJobs();
            }
        }


        // -----------------------------------------
        // 9. Provider availability
        // -----------------------------------------

        boolean available =
                provider.getStatus() == ProviderStatus.ACTIVE
                        && Boolean.TRUE.equals(
                        provider.getVerified()
                );


        // -----------------------------------------
        // 10. Build response
        // -----------------------------------------

        return ProviderCandidateResponse.builder()

                .providerId(
                        provider.getId()
                )

                .latitude(
                        providerLatitude
                )

                .longitude(
                        providerLongitude
                )

                .rating(
                        rating
                )

                .acceptanceRate(
                        acceptanceRate
                )

                .completionRate(
                        completionRate
                )

                .cancellationRate(
                        cancellationRate
                )

                .averageResponseTimeSeconds(
                        averageResponseTimeSeconds
                )

                .activeJobs(
                        activeJobs
                )

                .available(
                        available
                )

                .verified(
                        Boolean.TRUE.equals(
                                provider.getVerified()
                        )
                )

                .build();
    }


    /**
     * Calculates distance between two coordinates
     * using the Haversine formula.
     *
     * @return distance in kilometers
     */
    private double calculateDistance(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {

        final double EARTH_RADIUS_KM = 6371.0;

        double latDistance =
                Math.toRadians(
                        latitude2 - latitude1
                );

        double lonDistance =
                Math.toRadians(
                        longitude2 - longitude1
                );

        double a =
                Math.sin(latDistance / 2)
                        * Math.sin(latDistance / 2)

                        +

                        Math.cos(
                                Math.toRadians(latitude1)
                        )

                                * Math.cos(
                                Math.toRadians(latitude2)
                        )

                                * Math.sin(lonDistance / 2)
                                * Math.sin(lonDistance / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS_KM * c;
    }


    /**
     * Validates matching request parameters.
     */
    private void validateInput(
            Long serviceId,
            Double latitude,
            Double longitude,
            Double radiusKm,
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            LocalTime requestedEndTime) {

        if (serviceId == null || serviceId <= 0) {
            throw new IllegalArgumentException(
                    "Service ID must be valid"
            );
        }

        if (latitude == null ||
                latitude < -90 ||
                latitude > 90) {

            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (longitude == null ||
                longitude < -180 ||
                longitude > 180) {

            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }

        if (radiusKm == null || radiusKm <= 0) {

            throw new IllegalArgumentException(
                    "Radius must be greater than 0"
            );
        }

        if (requestedDate == null) {

            throw new IllegalArgumentException(
                    "Requested date is required"
            );
        }

        if (requestedStartTime == null ||
                requestedEndTime == null) {

            throw new IllegalArgumentException(
                    "Requested start and end time are required"
            );
        }

        if (!requestedStartTime.isBefore(requestedEndTime)) {

            throw new IllegalArgumentException(
                    "Requested start time must be before end time"
            );
        }
    }
}