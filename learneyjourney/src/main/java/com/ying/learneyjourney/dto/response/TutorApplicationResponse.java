package com.ying.learneyjourney.dto.response;

import com.ying.learneyjourney.constaint.EnumApplicationStatus;
import com.ying.learneyjourney.constaint.EnumIdentityStatus;
import com.ying.learneyjourney.constaint.EnumSubjectType;

import java.util.Set;
import java.util.UUID;

public record TutorApplicationResponse(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        String country,
        Set<EnumSubjectType> subjects,
        Integer yearsExperience,
        String teachingBio,
        String highestEducation,
        String certifications,
        String linkedinProfile,
        String generalAvailability,
        String cvFileUrl,
        Boolean termsAccepted,
        EnumApplicationStatus status,
        EnumIdentityStatus identityStatus,
        String stripeConnectAccountId,
        Boolean connectOnboardingComplete,
        Boolean payoutsEnabled,
        Boolean chargesEnabled
) {}