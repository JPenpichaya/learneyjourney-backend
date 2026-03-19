package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.TutorReviewDto;
import com.ying.learneyjourney.repository.TutorReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TutorReviewService {
    private final TutorReviewRepository tutorReviewRepository;

    public List<TutorReviewDto> getReviewListByTutorById(UUID tutorId){
        return tutorReviewRepository.findListByTutorId(tutorId).stream()
            .map(TutorReviewDto::fromEntity)
            .toList();
    }

    public Double getAverageReviewByTutorId(UUID tutorId){
        return tutorReviewRepository.getAverageRatingByTutorId(tutorId);
    }
}
