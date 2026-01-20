package com.ying.learneyjourney.service;

import com.ying.learneyjourney.criteria.SocialMediaPostCriteria;
import com.ying.learneyjourney.dto.SocialMediaPostDto;
import com.ying.learneyjourney.entity.SocialMediaPost;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.PageCriteria;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.repository.SocialMediaPostRepository;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialMediaPostService implements MasterService<SocialMediaPostDto, UUID> {
    private final SocialMediaPostRepository socialMediaPostRepository;
    private final TutorProfileRepository tutorProfileRepository;
    @Override
    public SocialMediaPostDto create(SocialMediaPostDto dto) {
        TutorProfile byId = tutorProfileRepository.findById(dto.getTutorProfileId()).orElseThrow(() -> new BusinessException("Tutor Profile not found", "TUTOR_PROFILE_NOT_FOUND"));
        SocialMediaPost entity = SocialMediaPostDto.toEntity(dto);
        entity.setTutorProfile(byId);
        socialMediaPostRepository.save(entity);
        return SocialMediaPostDto.fromEntity(entity);
    }

    @Override
    public SocialMediaPostDto update(UUID uuid, SocialMediaPostDto dto) {
        return null;
    }

    @Override
    public SocialMediaPostDto getById(UUID uuid) {
        return null;
    }

    @Override
    public List<SocialMediaPostDto> getAll() {
        return List.of();
    }

    @Override
    public Page<SocialMediaPostDto> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public void deleteById(UUID uuid) {

    }

    public Page<SocialMediaPostDto> getPostsByTutorProfileId(PageCriteria<SocialMediaPostCriteria> conditions) {
        Page<SocialMediaPost> posts = socialMediaPostRepository.findAll(conditions.getSpecification(), conditions.generatePageRequest());
        return posts.map(SocialMediaPostDto::fromEntity);
    }
}
