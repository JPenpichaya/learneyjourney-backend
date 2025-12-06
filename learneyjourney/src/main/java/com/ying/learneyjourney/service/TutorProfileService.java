package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.TutorProfilesDto;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.master.SearchSpecification;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import com.ying.learneyjourney.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
@AllArgsConstructor
public class TutorProfileService implements MasterService<TutorProfilesDto, UUID> {
    private final UserRepository userRepository;
    private final TutorProfileRepository tutorProfileRepository;
    @Override
    public TutorProfilesDto create(TutorProfilesDto dto) {
        Optional<User> byId = userRepository.findById(dto.getUserId());
        byId.orElseThrow(() -> new IllegalArgumentException("User not found"));
        TutorProfile entity = TutorProfilesDto.toEntity(dto, byId.get());
        tutorProfileRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public TutorProfilesDto update(UUID uuid, TutorProfilesDto dto) {
        Optional<TutorProfile> byId = tutorProfileRepository.findById(uuid);
        byId.orElseThrow(() -> new IllegalArgumentException("Tutor Profile not found"));
        TutorProfile entity = byId.get();
        entity.setBio(dto.getBio());
        tutorProfileRepository.save(entity);
        return dto;
    }

    @Override
    public TutorProfilesDto getById(UUID uuid) {
        Optional<TutorProfile> byId = tutorProfileRepository.findById(uuid);
        return byId.map(TutorProfilesDto::from).orElseThrow(() -> new IllegalArgumentException("Tutor Profile not found"));
    }

    @Override
    public List<TutorProfilesDto> getAll() {
        return tutorProfileRepository.findAll().stream().map(TutorProfilesDto::from).toList();
    }

    @Override
    public Page<TutorProfilesDto> getAll(Pageable pageable) {
        Page<TutorProfile> all = tutorProfileRepository.findAll(pageable);
        return all.map(TutorProfilesDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!tutorProfileRepository.existsById(uuid)) throw new IllegalArgumentException("Tutor Profile not found");
        tutorProfileRepository.deleteById(uuid);
    }

    @Override
    public Page<TutorProfilesDto> search(List<SearchCriteria> criteriaList, Pageable pageable) {
        SearchSpecification<TutorProfile> specification = new SearchSpecification<>();
        criteriaList.forEach(specification::add);
        Page<TutorProfile> all = tutorProfileRepository.findAll(specification, pageable);
        return all.map(TutorProfilesDto::from);
    }
}
