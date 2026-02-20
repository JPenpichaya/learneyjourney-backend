package com.ying.learneyjourney.service;

import com.ying.learneyjourney.criteria.StudentNoteCriteria;
import com.ying.learneyjourney.dto.StudentNoteDto;
import com.ying.learneyjourney.entity.StudentNote;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.PageCriteria;

import com.ying.learneyjourney.repository.CourseVideoRepository;
import com.ying.learneyjourney.repository.StudentNoteRepository;
import com.ying.learneyjourney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentNoteService implements MasterService<StudentNoteDto, UUID> {
    private final StudentNoteRepository studentNoteRepository;
    private final UserRepository userRepository;
    private final CourseVideoRepository courseVideoRepository;

    @Override
    public StudentNoteDto create(StudentNoteDto dto) {
        StudentNote entity = studentNoteRepository.findById(dto.getId()).orElse(StudentNoteDto.toEntity(dto));
        userRepository.findById(dto.getUserId()).ifPresent(entity::setUser);
        courseVideoRepository.findById(dto.getVideoId()).ifPresent(entity::setCourseVideo);
        studentNoteRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public StudentNoteDto update(UUID uuid, StudentNoteDto dto) {
        StudentNote studentNote = studentNoteRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Student Note not found"));
        studentNote.setContent(dto.getContent());
        studentNote.setImageUrl(dto.getImageUrl());
        studentNote.setVideoAt(dto.getVideoAt());
        studentNoteRepository.save(studentNote);
        return dto;
    }

    @Override
    public StudentNoteDto getById(UUID uuid) {
        return studentNoteRepository.findById(uuid).map(StudentNoteDto::from).orElseThrow(() -> new IllegalArgumentException("Student Note not found"));
    }

    @Override
    public List<StudentNoteDto> getAll() {
        return studentNoteRepository.findAll().stream().map(StudentNoteDto::from).toList();
    }

    @Override
    public Page<StudentNoteDto> getAll(Pageable pageable) {
        return studentNoteRepository.findAll(pageable).map(StudentNoteDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if (!studentNoteRepository.existsById(uuid)) {
            throw new IllegalArgumentException("Student Note not found");
        }
        studentNoteRepository.deleteById(uuid);
    }

    public List<StudentNoteDto> getAllList(PageCriteria<StudentNoteCriteria> conditions) {
        List<StudentNote> all = studentNoteRepository.findAll(conditions.getSpecification());
        return all.stream().map(this::convertToDto).toList();
    }

    private StudentNoteDto convertToDto(StudentNote studentNote) {
        StudentNoteDto from = StudentNoteDto.from(studentNote);
        from.setVideoId(studentNote.getCourseVideo().getId());
        return from;
    }

    public StudentNoteDto createUpdate(StudentNoteDto dto, String userId) {
        StudentNote entity;
        if(dto.getId() != null){
            entity = studentNoteRepository.findById(dto.getId()).orElseThrow(() -> new BusinessException("Student note not found", "STUDENT_NOTE_NOT_FOUND"));
        }else{
            entity = StudentNoteDto.toEntity(dto);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
        entity.setUser(user);
        courseVideoRepository.findById(dto.getVideoId()).ifPresent(entity::setCourseVideo);
        studentNoteRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }
}
