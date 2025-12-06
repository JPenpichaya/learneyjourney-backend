package com.ying.learneyjourney.criteria;

import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.master.SearchCriteria;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class CourseCriteria extends SearchCriteria<Course> {
    @Override
    public Specification<Course> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchText != null && !searchText.isBlank()) {
                String like = "%" + searchText.toLowerCase() + "%";
                predicates.add(
                        cb.like(cb.lower(root.get("title")), like)
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
