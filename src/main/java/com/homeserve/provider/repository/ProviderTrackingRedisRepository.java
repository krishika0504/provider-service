package com.homeserve.provider.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeserve.provider.dto.tracking.ProviderLiveLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProviderTrackingRedisRepository {

    private static final String KEY_PREFIX =
            "homeserve:tracking:booking:";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;


    @Value("${homeserve.tracking.location-ttl-seconds:120}")
    private long locationTtlSeconds;


    // =========================================================
    // SAVE LATEST LOCATION
    // =========================================================

    public void save(
            ProviderLiveLocation location
    ) {

        try {

            String key =
                    buildKey(
                            location.getBookingId()
                    );


            String json =
                    objectMapper.writeValueAsString(
                            location
                    );


            redisTemplate
                    .opsForValue()
                    .set(
                            key,
                            json,
                            Duration.ofSeconds(
                                    locationTtlSeconds
                            )
                    );


        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to serialize provider live location",
                    exception
            );
        }
    }


    // =========================================================
    // GET LATEST LOCATION
    // =========================================================

    public Optional<ProviderLiveLocation>
    findByBookingId(
            Long bookingId
    ) {

        String json =
                redisTemplate
                        .opsForValue()
                        .get(
                                buildKey(
                                        bookingId
                                )
                        );


        if (json == null) {
            return Optional.empty();
        }


        try {

            return Optional.of(
                    objectMapper.readValue(
                            json,
                            ProviderLiveLocation.class
                    )
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to deserialize provider live location",
                    exception
            );
        }
    }


    // =========================================================
    // DELETE TRACKING LOCATION
    // =========================================================

    public void delete(
            Long bookingId
    ) {

        redisTemplate.delete(
                buildKey(
                        bookingId
                )
        );
    }


    private String buildKey(
            Long bookingId
    ) {

        return KEY_PREFIX + bookingId;
    }
}