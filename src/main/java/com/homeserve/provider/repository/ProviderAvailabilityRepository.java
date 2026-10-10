package com.homeserve.provider.repository;

import com.homeserve.provider.entity.AvailabilityStatus;
import com.homeserve.provider.entity.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ProviderAvailabilityRepository
        extends JpaRepository<ProviderAvailability, Long> {


    // =========================================================
    // PROVIDER AVAILABILITY
    // =========================================================

    List<ProviderAvailability> findByProviderId(
            Long providerId
    );


    List<ProviderAvailability>
    findByProviderIdAndAvailableDate(
            Long providerId,
            LocalDate availableDate
    );


    List<ProviderAvailability>
    findByProviderIdAndAvailableDateAndStatus(
            Long providerId,
            LocalDate availableDate,
            AvailabilityStatus status
    );


    // =========================================================
    // FIND OVERLAPPING AVAILABILITY
    // =========================================================

    @Query("""
        SELECT a
        FROM ProviderAvailability a
        WHERE a.provider.id = :providerId
          AND a.availableDate = :availableDate
          AND a.status = :status
          AND a.startTime < :requestedEndTime
          AND a.endTime > :requestedStartTime
    """)
    List<ProviderAvailability> findOverlappingAvailability(

            @Param("providerId")
            Long providerId,

            @Param("availableDate")
            LocalDate availableDate,

            @Param("requestedStartTime")
            LocalTime requestedStartTime,

            @Param("requestedEndTime")
            LocalTime requestedEndTime,

            @Param("status")
            AvailabilityStatus status
    );


    // =========================================================
    // CHECK OVERLAP WHEN CREATING
    // =========================================================

    @Query("""
        SELECT CASE
            WHEN COUNT(a) > 0
            THEN true
            ELSE false
        END
        FROM ProviderAvailability a
        WHERE a.provider.id = :providerId
          AND a.availableDate = :availableDate
          AND a.status = :status
          AND a.startTime < :endTime
          AND a.endTime > :startTime
    """)
    boolean existsOverlappingAvailability(

            @Param("providerId")
            Long providerId,

            @Param("availableDate")
            LocalDate availableDate,

            @Param("startTime")
            LocalTime startTime,

            @Param("endTime")
            LocalTime endTime,

            @Param("status")
            AvailabilityStatus status
    );


    // =========================================================
    // CHECK OVERLAP WHEN UPDATING
    //
    // Excludes the availability row being edited.
    // =========================================================

    @Query("""
        SELECT CASE
            WHEN COUNT(a) > 0
            THEN true
            ELSE false
        END
        FROM ProviderAvailability a
        WHERE a.provider.id = :providerId
          AND a.id <> :availabilityId
          AND a.availableDate = :availableDate
          AND a.status = :status
          AND a.startTime < :endTime
          AND a.endTime > :startTime
    """)
    boolean existsOverlappingAvailabilityExcludingId(

            @Param("providerId")
            Long providerId,

            @Param("availabilityId")
            Long availabilityId,

            @Param("availableDate")
            LocalDate availableDate,

            @Param("startTime")
            LocalTime startTime,

            @Param("endTime")
            LocalTime endTime,

            @Param("status")
            AvailabilityStatus status
    );


    // =========================================================
    // DELETE OWN AVAILABILITY
    // =========================================================

    void deleteByIdAndProviderId(
            Long id,
            Long providerId
    );
}