package com.ying.learneyjourney.criteria;

import com.ying.learneyjourney.entity.CourseLesson;
import com.ying.learneyjourney.master.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public class CourseLessonCriteria extends SearchCriteria<CourseLesson> {

    @Override
    public Specification<CourseLesson> getSpecification() {
        return null;
    }
}
