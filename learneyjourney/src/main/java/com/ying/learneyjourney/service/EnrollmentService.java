package com.ying.learneyjourney.service;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.EnrollmentDto;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.Enrollment;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;

import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.EnrollmentRepository;
import com.ying.learneyjourney.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class EnrollmentService implements MasterService<EnrollmentDto, UUID> {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FirebaseAuthUtil firebaseAuthUtil;
    @Override
    public EnrollmentDto create(EnrollmentDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Course course = courseRepository.findById(dto.getCourseId()).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Enrollment entity = EnrollmentDto.toEntity(dto);
        entity.setUser(user);
        entity.setCourse(course);
        enrollmentRepository.save(entity);
        dto.setId(entity.getId());
        dto.setUserId(user.getId());
        dto.setCourseId(course.getId());
        return dto;
    }

    @Override
    public EnrollmentDto update(UUID uuid, EnrollmentDto dto) {
        Enrollment enrollment = enrollmentRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        enrollment.setStatus(dto.getStatus());
        enrollment.setProgress(dto.getProgress());
        enrollment.setCompletionAt(dto.getCompletionAt());
        enrollment.setLastAccessedAt(dto.getLastAccessedAt());
        enrollmentRepository.save(enrollment);
        dto.setStatus(enrollment.getStatus());
        dto.setProgress(enrollment.getProgress());
        dto.setCompletionAt(enrollment.getCompletionAt());
        dto.setLastAccessedAt(enrollment.getLastAccessedAt());
        return dto;
    }

    @Override
    public EnrollmentDto getById(UUID uuid) {
        Enrollment enrollment = enrollmentRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));
        return EnrollmentDto.from(enrollment);
    }

    @Override
    public List<EnrollmentDto> getAll() {
        return enrollmentRepository.findAll().stream().map(EnrollmentDto::from).toList();
    }

    @Override
    public Page<EnrollmentDto> getAll(Pageable pageable) {
        return enrollmentRepository.findAll(pageable).map(EnrollmentDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!enrollmentRepository.existsById(uuid)) throw new IllegalArgumentException("Enrollment not found");
        enrollmentRepository.deleteById(uuid);
    }

    public List<EnrollmentDto> getEnrollmentsByUserId(String token) throws Exception {
        String userIdFromToken = firebaseAuthUtil.getUserIdFromToken(token);
        return enrollmentRepository.findBy_UserId(userIdFromToken).stream().map(EnrollmentDto::from).toList();
    }

}
