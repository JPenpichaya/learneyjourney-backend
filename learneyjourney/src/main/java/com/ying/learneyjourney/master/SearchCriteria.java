package com.ying.learneyjourney.master;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;


public abstract class SearchCriteria<T> {
    @Getter
    @Setter
    protected String searchText;

    public abstract Specification<T> getSpecification();
}
