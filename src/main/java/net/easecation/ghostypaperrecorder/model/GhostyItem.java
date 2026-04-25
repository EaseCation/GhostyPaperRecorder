package net.easecation.ghostypaperrecorder.model;

import java.util.Objects;

public record GhostyItem(String identifier, int damage, int count) {
    public static final GhostyItem AIR = new GhostyItem("minecraft:air", 0, 0);

    public GhostyItem {
        identifier = Objects.requireNonNull(identifier, "identifier");
        count = Math.max(0, Math.min(255, count));
    }
}
