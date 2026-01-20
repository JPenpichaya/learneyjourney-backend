package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
}
