package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    @PostMapping("/test")
    public void EmailTest(@RequestBody String userEmail) {
        emailService.sendTestEmail(userEmail);
    }

}
