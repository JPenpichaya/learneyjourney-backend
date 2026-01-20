package com.ying.learneyjourney.controller;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ying.learneyjourney.entity.TutorProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import java.util.UUID;
import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tutor-connect")
@RequiredArgsConstructor
public class TutorConnectController {
    private final TutorProfileRepository tutorProfileRepository;
    private final FirebaseAuthUtil firebaseAuthUtil;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.connectClientId}")
    private String stripeConnectClientId;

    @Value("${stripe.apiBase:https://api.stripe.com}")
    private String stripeApiBase;

    @Value("${baseUrl}")
    private String appBaseUrl;

    @Value("${backendUrl}")
    private String frontendBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 1) Tutor clicks this endpoint -> you redirect them to Stripe OAuth authorize URL.
     */
    @GetMapping("/{tutorProfileId}/connect/authorize")
    public ResponseEntity<Void> authorize(@PathVariable UUID tutorProfileId) {

        // Your redirect/callback endpoint on YOUR backend:
        String redirectUri = appBaseUrl + "/api/tutor-connect/oauth/callback";

        // Build Stripe OAuth URL
        // Standard accounts doc uses response_type=code, scope=read_write, client_id, redirect_uri, state.
        // state should include tutorProfileId so we know who connected.
        String url = UriComponentsBuilder
                .fromHttpUrl("https://connect.stripe.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", stripeConnectClientId)
                .queryParam("scope", "read_write")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", tutorProfileId.toString())
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * 2) Stripe redirects here with ?code=...&state=...  (or error=...)
     * You must exchange code -> token at POST https://api.stripe.com/oauth/token
     */
    @GetMapping("/oauth/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            @RequestParam(required = false) String state
    ) {
        // If user cancels / Stripe returns error
        if (error != null) {
            // Send user back to frontend with error info
            String failUrl = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl + "/connect/failed")
                    .queryParam("error", error)
                    .queryParam("desc", error_description)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(failUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        if (code == null || state == null) {
            return ResponseEntity.badRequest().build();
        }

        UUID tutorProfileId = UUID.fromString(state);

        // Exchange authorization code for tokens (Stripe OAuth)
        StripeOAuthTokenResponse token = exchangeCodeForToken(code);

        // token.stripeUserId is the connected account id: acct_...
        String connectedAccountId = token.stripeUserId;

        // TODO: store connectedAccountId in TutorProfile table
         TutorProfile tp = tutorProfileRepository.findById(tutorProfileId).orElseThrow();
         tp.setStripConnect(connectedAccountId);
         tutorProfileRepository.save(tp);

        // Redirect back to frontend success page
        String successUrl = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl + "/connect/success")
                .queryParam("tutorProfileId", tutorProfileId.toString())
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(successUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private StripeOAuthTokenResponse exchangeCodeForToken(String code) {
        String tokenUrl = stripeApiBase + "/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(stripeSecretKey); // Authorization: Bearer sk_...

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<StripeOAuthTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                StripeOAuthTokenResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Stripe OAuth token exchange failed: " + response.getStatusCode());
        }

        return response.getBody();
    }

    // Minimal response fields (Stripe returns more too)
    public static class StripeOAuthTokenResponse {
        @JsonProperty("access_token")
        public String accessToken;

        @JsonProperty("livemode")
        public Boolean livemode;

        @JsonProperty("refresh_token")
        public String refreshToken;

        @JsonProperty("token_type")
        public String tokenType;

        @JsonProperty("stripe_user_id")
        public String stripeUserId; // acct_...

        @JsonProperty("scope")
        public String scope;

        @JsonProperty("publishable_key")
        public String publishableKey;
    }
}