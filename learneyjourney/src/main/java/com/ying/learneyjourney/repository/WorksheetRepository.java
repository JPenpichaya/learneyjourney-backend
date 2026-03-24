package com.ying.learneyjourney.repository;

import com.google.api.gax.paging.Page;
import com.ying.learneyjourney.entity.Worksheet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface WorksheetRepository extends JpaRepository<Worksheet, UUID> {
//    Page<Worksheet> findByUserIdAndDeletedFalse(String userId, Pageable pageable);
//    Page<Worksheet> findByUserIdAndDeletedFalseAndTitleContainingIgnoreCase(String userId, String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "versions")
    Optional<Worksheet> findByIdAndUserIdAndDeletedFalse(UUID id, String userId);
}
