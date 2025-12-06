package com.ying.learneyjourney.master;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
@Getter
@Setter
public class PageCriteria<C extends SearchCriteria<?>>{
    private C condition;
    private int pageNumber;
    private int pageSize;
    private Sort.Direction direction = Sort.DEFAULT_DIRECTION;
    private String sortBy;

    public PageRequest generatePageRequest() {
        if (sortBy != null && !sortBy.isEmpty()) {
            return PageRequest.of(pageNumber, pageSize, direction, sortBy);
        } else {
            return PageRequest.of(pageNumber, pageSize);
        }
    }

    public Specification getSpecification() {
        return condition.getSpecification();
    }
}
