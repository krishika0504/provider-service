package com.homeserve.provider.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.homeserve.provider.dto.notification.FcmSendResult;
import com.homeserve.provider.dto.notification.NewServiceOfferNotificationRequest;
import com.homeserve.provider.entity.ProviderDevice;
import com.homeserve.provider.repository.ProviderDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmNotificationService {

    private static final String NOTIFICATION_TYPE =
            "NEW_SERVICE_OFFER";

    /*
     * Firebase multicast limit.
     */
    private static final int MAX_TOKENS_PER_BATCH = 500;


    private final FirebaseMessaging firebaseMessaging;

    private final ProviderDeviceRepository providerDeviceRepository;


    // =========================================================
    // SEND NEW SERVICE OFFER
    // =========================================================

    public FcmSendResult sendNewServiceOffer(
            Long providerId,
            NewServiceOfferNotificationRequest request
    ) {

        /*
         * Load only active devices belonging to provider.
         */
        List<ProviderDevice> activeDevices =
                providerDeviceRepository
                        .findByProviderIdAndActiveTrue(
                                providerId
                        );


        if (activeDevices.isEmpty()) {

            log.warn(
                    "No active FCM devices found for providerId={}",
                    providerId
            );

            return FcmSendResult.builder()
                    .providerId(providerId)
                    .totalDevices(0)
                    .successCount(0)
                    .failureCount(0)
                    .deactivatedTokens(0)
                    .build();
        }


        List<String> tokens =
                activeDevices.stream()
                        .map(
                                ProviderDevice::getDeviceToken
                        )
                        .filter(token ->
                                token != null
                                        && !token.isBlank()
                        )
                        .distinct()
                        .toList();


        if (tokens.isEmpty()) {

            log.warn(
                    "Provider {} has active device rows but no valid token values",
                    providerId
            );

            return FcmSendResult.builder()
                    .providerId(providerId)
                    .totalDevices(activeDevices.size())
                    .successCount(0)
                    .failureCount(0)
                    .deactivatedTokens(0)
                    .build();
        }


        Map<String, String> data =
                buildOfferPayload(
                        request
                );


        String title =
                "New Service Request";


        String body =
                buildNotificationBody(
                        request
                );


        int successCount = 0;
        int failureCount = 0;
        int deactivatedTokens = 0;


        /*
         * Usually a provider has only one or two devices,
         * but batching keeps implementation safe if this
         * changes later.
         */
        for (
                int start = 0;
                start < tokens.size();
                start += MAX_TOKENS_PER_BATCH
        ) {

            int end =
                    Math.min(
                            start + MAX_TOKENS_PER_BATCH,
                            tokens.size()
                    );


            List<String> batchTokens =
                    new ArrayList<>(
                            tokens.subList(
                                    start,
                                    end
                            )
                    );


            try {

                MulticastMessage message =
                        buildMulticastMessage(
                                batchTokens,
                                title,
                                body,
                                data
                        );


                BatchResponse batchResponse =
                        firebaseMessaging
                                .sendEachForMulticast(
                                        message
                                );


                successCount +=
                        batchResponse.getSuccessCount();

                failureCount +=
                        batchResponse.getFailureCount();


                deactivatedTokens +=
                        processFailures(
                                batchTokens,
                                batchResponse
                        );


                log.info(
                        """
                        FCM offer notification processed:
                        providerId={}
                        offerId={}
                        bookingId={}
                        success={}
                        failure={}
                        """,
                        providerId,
                        request.getOfferId(),
                        request.getBookingId(),
                        batchResponse.getSuccessCount(),
                        batchResponse.getFailureCount()
                );


            } catch (FirebaseMessagingException exception) {

                /*
                 * A FirebaseMessagingException here means
                 * the whole multicast request failed.
                 *
                 * Do NOT deactivate every token here because
                 * the failure may be Firebase/network/quota
                 * related rather than token-related.
                 */

                failureCount +=
                        batchTokens.size();


                log.error(
                        """
                        FCM multicast request failed.

                        providerId={}
                        offerId={}
                        bookingId={}
                        firebaseError={}
                        message={}
                        """,
                        providerId,
                        request.getOfferId(),
                        request.getBookingId(),
                        exception.getMessagingErrorCode(),
                        exception.getMessage(),
                        exception
                );
            }
        }


        return FcmSendResult.builder()

                .providerId(
                        providerId
                )

                .totalDevices(
                        tokens.size()
                )

                .successCount(
                        successCount
                )

                .failureCount(
                        failureCount
                )

                .deactivatedTokens(
                        deactivatedTokens
                )

                .build();
    }


    // =========================================================
    // BUILD MULTICAST MESSAGE
    // =========================================================

    private MulticastMessage buildMulticastMessage(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> data
    ) {

        Notification notification =
                Notification.builder()

                        .setTitle(
                                title
                        )

                        .setBody(
                                body
                        )

                        .build();


        AndroidNotification androidNotification =
                AndroidNotification.builder()

                        /*
                         * Flutter Android app can later
                         * configure this notification channel.
                         */
                        .setChannelId(
                                "service_offers"
                        )

                        .setSound(
                                "default"
                        )

                        .build();


        AndroidConfig androidConfig =
                AndroidConfig.builder()

                        /*
                         * Service offers are time-sensitive.
                         */
                        .setPriority(
                                AndroidConfig.Priority.HIGH
                        )

                        .setNotification(
                                androidNotification
                        )

                        .build();


        return MulticastMessage.builder()

                .addAllTokens(
                        tokens
                )

                .setNotification(
                        notification
                )

                .putAllData(
                        data
                )

                .setAndroidConfig(
                        androidConfig
                )

                .build();
    }


    // =========================================================
    // BUILD FCM DATA PAYLOAD
    // =========================================================

    private Map<String, String> buildOfferPayload(
            NewServiceOfferNotificationRequest request
    ) {

        Map<String, String> data =
                new HashMap<>();


        /*
         * Every FCM data value must be a String.
         */
        data.put(
                "type",
                NOTIFICATION_TYPE
        );


        putIfNotNull(
                data,
                "offerId",
                request.getOfferId()
        );


        putIfNotNull(
                data,
                "bookingId",
                request.getBookingId()
        );


        putIfNotNull(
                data,
                "serviceId",
                request.getServiceId()
        );


        putIfNotNull(
                data,
                "serviceName",
                request.getServiceName()
        );


        putIfNotNull(
                data,
                "servicePrice",
                request.getServicePrice()
        );


        putIfNotNull(
                data,
                "address",
                request.getAddress()
        );


        putIfNotNull(
                data,
                "distanceKm",
                request.getDistanceKm()
        );


        putIfNotNull(
                data,
                "requestedDate",
                request.getRequestedDate()
        );


        putIfNotNull(
                data,
                "requestedStartTime",
                request.getRequestedStartTime()
        );


        putIfNotNull(
                data,
                "requestedEndTime",
                request.getRequestedEndTime()
        );


        putIfNotNull(
                data,
                "expiresAt",
                request.getExpiresAt()
        );


        return data;
    }


    // =========================================================
    // NOTIFICATION BODY
    // =========================================================

    private String buildNotificationBody(
            NewServiceOfferNotificationRequest request
    ) {

        String serviceName =
                request.getServiceName() != null
                        ? request.getServiceName()
                        : "Home Service";


        if (request.getServicePrice() != null) {

            return serviceName
                    + " • ₹"
                    + request.getServicePrice();
        }


        return serviceName;
    }


    // =========================================================
    // PROCESS INDIVIDUAL TOKEN FAILURES
    // =========================================================

    private int processFailures(
            List<String> tokens,
            BatchResponse batchResponse
    ) {

        int deactivatedCount = 0;


        List<SendResponse> responses =
                batchResponse.getResponses();


        /*
         * Firebase guarantees response order corresponds
         * to token order.
         */
        for (
                int index = 0;
                index < responses.size();
                index++
        ) {

            SendResponse response =
                    responses.get(index);


            if (response.isSuccessful()) {
                continue;
            }


            String token =
                    tokens.get(index);


            FirebaseMessagingException exception =
                    response.getException();


            if (exception == null) {

                log.warn(
                        "FCM send failed without exception details for token={}",
                        maskToken(token)
                );

                continue;
            }


            MessagingErrorCode errorCode =
                    exception.getMessagingErrorCode();


            log.warn(
                    """
                    FCM device delivery failed:
                    token={}
                    errorCode={}
                    message={}
                    """,
                    maskToken(token),
                    errorCode,
                    exception.getMessage()
            );


            /*
             * UNREGISTERED means Firebase explicitly says
             * this registration token is no longer valid.
             *
             * Deactivate it so future offers do not try
             * sending to it again.
             */
            if (errorCode
                    == MessagingErrorCode.UNREGISTERED) {

                deactivateToken(
                        token
                );

                deactivatedCount++;
            }


            /*
             * Do NOT automatically deactivate every
             * INVALID_ARGUMENT result.
             *
             * INVALID_ARGUMENT may also mean our FCM
             * message payload itself is malformed.
             */
            if (errorCode
                    == MessagingErrorCode.INVALID_ARGUMENT) {

                log.error(
                        """
                        Firebase returned INVALID_ARGUMENT.
                        Check the FCM payload before deciding
                        that the device token itself is invalid.
                        token={}
                        """,
                        maskToken(token)
                );
            }
        }


        return deactivatedCount;
    }


    // =========================================================
    // DEACTIVATE INVALID TOKEN
    // =========================================================

    @Transactional
    protected void deactivateToken(
            String token
    ) {

        providerDeviceRepository
                .findByDeviceToken(
                        token
                )
                .ifPresent(device -> {

                    device.setActive(false);

                    providerDeviceRepository.save(
                            device
                    );


                    log.info(
                            "Deactivated invalid FCM token for providerId={}, deviceId={}",
                            device.getProvider().getId(),
                            device.getId()
                    );
                });
    }


    // =========================================================
    // PAYLOAD UTILITY
    // =========================================================

    private void putIfNotNull(
            Map<String, String> data,
            String key,
            Object value
    ) {

        if (value != null) {

            data.put(
                    key,
                    String.valueOf(value)
            );
        }
    }


    // =========================================================
    // TOKEN LOGGING UTILITY
    // =========================================================

    private String maskToken(
            String token
    ) {

        if (token == null) {
            return "null";
        }


        if (token.length() <= 12) {
            return "***";
        }


        return token.substring(0, 6)
                + "..."
                + token.substring(
                token.length() - 6
        );
    }
}