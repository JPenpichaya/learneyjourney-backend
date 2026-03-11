package com.ying.learneyjourney.mapper;

import com.ying.learneyjourney.dto.response.TutorApplicationResponse;
import com.ying.learneyjourney.entity.TutorProfile;

public class TutorApplicationMapper {
    public static TutorApplicationResponse toResponse(TutorProfile a) {
        return new TutorApplicationResponse(
                a.getId(),
                a.getFullName(),
                a.getEmail(),
                a.getPhoneNumber(),
                a.getCountry(),
                a.getSubjects(),
                a.getYearsExperience(),
                a.getTeachingBio(),
                a.getHighestEducation(),
                a.getCertifications(),
                a.getLinkedinProfile(),
                a.getGeneralAvailability(),
                a.getCvFileUrl(),
                a.getTermsAccepted(),
                a.getStatus(),
                a.getIdentityStatus(),
                a.getStripeConnectAccountId(),
                a.getConnectOnboardingComplete(),
                a.getPayoutsEnabled(),
                a.getChargesEnabled()
        );
    }
}
