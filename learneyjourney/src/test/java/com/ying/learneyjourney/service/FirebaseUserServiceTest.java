package com.ying.learneyjourney.service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ying.learneyjourney.constaint.EnumUserRoles;
import com.ying.learneyjourney.dto.LoginAttemptsDto;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private LoginAttemptsService loginAttemptsService;
    @Mock private HttpServletRequest request;

    @InjectMocks private FirebaseUserService service;

    @Test
    void authenticateAndSyncUser_shouldUpdateExistingUser_andRecordLoginAttempt() throws Exception {
        // arrange
        String idToken = "token";
        String uid = "uid-1";
        String email = "new@mail.com";
        String name = "New Name";
        String picture = "http://pic";

        User existing = new User();
        existing.setId(uid);
        existing.setEmail("old@mail.com");
        existing.setDisplayName("Old Name");
        existing.setPhotoUrl("oldPic");

        when(userRepository.findById(uid)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // request headers
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        // firebase mocks (static)
        FirebaseToken decoded = mock(FirebaseToken.class);
        when(decoded.getUid()).thenReturn(uid);
        when(decoded.getEmail()).thenReturn(email);

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", name);
        claims.put("picture", picture);
        when(decoded.getClaims()).thenReturn(claims);

        FirebaseAuth auth = mock(FirebaseAuth.class);
        when(auth.verifyIdToken(idToken)).thenReturn(decoded);

        try (MockedStatic<FirebaseAuth> mocked = Mockito.mockStatic(FirebaseAuth.class)) {
            mocked.when(FirebaseAuth::getInstance).thenReturn(auth);

            // act
            User out = service.authenticateAndSyncUser(idToken, request);

            // assert - user updated + saved
            assertNotNull(out);
            assertEquals(uid, out.getId());
            assertEquals(email, out.getEmail());
            assertEquals(name, out.getDisplayName());
            assertEquals(picture, out.getPhotoUrl());

            verify(userRepository).save(existing);

            // assert - login attempt recorded
            ArgumentCaptor<LoginAttemptsDto> captor = ArgumentCaptor.forClass(LoginAttemptsDto.class);
            verify(loginAttemptsService).recordLoginAttempt(captor.capture());

            LoginAttemptsDto attempt = captor.getValue();
            assertEquals(uid, attempt.getUserId());
            assertTrue(attempt.isSuccess());
            assertEquals("1.2.3.4", attempt.getIpAddress()); // uses X-Forwarded-For
            assertEquals("JUnit", attempt.getUserAgent());
            assertNotNull(attempt.getAttemptTime());
        }
    }

    @Test
    void authenticateAndSyncUser_shouldCreateNewUser_andRecordLoginAttempt() throws Exception {
        // arrange
        String idToken = "token";
        String uid = "uid-new";
        String email = "u@mail.com";
        String name = "User";
        String picture = "http://pic";

        when(userRepository.findById(uid)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        FirebaseToken decoded = mock(FirebaseToken.class);
        when(decoded.getUid()).thenReturn(uid);
        when(decoded.getEmail()).thenReturn(email);

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", name);
        claims.put("picture", picture);
        when(decoded.getClaims()).thenReturn(claims);

        FirebaseAuth auth = mock(FirebaseAuth.class);
        when(auth.verifyIdToken(idToken)).thenReturn(decoded);

        try (MockedStatic<FirebaseAuth> mocked = Mockito.mockStatic(FirebaseAuth.class)) {
            mocked.when(FirebaseAuth::getInstance).thenReturn(auth);

            // act
            User out = service.authenticateAndSyncUser(idToken, request);

            // assert
            assertNotNull(out);
            assertEquals(uid, out.getId());
            assertEquals(email, out.getEmail());
            assertEquals(name, out.getDisplayName());
            assertEquals(picture, out.getPhotoUrl());
            assertEquals(EnumUserRoles.STUDENT, out.getRole());

            // recordLoginAttempts uses remoteAddr if XFF missing
            ArgumentCaptor<LoginAttemptsDto> captor = ArgumentCaptor.forClass(LoginAttemptsDto.class);
            verify(loginAttemptsService).recordLoginAttempt(captor.capture());
            assertEquals("9.9.9.9", captor.getValue().getIpAddress());
        }
    }

    @Test
    void recordLoginAttempts_shouldUseRemoteAddr_whenXForwardedForEmpty() {
        // arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Agent");

        // act
        service.recordLoginAttempts(request, "uid", true);

        // assert
        ArgumentCaptor<LoginAttemptsDto> captor = ArgumentCaptor.forClass(LoginAttemptsDto.class);
        verify(loginAttemptsService).recordLoginAttempt(captor.capture());

        LoginAttemptsDto dto = captor.getValue();
        assertEquals("10.0.0.1", dto.getIpAddress());
        assertEquals("Agent", dto.getUserAgent());
        assertEquals("uid", dto.getUserId());
        assertTrue(dto.isSuccess());
        assertNotNull(dto.getAttemptTime());
    }
}
