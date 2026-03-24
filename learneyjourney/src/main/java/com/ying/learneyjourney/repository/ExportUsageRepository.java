package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.ExportUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExportUsageRepository extends JpaRepository<ExportUsage, UUID> {
    long countByUserId(String userId);
}
