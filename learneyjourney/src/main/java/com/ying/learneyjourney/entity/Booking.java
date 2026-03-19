package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumBookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "booking",
        indexes = {
                @Index(name = "idx_booking_student_id", columnList = "student_id"),
                @Index(name = "idx_booking_tutor_id", columnList = "tutor_id"),
                @Index(name = "idx_booking_status", columnList = "status"),
                @Index(name = "idx_booking_start_time", columnList = "start_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    // 👤 Student who booked
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // 👨‍🏫 Tutor being booked
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorProfile tutor;

    // 📅 Session start
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // 📅 Session end
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // 🧾 Optional note from student
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // 💰 Price at time of booking (important for history)
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    // 📊 Status of booking lifecycle
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnumBookingStatus status = EnumBookingStatus.PENDING;

    // 🔗 Optional: meeting link (Zoom / Google Meet / LiveKit)
    @Column(name = "meeting_url")
    private String meetingUrl;

}