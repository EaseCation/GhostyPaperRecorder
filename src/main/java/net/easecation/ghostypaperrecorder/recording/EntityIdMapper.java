package net.easecation.ghostypaperrecorder.recording;

import net.easecation.ghostypaperrecorder.format.GhostyBinaryWriter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class EntityIdMapper {
    private final Map<UUID, Long> ids = new LinkedHashMap<>();

    public long id(UUID uuid) {
        return ids.computeIfAbsent(uuid, GhostyBinaryWriter::stableEntityId);
    }
}
