package com.homeserve.provider.dto.notification;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class InternalServiceOfferNotificationRequest {

    private Long providerId;

    private Long offerId;

    private Long bookingId;

    private Long serviceId;

    private String serviceName;

    private BigDecimal servicePrice;

    private String address;

    private Double distanceKm;

    private LocalDate requestedDate;

    private LocalTime requestedStartTime;

    private LocalTime requestedEndTime;

    private LocalDateTime expiresAt;
}