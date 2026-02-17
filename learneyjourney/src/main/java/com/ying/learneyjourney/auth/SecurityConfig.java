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
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}supersecret") // no encoding
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .httpBasic(Customizer.withDefaults()) // Basic for admin
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // public
                        .requestMatchers(
                                "/stripe-success.html",
                                "/public/**",
                                "/health",
                                "/dbcheck",
                                "/api/stripe/webhook",
                                "/api/stripe/webhook-test",
                                "/api/checkout/create-session",
                                "/token",
                                "/api/course/all/**"
                        ).permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ USER APIs (USER/TEACHER/ADMIN can call)
                        .requestMatchers("/api/lesson-progress/**")
                        .hasAnyRole("USER", "TEACHER", "ADMIN")

                        // ✅ TEACHER APIs (TEACHER/ADMIN can call)
                        .requestMatchers("/api/teacher/**")
                        .hasAnyRole("TEACHER", "ADMIN")

                        // ✅ ADMIN APIs (ADMIN only)
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // everything else: admin
                        .anyRequest().hasRole("ADMIN")
                )
                .addFilterBefore(firebaseFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}