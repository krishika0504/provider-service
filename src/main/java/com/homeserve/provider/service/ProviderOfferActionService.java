package com.homeserve.provider.service;

import com.homeserve.provider.client.CustomerProviderOfferClient;
import com.homeserve.provider.dto.offer.InternalOfferActionResponse;
import com.homeserve.provider.dto.offer.ProviderOfferActionResponse;
import com.homeserve.provider.entity.Provider;
import com.homeserve.provider.entity.ProviderStatus;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderOfferActionService {

    private final CurrentProviderService
            currentProviderService;

    private final CustomerProviderOfferClient
            customerProviderOfferClient;


    @Value("${homeserve.internal.api-key}")
    private String internalApiKey;


    // =========================================================
    // ACCEPT OFFER
    // =========================================================

    public ProviderOfferActionResponse acceptOffer(
            Long offerId
    ) {

        Provider provider =
                getEligibleCurrentProvider();


        try {

            InternalOfferActionResponse response =
                    customerProviderOfferClient
                            .acceptOffer(
                                    offerId,
                                    provider.getId(),
                                    internalApiKey
                            );


            return buildResponse(
                    offerId,
                    provider.getId(),
                    "ACCEPT",
                    response
            );

        } catch (FeignException exception) {

            throw handleFeignException(
                    exception
            );
        }
    }


    // =========================================================
    // REJECT OFFER
    // =========================================================

    public ProviderOfferActionResponse rejectOffer(
            Long offerId
    ) {

        Provider provider =
                getEligibleCurrentProvider();


        try {

            InternalOfferActionResponse response =
                    customerProviderOfferClient
                            .rejectOffer(
                                    offerId,
                                    provider.getId(),
                                    internalApiKey
                            );


            return buildResponse(
                    offerId,
                    provider.getId(),
                    "REJECT",
                    response
            );

        } catch (FeignException exception) {

            throw handleFeignException(
                    exception
            );
        }
    }


    // =========================================================
    // CURRENT PROVIDER VALIDATION
    // =========================================================

    private Provider getEligibleCurrentProvider() {

        Provider provider =
                currentProviderService
                        .getCurrentProvider();


        if (provider.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Provider account is not active"
            );
        }


        if (!Boolean.TRUE.equals(
                provider.getVerified()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Provider account is not verified"
            );
        }


        return provider;
    }


    // =========================================================
    // BUILD RESPONSE
    // =========================================================

    private ProviderOfferActionResponse buildResponse(

            Long offerId,

            Long providerId,

            String action,

            InternalOfferActionResponse response
    ) {

        if (response == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Customer service returned empty response"
            );
        }


        if (!response.isSuccess()) {

            String message =
                    response.getMessage();


            if (message != null
                    &&
                    message.contains(
                            "not authorized"
                    )) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        message
                );
            }


            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    message
            );
        }


        return ProviderOfferActionResponse
                .builder()

                .offerId(
                        offerId
                )

                .providerId(
                        providerId
                )

                .action(
                        action
                )

                .message(
                        response.getMessage()
                )

                .build();
    }


    // =========================================================
    // FEIGN ERROR HANDLER
    // =========================================================

    private ResponseStatusException handleFeignException(
            FeignException exception
    ) {

        if (exception.status() == 401) {

            return new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Internal authentication with customer-service failed"
            );
        }


        if (exception.status() == 404) {

            return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Offer was not found"
            );
        }


        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Unable to process provider offer action"
        );
    }
}