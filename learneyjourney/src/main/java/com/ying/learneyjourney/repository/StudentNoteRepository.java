package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.StudentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface StudentNoteRepository extends JpaRepository<StudentNote, UUID>, JpaSpecificationExecutor<StudentNote> {
}
