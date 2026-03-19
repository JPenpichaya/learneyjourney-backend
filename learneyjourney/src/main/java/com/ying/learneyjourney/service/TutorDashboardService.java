package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.response.TutorDashboardResponse;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.EnrollmentRepository;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TutorDashboardService {
    private final CourseService courseService;
    private final CourseReviewService courseReviewService;
    private final TutorReviewService tutorReviewService;
    private final BookingService bookingService;

    public TutorDashboardResponse getDashboardInfo(UUID tutorId){
        TutorDashboardResponse tutorDashboardResponse = new TutorDashboardResponse();
        tutorDashboardResponse.setCourseRate(courseReviewService.getAverageRatingByCourseId(tutorId));
        tutorDashboardResponse.setTutorRate(tutorReviewService.getAverageReviewByTutorId(tutorId));
        tutorDashboardResponse.setUpComingClasses(bookingService.bookingComingSoonByTutorId(tutorId));
        tutorDashboardResponse.setActiveCourses(courseService.getCourseActiveByTutor(tutorId));
        return tutorDashboardResponse;
    }
}
