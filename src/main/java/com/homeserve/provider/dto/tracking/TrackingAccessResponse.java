package com.homeserve.provider.dto.tracking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingAccessResponse {

    private Long bookingId;

    private Long providerId;

    private Long customerId;

    private String status;

    private Double customerLatitude;

    private Double customerLongitude;

    private boolean trackingAllowed;
}