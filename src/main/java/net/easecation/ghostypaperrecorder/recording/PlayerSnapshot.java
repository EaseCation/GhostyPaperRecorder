package net.easecation.ghostypaperrecorder.recording;

import net.easecation.ghostypaperrecorder.model.GhostyItem;

public record PlayerSnapshot(
        double x,
        double y,
        double z,
        double yaw,
        double pitch,
        String nameTag,
        GhostyItem hand,
        GhostyItem helmet,
        GhostyItem chestplate,
        GhostyItem leggings,
        GhostyItem boots,
        GhostyItem offhand,
        long dataFlags,
        int ping
) {
}
