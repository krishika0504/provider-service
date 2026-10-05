package com.homeserve.provider.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnregisterDeviceRequest {

    @NotBlank(message = "Device token is required")
    @Size(max = 500)
    private String deviceToken;
}