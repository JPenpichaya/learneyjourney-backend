package com.ying.learneyjourney.dto.request;

import com.ying.learneyjourney.dto.SelectedSlot;
import java.util.List;

public record UpdateTutorCredentialsRequest(
        String highestEducation,
        String certifications,
        String linkedinProfile,
        List<SelectedSlot> generalAvailability,
        String cvFileUrl,
        Boolean termsAccepted
) {}
