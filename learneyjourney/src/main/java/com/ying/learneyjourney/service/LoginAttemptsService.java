package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.LoginAttemptsDto;
import com.ying.learneyjourney.entity.LoginAttempts;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.repository.LoginAttemptsRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LoginAttemptsService {
    private final LoginAttemptsRepository loginAttemptsRepository;
    public void recordLoginAttempt(LoginAttemptsDto dto, User user){
        LoginAttempts entity = LoginAttemptsDto.toEntity(dto, user);
        loginAttemptsRepository.save(entity);
    }

}
