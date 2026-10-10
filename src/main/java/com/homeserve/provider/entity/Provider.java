package com.homeserve.provider.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "providers",
        indexes = {
                @Index(
                        name = "idx_provider_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_provider_verified",
                        columnList = "verified"
                ),
                @Index(
                        name = "idx_provider_status_verified",
                        columnList = "status, verified"
                ),
                @Index(
                        name = "idx_provider_available_for_jobs",
                        columnList = "available_for_jobs"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // PERSONAL INFORMATION
    // =========================================================

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;


    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;


    // =========================================================
    // CONTACT / LOGIN
    // =========================================================

    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;


    @Column(
            nullable = false,
            unique = true,
            length = 20
    )
    private String phone;


    @Column(
            nullable = false,
            length = 255
    )
    private String password;


    // =========================================================
    // ACCOUNT STATUS
    //
    // ACTIVE != online
    //
    // ACTIVE means provider account is allowed to operate.
    // availableForJobs means provider currently wants jobs.
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    @Builder.Default
    private ProviderStatus status =
            ProviderStatus.PENDING_VERIFICATION;


    @Column(
            nullable = false
    )
    @Builder.Default
    private Boolean verified = false;


    // =========================================================
    // JOB AVAILABILITY
    //
    // true  = ONLINE / accepting jobs
    // false = OFFLINE / not accepting jobs
    // =========================================================

    @Column(
            name = "available_for_jobs",
            nullable = false
    )
    @Builder.Default
    private Boolean availableForJobs = false;


    // =========================================================
    // PROFESSIONAL INFORMATION
    // =========================================================

    @Column(
            name = "experience_years"
    )
    private Integer experienceYears;


    @Column(
            name = "profile_image_url",
            length = 500
    )
    private String profileImageUrl;


    // =========================================================
    // LOCATION
    // =========================================================

    @OneToOne(
            mappedBy = "provider",
            fetch = FetchType.LAZY
    )
    private ProviderLocation location;


    // =========================================================
    // AUDIT
    // =========================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    // =========================================================
    // LIFECYCLE
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;


        if (status == null) {
            status =
                    ProviderStatus.PENDING_VERIFICATION;
        }


        if (verified == null) {
            verified = false;
        }


        if (availableForJobs == null) {
            availableForJobs = false;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}