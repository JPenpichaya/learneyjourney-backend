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

    @GetMapping("/get/{id}")
    ResponseEntity<D> getById(@PathVariable ID id);

    @GetMapping
    ResponseEntity<Page<D>> getAll(Pageable pageable);
    @PostMapping("/create")
    ResponseEntity<D> create(@RequestBody D body);

    @PutMapping("/update/{id}")
    ResponseEntity<D> update(@PathVariable ID id, @RequestBody D body);

    ResponseEntity<UserDto> update(String s, UserDto body);

    @DeleteMapping("/delete/{id}")
    ResponseEntity<Void> delete(@PathVariable ID id);

    @PostMapping("/search")
    ResponseEntity<Page<D>> search(@RequestBody List<SearchCriteria> criteria,
                                   Pageable pageable);
}