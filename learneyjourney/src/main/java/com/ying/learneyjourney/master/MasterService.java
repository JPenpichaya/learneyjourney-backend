package com.ying.learneyjourney.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MasterService<D, ID> {

    D create(D dto);

    D update(ID id, D dto);

    D getById(ID id);

    List<D> getAll();

    Page<D> getAll(Pageable pageable);

    void deleteById(ID id);

}