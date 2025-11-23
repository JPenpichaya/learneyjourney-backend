package com.ying.learneyjourney.master;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SearchSpecification<E> implements Specification<E>{
    private final List<SearchCriteria> criteriaList = new ArrayList<>();

    public void add(SearchCriteria criteria) {
        criteriaList.add(criteria);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Predicate toPredicate(Root<E> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();

        for (SearchCriteria criteria : criteriaList) {
            Path path = root.get(criteria.getKey());

            switch (criteria.getOperation()) {
                case EQUAL -> predicates.add(builder.equal(path, criteria.getValue()));
                case NOT_EQUAL -> predicates.add(builder.notEqual(path, criteria.getValue()));
                case GREATER_THAN ->
                        predicates.add(builder.greaterThan(path, (Comparable) criteria.getValue()));
                case LESS_THAN ->
                        predicates.add(builder.lessThan(path, (Comparable) criteria.getValue()));
                case GREATER_THAN_EQUAL ->
                        predicates.add(builder.greaterThanOrEqualTo(path, (Comparable) criteria.getValue()));
                case LESS_THAN_EQUAL ->
                        predicates.add(builder.lessThanOrEqualTo(path, (Comparable) criteria.getValue()));
                case LIKE ->
                        predicates.add(
                                builder.like(
                                        builder.lower(path.as(String.class)),
                                        "%" + criteria.getValue().toString().toLowerCase() + "%"
                                )
                        );
                case IN -> {
                    CriteriaBuilder.In<Object> inClause = builder.in(path);
                    if (criteria.getValue() instanceof Iterable<?> iterable) {
                        for (Object v : iterable) {
                            inClause.value(v);
                        }
                    } else {
                        inClause.value(criteria.getValue());
                    }
                    predicates.add(inClause);
                }
                default -> { }
            }
        }

        return predicates.isEmpty()
                ? builder.conjunction()
                : builder.and(predicates.toArray(new Predicate[0]));
    }
}
