package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.UserDto;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService service;

    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user-1";
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSaveUserEntity() {
        UserDto dto = new UserDto();
        dto.setId(userId);
        dto.setEmail("a@b.com");
        dto.setDisplayName("A");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto out = service.create(dto);

        assertSame(dto, out);
        verify(userRepository).save(any(User.class));
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateFields_andSave_whenUserExists() {
        User existing = new User();
        existing.setId(userId);
        existing.setEmail("old@b.com");
        existing.setDisplayName("Old");
        existing.setPhotoUrl("old.png");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto dto = new UserDto();
        dto.setEmail("new@b.com");
        dto.setDisplayName("New");
        dto.setPhotoUrl("new.png");

        UserDto out = service.update(userId, dto);

        assertSame(dto, out);
        assertEquals("new@b.com", existing.getEmail());
        assertEquals("New", existing.getDisplayName());
        assertEquals("new.png", existing.getPhotoUrl());

        verify(userRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(userId, new UserDto())
        );

        assertEquals("User not found with id: " + userId, ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(userId)
        );

        assertEquals("User not found with id: " + userId, ex.getMessage());
    }

    @Test
    void getById_currentlyReturnsNull_whenUserExists_bug() {
        // This test demonstrates the current bug in your service:
        // getById(...) returns null instead of UserDto.from(user)
        User existing = new User();
        existing.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

        UserDto out = service.getById(userId);

        assertNull(out, "Bug: getById currently returns null even when user exists");
    }

    // -------------------- getAll --------------------

    @Test
    void getAll_shouldMapAllUsers() {
        User u1 = new User(); u1.setId("u1");
        User u2 = new User(); u2.setId("u2");

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserDto> out = service.getAll();

        assertEquals(2, out.size());
        assertEquals("u1", out.get(0).getId());
        assertEquals("u2", out.get(1).getId());
    }

    // -------------------- getAll(Pageable) --------------------

    @Test
    void getAllPage_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        User u1 = new User(); u1.setId("u1");

        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(u1), pageable, 1));

        Page<UserDto> out = service.getAll(pageable);

        assertEquals(1, out.getTotalElements());
        assertEquals("u1", out.getContent().get(0).getId());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(userRepository.existsById(userId)).thenReturn(true);

        service.deleteById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        when(userRepository.existsById(userId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteById(userId)
        );

        assertEquals("User not found with id: " + userId, ex.getMessage());
        verify(userRepository, never()).deleteById(any());
    }
}
