package com.ying.learneyjourney.controller;

import com.stripe.exception.StripeException;
import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.request.CreateTutorApplicationRequest;
import com.ying.learneyjourney.dto.request.UpdateTutorCredentialsRequest;
import com.ying.learneyjourney.dto.response.StripeConnectOnboardingResponse;
import com.ying.learneyjourney.dto.response.StripeIdentitySessionResponse;
import com.ying.learneyjourney.dto.response.TutorApplicationResponse;
import com.ying.learneyjourney.mapper.TutorApplicationMapper;
import com.ying.learneyjourney.service.StripeConnectService;
import com.ying.learneyjourney.service.StripeIdentityService;
import com.ying.learneyjourney.service.TutorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tutor-applications")
@RequiredArgsConstructor
public class TutorApplicationController {

    private final TutorProfileService tutorApplicationService;
    private final StripeIdentityService stripeIdentityService;
    private final StripeConnectService stripeConnectService;
    private final FirebaseAuthUtil firebaseAuthUtil;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TutorApplicationResponse create(@Valid @RequestBody CreateTutorApplicationRequest request,  @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String userId = authHeader != null ? firebaseAuthUtil.getUserIdFromToken(authHeader): null;
        return TutorApplicationMapper.toResponse(tutorApplicationService.createTutor(request, userId));
    }

    @PostMapping("/{id}")
    public TutorApplicationResponse get(@PathVariable UUID id) {
        return TutorApplicationMapper.toResponse(tutorApplicationService.get(id));
    }

    @PostMapping("/{id}/credentials")
    public TutorApplicationResponse updateCredentials(
            @PathVariable UUID id,
            @RequestBody UpdateTutorCredentialsRequest request
    ) {
        return TutorApplicationMapper.toResponse(tutorApplicationService.updateCredentials(id, request));
    }

    @PostMapping("/{id}/submit")
    public TutorApplicationResponse submit(@PathVariable UUID id) {
        return TutorApplicationMapper.toResponse(tutorApplicationService.submit(id));
    }

    @PostMapping("/{id}/stripe/identity-session")
    public StripeIdentitySessionResponse createIdentitySession(@PathVariable UUID id) throws StripeException, StripeException {
        return stripeIdentityService.createVerificationSession(id);
    }

    @PostMapping("/{id}/stripe/connect/onboarding-link")
    public StripeConnectOnboardingResponse createConnectOnboardingLink(@PathVariable UUID id) throws StripeException {
        return stripeConnectService.createOrResumeOnboarding(id);
    }

    @GetMapping("/{applicationId}/stripe/connect/return")
    public ResponseEntity<?> handleReturn(@PathVariable UUID applicationId) throws StripeException {
        stripeConnectService.syncAccountState(applicationId);

        var app = tutorApplicationService.get(applicationId);

        return ResponseEntity.ok(Map.of(
                "applicationId", app.getId(),
                "connectOnboardingComplete", app.getConnectOnboardingComplete(),
                "chargesEnabled", app.getChargesEnabled(),
                "payoutsEnabled", app.getPayoutsEnabled(),
                "status", app.getStatus().name()
        ));
    }
}
