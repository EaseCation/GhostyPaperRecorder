package net.easecation.ghostypaperrecorder.api;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface GhostyRecorderApi {
    RecordingStatus startRecording(RecordingStartRequest request) throws IOException;

    RecordingStopResult stopRecording(String sessionId) throws IOException;

    RecordingStatus status(String sessionId);

    Collection<RecordingStatus> activeSessions();

    boolean isRecording(String sessionId);

    RecordingStatus addParticipant(String sessionId, UUID participant, RecordingPlayerInfo playerInfo);

    void setMetadata(String sessionId, RecordingMetadata metadata);

    void mergeMetadata(String sessionId, RecordingMetadata metadata);

    void recordCustomEvent(String sessionId, JsonObject event);
}
