package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.StudentNoteDto;
import com.ying.learneyjourney.entity.StudentNote;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;

import com.ying.learneyjourney.repository.StudentNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentNoteService implements MasterService<StudentNoteDto, UUID> {
    private final StudentNoteRepository studentNoteRepository;
    @Override
    public StudentNoteDto create(StudentNoteDto dto) {
        StudentNote entity = StudentNoteDto.toEntity(dto);
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
        if(!studentNoteRepository.existsById(uuid)) {
            throw new IllegalArgumentException("Student Note not found");
        }
        studentNoteRepository.deleteById(uuid);
    }
}
