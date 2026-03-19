package com.ying.learneyjourney.dto.response;

import lombok.Data;

@Data
public class TutorDashboardResponse {
    private Double courseRate;
    private Double tutorRate;
    private long upComingClasses;
    private long activeCourses;
}
