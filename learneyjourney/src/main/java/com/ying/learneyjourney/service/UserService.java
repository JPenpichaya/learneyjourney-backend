package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.UserDto;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements MasterService<UserDto, String> {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto dto) {
        userRepository.save(UserDto.toEntity(dto));
        return dto;
    }

    @Override
    public UserDto update(String s, UserDto dto) {
        Optional<User> byId = userRepository.findById(s);
        if(byId.isEmpty()) throw new IllegalArgumentException("User not found with id: " + s);
        byId.ifPresent(existingUser -> {
            existingUser.setEmail(dto.getEmail());
            existingUser.setDisplayName(dto.getDisplayName());
            existingUser.setPhotoUrl(dto.getPhotoUrl());
            userRepository.save(existingUser);
        });
        return dto;
    }

    @Override
    public UserDto getById(String s) {
        Optional<User> byId = userRepository.findById(s);
        if(byId.isEmpty()) throw new IllegalArgumentException("User not found with id: " + s);
        return null;
    }

    @Override
    public List<UserDto> getAll() {
        List<User> all = userRepository.findAll();
        return all.stream().map(UserDto::from).toList();
    }

    @Override
    public Page<UserDto> getAll(Pageable pageable) {
        Page<User> all = userRepository.findAll(pageable);
        return all.map(UserDto::from);
    }

    @Override
    public void deleteById(String s) {
        if(!userRepository.existsById(s)) throw new IllegalArgumentException("User not found with id: " + s);
        userRepository.deleteById(s);
    }

}
