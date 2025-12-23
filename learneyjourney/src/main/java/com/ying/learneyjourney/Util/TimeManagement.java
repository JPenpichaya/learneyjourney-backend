package com.ying.learneyjourney.Util;

public class TimeManagement {
    public static String formatSecondsToHMS(int totalSeconds) {
        int s = Math.max(0, totalSeconds);

        int hours = s / 3600;
        int minutes = (s % 3600) / 60;
        int seconds = s % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);

    }
}
