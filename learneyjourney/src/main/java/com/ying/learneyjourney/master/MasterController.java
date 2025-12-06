package com.ying.learneyjourney.master;

import com.ying.learneyjourney.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Abstract controller – extend this per entity and put @RestController/@RequestMapping on the subclass.
 */
public interface MasterController<D, ID> {

    @PostMapping("/get/{uuid}")
    ResponseEntity<D> getById(@PathVariable ID uuid);

    @PostMapping
    ResponseEntity<Page<D>> getAll(Pageable pageable);
    @PostMapping("/create")
    ResponseEntity<D> create(@RequestBody D body);

    @PostMapping("/update/{uuid}")
    ResponseEntity<D> update(@PathVariable ID uuid, @RequestBody D body);

    @PostMapping("/delete/{uuid}")
    ResponseEntity<Void> delete(@PathVariable ID uuid);

}