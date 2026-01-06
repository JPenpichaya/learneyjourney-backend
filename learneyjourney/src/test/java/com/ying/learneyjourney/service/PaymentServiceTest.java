package com.ying.learneyjourney.service;
import com.stripe.model.checkout.Session;
import com.ying.learneyjourney.constaint.EnumEnrollmentStatus;
import com.ying.learneyjourney.dto.EnrollmentDto;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.repository.PurchaseRepository;
import com.ying.learneyjourney.service.EnrollmentService;
import com.ying.learneyjourney.service.PaymentService;
import com.ying.learneyjourney.service.PostEnrollmentAsyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PurchaseRepository purchaseRepository;
    @Mock private EnrollmentService enrollmentService;
    @Mock private PostEnrollmentAsyncService postEnrollmentAsyncService;

    @InjectMocks private PaymentService paymentService;

    private String userId;
    private String courseIdStr;
    private String sessionId;
    private String paymentIntentId;

    @BeforeEach
    void setUp() {
        userId = "user-1";
        courseIdStr = UUID.randomUUID().toString();
        sessionId = "cs_test_123";
        paymentIntentId = "pi_123";
    }

    // -------------------- handleCheckoutSessionCompleted --------------------

    @Test
    void handleCheckoutSessionCompleted_shouldReturnEarly_whenSessionAlreadyProcessed() {
        // arrange
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(sessionId);
        when(purchaseRepository.findByStripeSessionId(sessionId))
                .thenReturn(Optional.of(new Purchase()));

        // act
        paymentService.handleCheckoutSessionCompleted(session);

        // assert
        verify(purchaseRepository, never()).save(any());
        verifyNoInteractions(enrollmentService, postEnrollmentAsyncService);
    }

    @Test
    void handleCheckoutSessionCompleted_shouldSavePurchase_whenNewSession() {
        // arrange
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(sessionId);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", userId);
        metadata.put("courseId", courseIdStr);

        when(session.getMetadata()).thenReturn(metadata);
        when(session.getAmountTotal()).thenReturn(19900L);
        when(session.getCurrency()).thenReturn("thb");
        when(session.getPaymentIntent()).thenReturn(paymentIntentId);

        when(purchaseRepository.findByStripeSessionId(sessionId)).thenReturn(Optional.empty());

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);

        // act
        paymentService.handleCheckoutSessionCompleted(session);

        // assert
        verify(purchaseRepository).save(captor.capture());
        Purchase saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(courseIdStr, saved.getCourseId());
        assertEquals(19900L, saved.getAmount());
        assertEquals("thb", saved.getCurrency());
        assertEquals(sessionId, saved.getStripeSessionId());
        assertEquals(paymentIntentId, saved.getStripePaymentIntentId());
        assertEquals("PAID", saved.getStatus());
        assertNotNull(saved.getPurchasedAt());
    }

    // -------------------- saveCoursePurchaseFromWebhook --------------------

    @Test
    void saveCoursePurchaseFromWebhook_shouldReturnEarly_whenEventAlreadyProcessed() {
        // arrange
        String eventId = "evt_1";
        when(purchaseRepository.exist_StripEvenId(eventId)).thenReturn(true);

        // act
        paymentService.saveCoursePurchaseFromWebhook(
                sessionId, paymentIntentId, userId, courseIdStr, 100L, "thb", eventId
        );

        // assert
        verify(purchaseRepository, never()).save(any());
        verify(enrollmentService, never()).create(any());
        verify(postEnrollmentAsyncService, never()).sendingEmailAfterEnrolled(any(), any(), any());
    }

    @Test
    void saveCoursePurchaseFromWebhook_shouldReturnEarly_whenSessionAlreadyExists() {
        // arrange
        String eventId = "evt_1";
        when(purchaseRepository.exist_StripEvenId(eventId)).thenReturn(false);
        when(purchaseRepository.findByStripeSessionId(sessionId)).thenReturn(Optional.of(new Purchase()));

        // act
        paymentService.saveCoursePurchaseFromWebhook(
                sessionId, paymentIntentId, userId, courseIdStr, 100L, "thb", eventId
        );

        // assert
        verify(purchaseRepository, never()).save(any());
        verify(enrollmentService, never()).create(any());
        verify(postEnrollmentAsyncService, never()).sendingEmailAfterEnrolled(any(), any(), any());
    }

    @Test
    void saveCoursePurchaseFromWebhook_shouldSavePurchase_thenCreateEnrollment_thenSendEmail_whenNew() {
        // arrange
        String eventId = "evt_1";
        Long amount = 100L;
        String currency = "thb";

        when(purchaseRepository.exist_StripEvenId(eventId)).thenReturn(false);
        when(purchaseRepository.findByStripeSessionId(sessionId)).thenReturn(Optional.empty());

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        ArgumentCaptor<EnrollmentDto> enrollmentCaptor = ArgumentCaptor.forClass(EnrollmentDto.class);

        // act
        paymentService.saveCoursePurchaseFromWebhook(
                sessionId, paymentIntentId, userId, courseIdStr, amount, currency, eventId
        );

        // assert purchase saved
        verify(purchaseRepository).save(purchaseCaptor.capture());
        Purchase saved = purchaseCaptor.getValue();

        assertEquals(sessionId, saved.getStripeSessionId());
        assertEquals(paymentIntentId, saved.getStripePaymentIntentId());
        assertEquals(userId, saved.getUserId());
        assertEquals(courseIdStr, saved.getCourseId());
        assertEquals(amount, saved.getAmount());
        assertEquals(currency, saved.getCurrency());
        assertEquals("PAID", saved.getStatus());
        assertEquals(eventId, saved.getStripeEventId());
        assertNotNull(saved.getPurchasedAt());

        // assert enrollment created
        verify(enrollmentService).create(enrollmentCaptor.capture());
        EnrollmentDto created = enrollmentCaptor.getValue();

        assertEquals(userId, created.getUserId());
        assertEquals(UUID.fromString(courseIdStr), created.getCourseId());
        assertEquals(0, created.getProgress());
        assertEquals(EnumEnrollmentStatus.NOT_START, created.getStatus());

        // assert email triggered
        verify(postEnrollmentAsyncService).sendingEmailAfterEnrolled(
                eq(userId),
                eq(UUID.fromString(courseIdStr)),
                eq(sessionId)
        );
    }
}
