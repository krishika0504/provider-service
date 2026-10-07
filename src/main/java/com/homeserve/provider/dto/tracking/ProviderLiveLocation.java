package com.homeserve.provider.dto.tracking;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class ProviderLiveLocation {

    private Long bookingId;

    private Long providerId;

    private Double latitude;

    private Double longitude;

    private Double accuracy;

    private Double heading;

    private Double speed;

    private OffsetDateTime recordedAt;

    private OffsetDateTime serverReceivedAt;
}