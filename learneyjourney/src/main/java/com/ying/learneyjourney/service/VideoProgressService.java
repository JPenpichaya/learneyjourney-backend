package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.VideoProgressDto;
import com.ying.learneyjourney.entity.VideoProgress;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.master.SearchSpecification;
import com.ying.learneyjourney.repository.VideoProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoProgressService implements MasterService<VideoProgressDto, UUID> {
    private final VideoProgressRepository videoProgressRepository;
    @Override
    public VideoProgressDto create(VideoProgressDto dto) {
        VideoProgress entity = VideoProgressDto.toEntity(dto);
        videoProgressRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public VideoProgressDto update(UUID uuid, VideoProgressDto dto) {
        VideoProgress videoProgress = videoProgressRepository.findById(uuid).orElseThrow(() -> new RuntimeException("VideoProgress not found"));
        videoProgress.setWatchedSeconds(dto.getWatchedSeconds());
        videoProgress.setStatus(dto.getStatus());
        videoProgress.setLastWatchedAt(dto.getLastWatchedAt());
        videoProgressRepository.save(videoProgress);
        return dto;
    }

    @Override
    public VideoProgressDto getById(UUID uuid) {
        return videoProgressRepository.findById(uuid).map(VideoProgressDto::from).orElseThrow(() -> new RuntimeException("VideoProgress not found"));;
    }

    @Override
    public List<VideoProgressDto> getAll() {
        return videoProgressRepository.findAll().stream().map(VideoProgressDto::from).toList();
    }

    @Override
    public Page<VideoProgressDto> getAll(Pageable pageable) {
        return videoProgressRepository.findAll(pageable).map(VideoProgressDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!videoProgressRepository.existsById(uuid)) {
            throw new RuntimeException("VideoProgress not found");
        }
        videoProgressRepository.deleteById(uuid);
    }

    @Override
    public Page<VideoProgressDto> search(List<SearchCriteria> criteriaList, Pageable pageable) {
        SearchSpecification<VideoProgress> specification = new SearchSpecification<>();
        criteriaList.forEach(specification::add);
        return videoProgressRepository.findAll(specification, pageable).map(VideoProgressDto::from);
    }
}
