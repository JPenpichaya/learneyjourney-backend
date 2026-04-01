package com.ying.learneyjourney.criteria;

import com.ying.learneyjourney.entity.Worksheet;
import com.ying.learneyjourney.master.SearchCriteria;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
public class WorksheetCriteria extends SearchCriteria<Worksheet> {
    private String userId;
    @Override
    public Specification<Worksheet> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchText != null && !searchText.isBlank()) {
                String like = "%" + searchText.toLowerCase() + "%";
                predicates.add(
                        cb.like(cb.lower(root.get("promptText")), like)
                );
            }

            if (userId != null) {
                predicates.add(
                        cb.equal(root.get("user").get("id"), userId)
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
