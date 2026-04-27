package net.easecation.ghostypaperrecorder.api;

public record RecordingHackInfo(
        int kbH,
        int kbV,
        boolean enableHitbox,
        int hitboxV,
        int hitboxH,
        boolean enableSpeed,
        int speed
) {
}
