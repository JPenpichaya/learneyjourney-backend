package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.criteria.SocialMediaPostCriteria;
import com.ying.learneyjourney.dto.SocialMediaPostDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.PageCriteria;
import com.ying.learneyjourney.service.SocialMediaPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/social-media-post")
public class SocialMediaPostController implements MasterController<SocialMediaPostDto, UUID> {
    private final SocialMediaPostService socialMediaPostService;
    @Override
    public ResponseEntity<SocialMediaPostDto> getById(UUID uuid) {
        return null;
    }

    @Override
    public ResponseEntity<Page<SocialMediaPostDto>> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<SocialMediaPostDto> create(SocialMediaPostDto body) {
        return ResponseEntity.ok(socialMediaPostService.create(body));
    }

    @Override
    public ResponseEntity<SocialMediaPostDto> update(UUID uuid, SocialMediaPostDto body) {
        return null;
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        return null;
    }

    @PostMapping("post-by-profile")
    public ResponseEntity<Page<SocialMediaPostDto>> getPostsByProfile(@RequestBody PageCriteria<SocialMediaPostCriteria> conditions) {
        return ResponseEntity.ok(socialMediaPostService.getPostsByTutorProfileId(conditions));
    }
}
