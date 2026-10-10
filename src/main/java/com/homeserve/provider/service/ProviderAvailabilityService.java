package com.homeserve.provider.service;

import com.homeserve.provider.dto.availability.ProviderAvailabilityRequest;
import com.homeserve.provider.dto.availability.ProviderAvailabilityResponse;
import com.homeserve.provider.entity.AvailabilityStatus;
import com.homeserve.provider.entity.Provider;
import com.homeserve.provider.entity.ProviderAvailability;
import com.homeserve.provider.repository.ProviderAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderAvailabilityService {

    private final ProviderAvailabilityRepository
            availabilityRepository;

    private final CurrentProviderService
            currentProviderService;


    // =========================================================
    // ADD AVAILABILITY
    // =========================================================

    @Transactional
    public ProviderAvailabilityResponse addMyAvailability(
            ProviderAvailabilityRequest request
    ) {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        validateRequest(
                request
        );


        // -----------------------------------------------------
        // Prevent duplicate / overlapping AVAILABLE slot
        // -----------------------------------------------------

        boolean overlapping =
                availabilityRepository
                        .existsOverlappingAvailability(

                                provider.getId(),

                                request.getAvailableDate(),

                                request.getStartTime(),

                                request.getEndTime(),

                                AvailabilityStatus.AVAILABLE
                        );


        if (overlapping) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Availability overlaps with an existing available slot"
            );
        }


        ProviderAvailability availability =
                ProviderAvailability.builder()

                        .provider(
                                provider
                        )

                        .availableDate(
                                request.getAvailableDate()
                        )

                        .startTime(
                                request.getStartTime()
                        )

                        .endTime(
                                request.getEndTime()
                        )

                        .status(
                                AvailabilityStatus.AVAILABLE
                        )

                        .build();


        ProviderAvailability saved =
                availabilityRepository
                        .save(
                                availability
                        );


        return mapToResponse(
                saved
        );
    }


    // =========================================================
    // GET AVAILABILITY
    // =========================================================

    @Transactional(readOnly = true)
    public List<ProviderAvailabilityResponse>
    getMyAvailability(
            LocalDate date
    ) {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        List<ProviderAvailability> availability;


        if (date != null) {

            availability =
                    availabilityRepository
                            .findByProviderIdAndAvailableDate(

                                    provider.getId(),

                                    date
                            );

        } else {

            availability =
                    availabilityRepository
                            .findByProviderId(
                                    provider.getId()
                            );
        }


        return availability
                .stream()
                .map(
                        this::mapToResponse
                )
                .toList();
    }


    // =========================================================
    // UPDATE AVAILABILITY
    // =========================================================

    @Transactional
    public ProviderAvailabilityResponse
    updateMyAvailability(

            Long availabilityId,

            ProviderAvailabilityRequest request
    ) {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        ProviderAvailability availability =
                getAvailabilityOrThrow(
                        availabilityId
                );


        verifyOwnership(
                availability,
                provider.getId()
        );


        validateRequest(
                request
        );


        /*
         * If this slot is currently AVAILABLE,
         * ensure the new date/time does not overlap
         * another AVAILABLE slot.
         *
         * If it is UNAVAILABLE, we allow editing.
         * Overlap will be checked again when it is
         * switched back to AVAILABLE.
         */
        if (availability.getStatus()
                == AvailabilityStatus.AVAILABLE) {

            boolean overlapping =
                    availabilityRepository
                            .existsOverlappingAvailabilityExcludingId(

                                    provider.getId(),

                                    availabilityId,

                                    request.getAvailableDate(),

                                    request.getStartTime(),

                                    request.getEndTime(),

                                    AvailabilityStatus.AVAILABLE
                            );


            if (overlapping) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Availability overlaps with another available slot"
                );
            }
        }


        availability.setAvailableDate(
                request.getAvailableDate()
        );


        availability.setStartTime(
                request.getStartTime()
        );


        availability.setEndTime(
                request.getEndTime()
        );


        ProviderAvailability updated =
                availabilityRepository
                        .save(
                                availability
                        );


        return mapToResponse(
                updated
        );
    }


    // =========================================================
    // UPDATE AVAILABILITY STATUS
    // =========================================================

    @Transactional
    public ProviderAvailabilityResponse
    updateMyAvailabilityStatus(

            Long availabilityId,

            AvailabilityStatus status
    ) {

        if (status == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Availability status is required"
            );
        }


        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        ProviderAvailability availability =
                getAvailabilityOrThrow(
                        availabilityId
                );


        verifyOwnership(
                availability,
                provider.getId()
        );


        // -----------------------------------------------------
        // When changing UNAVAILABLE -> AVAILABLE,
        // ensure it does not overlap another active slot.
        // -----------------------------------------------------

        if (status == AvailabilityStatus.AVAILABLE) {

            validateDate(
                    availability.getAvailableDate()
            );


            boolean overlapping =
                    availabilityRepository
                            .existsOverlappingAvailabilityExcludingId(

                                    provider.getId(),

                                    availabilityId,

                                    availability.getAvailableDate(),

                                    availability.getStartTime(),

                                    availability.getEndTime(),

                                    AvailabilityStatus.AVAILABLE
                            );


            if (overlapping) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot activate this slot because it overlaps with another available slot"
                );
            }
        }


        availability.setStatus(
                status
        );


        ProviderAvailability updated =
                availabilityRepository
                        .save(
                                availability
                        );


        return mapToResponse(
                updated
        );
    }


    // =========================================================
    // DELETE AVAILABILITY
    // =========================================================

    @Transactional
    public void deleteMyAvailability(
            Long availabilityId
    ) {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        ProviderAvailability availability =
                getAvailabilityOrThrow(
                        availabilityId
                );


        verifyOwnership(
                availability,
                provider.getId()
        );


        availabilityRepository.delete(
                availability
        );
    }


    // =========================================================
    // VALIDATE REQUEST
    // =========================================================

    private void validateRequest(
            ProviderAvailabilityRequest request
    ) {

        if (request == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Availability request is required"
            );
        }


        validateDate(
                request.getAvailableDate()
        );


        validateTime(
                request.getStartTime(),
                request.getEndTime()
        );
    }


    // =========================================================
    // VALIDATE DATE
    // =========================================================

    private void validateDate(
            LocalDate date
    ) {

        if (date == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Available date is required"
            );
        }


        if (date.isBefore(
                LocalDate.now()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Availability date cannot be in the past"
            );
        }
    }


    // =========================================================
    // VALIDATE TIME
    // =========================================================

    private void validateTime(

            LocalTime startTime,

            LocalTime endTime
    ) {

        if (startTime == null
                || endTime == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time and end time are required"
            );
        }


        if (!startTime.isBefore(
                endTime
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start time must be before end time"
            );
        }
    }


    // =========================================================
    // GET AVAILABILITY
    // =========================================================

    private ProviderAvailability
    getAvailabilityOrThrow(
            Long availabilityId
    ) {

        return availabilityRepository
                .findById(
                        availabilityId
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Availability not found"
                                )
                );
    }


    // =========================================================
    // VERIFY OWNERSHIP
    // =========================================================

    private void verifyOwnership(

            ProviderAvailability availability,

            Long providerId
    ) {

        if (!availability
                .getProvider()
                .getId()
                .equals(
                        providerId
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify another provider's availability"
            );
        }
    }


    // =========================================================
    // MAP RESPONSE
    // =========================================================

    private ProviderAvailabilityResponse
    mapToResponse(
            ProviderAvailability availability
    ) {

        return ProviderAvailabilityResponse
                .builder()

                .id(
                        availability.getId()
                )

                .providerId(
                        availability
                                .getProvider()
                                .getId()
                )

                .availableDate(
                        availability
                                .getAvailableDate()
                )

                .startTime(
                        availability
                                .getStartTime()
                )

                .endTime(
                        availability
                                .getEndTime()
                )

                .status(
                        availability
                                .getStatus()
                )

                .createdAt(
                        availability
                                .getCreatedAt()
                )

                .updatedAt(
                        availability
                                .getUpdatedAt()
                )

                .build();
    }
}