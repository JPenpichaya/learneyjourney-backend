package com.ying.learneyjourney.dto.request;

import com.ying.learneyjourney.constaint.EnumSubjectType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateTutorApplicationRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank String phoneNumber,
        String country,
        @NotEmpty Set<EnumSubjectType> subjects,
        Integer yearsExperience,
        String teachingBio
) {}