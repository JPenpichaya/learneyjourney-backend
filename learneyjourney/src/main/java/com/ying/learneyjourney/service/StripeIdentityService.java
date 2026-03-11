package com.ying.learneyjourney.service;

import com.stripe.exception.StripeException;
import com.stripe.model.identity.VerificationSession;
import com.stripe.param.identity.VerificationSessionCreateParams;
import com.ying.learneyjourney.config.StripeConfig;
import com.ying.learneyjourney.constaint.EnumIdentityStatus;
import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.dto.response.StripeIdentitySessionResponse;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeIdentityService {

    private final TutorProfileService tutorApplicationService;
    private final TutorProfileRepository repository;
    private final StripeConfig stripeProperties;

    @Transactional
    public StripeIdentitySessionResponse createVerificationSession(UUID applicationId) throws StripeException {
        TutorProfile app = tutorApplicationService.get(applicationId);

        VerificationSessionCreateParams params = VerificationSessionCreateParams.builder()
                .setType(VerificationSessionCreateParams.Type.DOCUMENT)
                .setReturnUrl(stripeProperties.getIdentity().getReturnUrl() + "?applicationId=" + app.getId())
                .putAllMetadata(Map.of(
                        "applicationId", app.getId().toString(),
                        "email", app.getEmail()
                ))
                .build();

        VerificationSession session = VerificationSession.create(params);

        app.setStripeIdentitySessionId(session.getId());
        app.setIdentityStatus(EnumIdentityStatus.PENDING);
        repository.save(app);

        return new StripeIdentitySessionResponse(
                session.getId(),
                session.getClientSecret(),
                session.getUrl(),
                session.getStatus()
        );
    }
}
