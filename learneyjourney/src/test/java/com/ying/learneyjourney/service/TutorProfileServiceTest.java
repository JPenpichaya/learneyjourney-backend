package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.TutorProfilesDto;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import com.ying.learneyjourney.repository.UserRepository;
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
class TutorProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TutorProfileRepository tutorProfileRepository;

    @InjectMocks private TutorProfileService service;

    private UUID profileId;
    private String userId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
        userId = "user-1";
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSaveTutorProfile_whenUserExists() {
        // arrange
        TutorProfilesDto dto = new TutorProfilesDto();
        dto.setUserId(userId);

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(tutorProfileRepository.save(any(TutorProfile.class))).thenAnswer(inv -> {
            TutorProfile p = inv.getArgument(0);
            p.setId(profileId); // simulate JPA id generation
            return p;
        });

        // act
        TutorProfilesDto out = service.create(dto);

        // assert
        assertNotNull(out);
        assertEquals(profileId, out.getId());

        verify(userRepository).findById(userId);
        verify(tutorProfileRepository).save(any(TutorProfile.class));
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        TutorProfilesDto dto = new TutorProfilesDto();
        dto.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(dto)
        );

        assertEquals("User not found", ex.getMessage());
        verify(tutorProfileRepository, never()).save(any());
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateBio_andSave_whenProfileExists() {
        // arrange
        TutorProfile existing = new TutorProfile();
        existing.setId(profileId);
        existing.setBio("old");

        when(tutorProfileRepository.findById(profileId)).thenReturn(Optional.of(existing));
        when(tutorProfileRepository.save(any(TutorProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TutorProfilesDto dto = new TutorProfilesDto();
        dto.setBio("new bio");

        // act
        TutorProfilesDto out = service.update(profileId, dto);

        // assert
        assertSame(dto, out); // service returns dto
        assertEquals("new bio", existing.getBio());
        verify(tutorProfileRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenProfileNotFound() {
        when(tutorProfileRepository.findById(profileId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(profileId, new TutorProfilesDto())
        );

        assertEquals("Tutor Profile not found", ex.getMessage());
        verify(tutorProfileRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldReturnDto_whenFound() {
        TutorProfile profile = new TutorProfile();
        profile.setId(profileId);
        profile.setUser(new User());

        when(tutorProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        TutorProfilesDto out = service.getById(profileId);

        assertNotNull(out);
        assertEquals(profileId, out.getId());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(tutorProfileRepository.findById(profileId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(profileId)
        );

        assertEquals("Tutor Profile not found", ex.getMessage());
    }

    // -------------------- getAll --------------------

    @Test
    void getAll_shouldMapAllProfiles() {
        TutorProfile p1 = new TutorProfile(); p1.setId(UUID.randomUUID()); p1.setUser(new User());
        TutorProfile p2 = new TutorProfile(); p2.setId(UUID.randomUUID()); p2.setUser(new User());

        when(tutorProfileRepository.findAll()).thenReturn(List.of(p1, p2));

        List<TutorProfilesDto> out = service.getAll();

        assertEquals(2, out.size());
        assertEquals(p1.getId(), out.get(0).getId());
        assertEquals(p2.getId(), out.get(1).getId());
    }

    // -------------------- getAll(Pageable) --------------------

    @Test
    void getAllPage_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);

        TutorProfile p1 = new TutorProfile(); p1.setId(UUID.randomUUID()); p1.setUser(new User());
        Page<TutorProfile> page = new PageImpl<>(List.of(p1), pageable, 1);

        when(tutorProfileRepository.findAll(pageable)).thenReturn(page);

        Page<TutorProfilesDto> out = service.getAll(pageable);

        assertEquals(1, out.getTotalElements());
        assertEquals(p1.getId(), out.getContent().get(0).getId());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(tutorProfileRepository.existsById(profileId)).thenReturn(true);

        service.deleteById(profileId);

        verify(tutorProfileRepository).deleteById(profileId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        when(tutorProfileRepository.existsById(profileId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteById(profileId)
        );

        assertEquals("Tutor Profile not found", ex.getMessage());
        verify(tutorProfileRepository, never()).deleteById(any());
    }
}
