package net.easecation.ghostypaperrecorder.api;

import java.util.Set;
import java.util.UUID;
import java.util.Map;

public record RecordingStartRequest(
        String sessionId,
        String recordName,
        Set<UUID> participants,
        RecordingMetadata metadata,
        String outputFileName,
        Map<UUID, RecordingPlayerInfo> playerIdentities
) {
    public RecordingStartRequest {
        participants = participants == null ? Set.of() : Set.copyOf(participants);
        playerIdentities = playerIdentities == null ? Map.of() : Map.copyOf(playerIdentities);
    }

    public RecordingStartRequest(String sessionId, String recordName, Set<UUID> participants,
                                 RecordingMetadata metadata, String outputFileName) {
        this(sessionId, recordName, participants, metadata, outputFileName, Map.of());
    }

    public static RecordingStartRequest of(String sessionId, String recordName, Set<UUID> participants) {
        return new RecordingStartRequest(sessionId, recordName, participants, null, null);
    }
}
