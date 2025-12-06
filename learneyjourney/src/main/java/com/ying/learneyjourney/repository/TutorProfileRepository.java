package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.TutorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TutorProfileRepository extends JpaRepository<TutorProfile, UUID>, JpaSpecificationExecutor<TutorProfile> {
}
