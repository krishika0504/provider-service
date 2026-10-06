package com.homeserve.provider.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmSendResult {

    private Long providerId;

    private int totalDevices;

    private int successCount;

    private int failureCount;

    private int deactivatedTokens;
}