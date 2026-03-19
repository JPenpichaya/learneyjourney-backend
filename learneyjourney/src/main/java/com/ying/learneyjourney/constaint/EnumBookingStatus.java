package com.ying.learneyjourney.constaint;

public enum EnumBookingStatus {
    PENDING,        // created but not confirmed
    CONFIRMED,      // accepted by tutor
    CANCELLED,      // cancelled before session
    COMPLETED,      // finished successfully
    NO_SHOW         // student or tutor didn't show up
}
