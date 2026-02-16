package com.ying.learneyjourney.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseFilter;

    public SecurityConfig(FirebaseAuthFilter firebaseFilter) {
        this.firebaseFilter = firebaseFilter;
    }

    @Bean
    SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        (req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                ))
                .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(b -> b.disable()) // IMPORTANT: avoid Basic challenges on API
                .authorizeHttpRequests(auth -> auth

                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers(
                                "/api/stripe/webhook",
                                "/api/stripe/webhook-test",
                                "/api/checkout/create-session",
                                "/api/auth/**"
                        ).permitAll()

                        // ✅ USER endpoints (USER, TEACHER, ADMIN)
                        .requestMatchers(
                                "/api/courses/**",
                                "/api/lessons/**",
                                "/api/lesson-progress/**"
                        ).hasAnyRole("USER", "TEACHER", "ADMIN")

                        // ✅ TEACHER endpoints (TEACHER, ADMIN)
                        .requestMatchers(
                                "/api/teacher/**"
                        ).hasAnyRole("TEACHER", "ADMIN")

                        // ✅ ADMIN endpoints (ADMIN only)
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/users/**",
                                "/api/system/**"
                        ).hasRole("ADMIN")

                        // Everything else under /api requires at least logged-in
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}