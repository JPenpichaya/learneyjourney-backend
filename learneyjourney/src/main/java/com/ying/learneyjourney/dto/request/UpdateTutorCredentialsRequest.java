package com.ying.learneyjourney.dto.request;

public record UpdateTutorCredentialsRequest(
        String highestEducation,
        String certifications,
        String linkedinProfile,
        String generalAvailability,
        String cvFileUrl,
        Boolean termsAccepted
) {}
