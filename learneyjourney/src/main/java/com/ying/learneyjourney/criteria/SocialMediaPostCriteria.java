package com.ying.learneyjourney.criteria;

import com.ying.learneyjourney.entity.SocialMediaPost;
import com.ying.learneyjourney.master.SearchCriteria;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Data
public class SocialMediaPostCriteria extends SearchCriteria<SocialMediaPost> {
    private UUID tutorId;
    @Override
    public Specification<SocialMediaPost> getSpecification() {
        return (root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();

            if (searchText != null && !searchText.isBlank()) {
                String like = "%" + searchText.toLowerCase() + "%";
                predicates.add(
                        cb.like(cb.lower(root.get("content")), like)
                );
            }

            if (tutorId != null) {
                predicates.add(cb.equal(root.get("tutorProfile").get("id"), tutorId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
