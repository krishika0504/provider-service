package com.homeserve.provider.dto.notification;

import com.homeserve.provider.entity.DeviceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProviderDeviceResponse {

    private Long id;

    private Long providerId;

    private DeviceType deviceType;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}