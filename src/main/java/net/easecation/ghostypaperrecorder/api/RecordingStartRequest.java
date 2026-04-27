package net.easecation.ghostypaperrecorder.api;

import java.util.Set;
import java.util.UUID;

public record RecordingStartRequest(
        String sessionId,
        String recordName,
        Set<UUID> participants,
        RecordingMetadata metadata,
        String outputFileName
) {
    public RecordingStartRequest {
        participants = participants == null ? Set.of() : Set.copyOf(participants);
    }

    public static RecordingStartRequest of(String sessionId, String recordName, Set<UUID> participants) {
        return new RecordingStartRequest(sessionId, recordName, participants, null, null);
    }
}
