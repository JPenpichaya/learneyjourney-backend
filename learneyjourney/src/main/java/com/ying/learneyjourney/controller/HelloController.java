package com.ying.learneyjourney.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/health") public String health() { return "ok"; }
    @GetMapping("/api/me") public String me(Authentication auth) {
        return "Hello uid=" + auth.getName();
    }
}