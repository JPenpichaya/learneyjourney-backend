package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumApplicationStatus;
import com.ying.learneyjourney.constaint.EnumIdentityStatus;
import com.ying.learneyjourney.constaint.EnumSubjectType;
import com.ying.learneyjourney.converter.SelectedSlotListConverter;
import com.ying.learneyjourney.converter.StringListConverter;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "tutor_profile")
public class TutorProfile extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "strip_connect", length = 1000)
    private String stripConnect;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    private String country;
    private String highestEducation;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    private String linkedinProfile;

    @Column(columnDefinition = "TEXT")
    private String teachingBio;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> teachingStyles;

    @Convert(converter = SelectedSlotListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<SelectedSlot> generalAvailability;
    
    private Integer yearsExperience;
    private String cvFileUrl;
    private Boolean termsAccepted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumApplicationStatus status = EnumApplicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "identity_status")
    private EnumIdentityStatus identityStatus = EnumIdentityStatus.NOT_STARTED;

    private String stripeIdentitySessionId;
    private String stripeIdentityVerificationReportId;
    private String stripeConnectAccountId;
    private Boolean connectOnboardingComplete = false;
    private Boolean payoutsEnabled = false;
    private Boolean chargesEnabled = false;

    @ElementCollection(targetClass = EnumSubjectType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "tutor_application_subjects", joinColumns = @JoinColumn(name = "application_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_name")
    private Set<EnumSubjectType> subjects = new HashSet<>();


}