package com.homeserve.provider.controller;

import com.homeserve.provider.dto.common.ApiResponse;
import com.homeserve.provider.dto.notification.ProviderDeviceResponse;
import com.homeserve.provider.dto.notification.RegisterDeviceRequest;
import com.homeserve.provider.dto.notification.UnregisterDeviceRequest;
import com.homeserve.provider.service.ProviderDeviceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers/me/devices")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProviderDeviceController {

    private final ProviderDeviceService deviceService;


    // =========================================================
    // REGISTER / UPDATE FCM TOKEN
    // =========================================================

    @PostMapping
    public ResponseEntity<
            ApiResponse<ProviderDeviceResponse>>
    registerDevice(

            @Valid
            @RequestBody
            RegisterDeviceRequest request
    ) {

        ProviderDeviceResponse response =
                deviceService.registerDevice(
                        request
                );


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Provider device registered successfully",
                        response
                )
        );
    }


    // =========================================================
    // GET ACTIVE DEVICES
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<ProviderDeviceResponse>>>
    getMyDevices() {

        List<ProviderDeviceResponse> response =
                deviceService.getMyActiveDevices();


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Provider devices fetched successfully",
                        response
                )
        );
    }


    // =========================================================
    // UNREGISTER DEVICE
    // =========================================================

    @PostMapping("/unregister")
    public ResponseEntity<ApiResponse<Void>>
    unregisterDevice(

            @Valid
            @RequestBody
            UnregisterDeviceRequest request
    ) {

        deviceService.unregisterDevice(
                request
        );


        return ResponseEntity.ok(
                ApiResponse.success(
                        "Provider device unregistered successfully",
                        null
                )
        );
    }
}