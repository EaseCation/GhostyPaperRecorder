package net.easecation.ghostypaperrecorder.api;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public record RecordingStatus(
        String sessionId,
        String recordName,
        Path outputPath,
        int tick,
        int playerCount,
        Set<UUID> participants
) {
    public RecordingStatus {
        participants = participants == null ? Set.of() : Set.copyOf(participants);
    }
}
