package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.WorksheetVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorksheetVersionRepository extends JpaRepository<WorksheetVersion, UUID> {
}
