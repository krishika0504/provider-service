package com.homeserve.provider.controller;

import com.homeserve.provider.dto.notification.FcmSendResult;
import com.homeserve.provider.dto.notification.InternalServiceOfferNotificationRequest;
import com.homeserve.provider.dto.notification.NewServiceOfferNotificationRequest;
import com.homeserve.provider.service.FcmNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final FcmNotificationService
            fcmNotificationService;


    @PostMapping("/service-offer")
    public ResponseEntity<FcmSendResult>
    sendServiceOfferNotification(

            @RequestBody
            InternalServiceOfferNotificationRequest request
    ) {

        NewServiceOfferNotificationRequest notification =
                NewServiceOfferNotificationRequest.builder()

                        .offerId(
                                request.getOfferId()
                        )

                        .bookingId(
                                request.getBookingId()
                        )

                        .serviceId(
                                request.getServiceId()
                        )

                        .serviceName(
                                request.getServiceName()
                        )

                        .servicePrice(
                                request.getServicePrice()
                        )

                        .address(
                                request.getAddress()
                        )

                        .distanceKm(
                                request.getDistanceKm()
                        )

                        .requestedDate(
                                request.getRequestedDate()
                        )

                        .requestedStartTime(
                                request.getRequestedStartTime()
                        )

                        .requestedEndTime(
                                request.getRequestedEndTime()
                        )

                        .expiresAt(
                                request.getExpiresAt()
                        )

                        .build();


        FcmSendResult result =
                fcmNotificationService
                        .sendNewServiceOffer(
                                request.getProviderId(),
                                notification
                        );


        return ResponseEntity.ok(
                result
        );
    }
}