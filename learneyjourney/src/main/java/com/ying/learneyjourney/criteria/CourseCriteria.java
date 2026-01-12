package com.ying.learneyjourney.criteria;

import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.Enrollment;
import com.ying.learneyjourney.master.SearchCriteria;
import jakarta.persistence.criteria.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class CourseCriteria extends SearchCriteria<Course> {
    private String classType;
    private List<String> badgeType;
    private String userId;
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

            if (classType != null && !classType.isBlank()) {
                switch (classType) {
                    case "live" -> predicates.add(cb.equal(root.get("isLive"), true));
                    case "video" -> predicates.add(cb.equal(root.get("isLive"), false));
                    case "all" -> {
                    }
                    // do nothing
                }
            }

            if (badgeType != null && !badgeType.isEmpty()) {
                predicates.add(root.get("badge").in(badgeType));
            }

            if (userId != null && !userId.isBlank()) {
                query.distinct(true); // optional but safe

                Subquery<Long> sq = query.subquery(Long.class);
                Root<Enrollment> e = sq.from(Enrollment.class);

                sq.select(cb.literal(1L))
                        .where(
                                cb.equal(e.get("course").get("id"), root.get("id")),
                                cb.equal(e.get("user").get("id"), userId)
                        );

                predicates.add(cb.exists(sq));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
