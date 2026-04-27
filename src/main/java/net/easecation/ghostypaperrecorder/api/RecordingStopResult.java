package net.easecation.ghostypaperrecorder.api;

import java.nio.file.Path;
import java.util.List;

public record RecordingStopResult(
        String sessionId,
        String recordName,
        Path outputPath,
        int lastTick,
        List<String> playerNames,
        RecordingMetadata metadata
) {
    public RecordingStopResult {
        playerNames = playerNames == null ? List.of() : List.copyOf(playerNames);
    }
}
