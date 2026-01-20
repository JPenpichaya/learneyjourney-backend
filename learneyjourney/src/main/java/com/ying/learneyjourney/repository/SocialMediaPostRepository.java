package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.SocialMediaPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface SocialMediaPostRepository extends JpaRepository<SocialMediaPost, UUID>, JpaSpecificationExecutor<SocialMediaPost> {
}
