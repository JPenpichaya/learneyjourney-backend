package com.ying.learneyjourney.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record SelectedSlot(
        String day,
        String time
) {}
