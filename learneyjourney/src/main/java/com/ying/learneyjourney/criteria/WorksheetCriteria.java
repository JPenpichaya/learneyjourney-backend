package com.ying.learneyjourney.criteria;

import com.ying.learneyjourney.entity.Worksheet;
import com.ying.learneyjourney.master.SearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WorksheetCriteria extends SearchCriteria<Worksheet> {
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
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
