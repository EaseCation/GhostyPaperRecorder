package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.api.RecordingHackInfo;
import net.easecation.ghostypaperrecorder.api.RecordingMetadata;
import net.easecation.ghostypaperrecorder.api.RecordingPlayerInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilTest {
    @Test
    void writesMetadataWithHackInfo() {
        RecordingHackInfo hackInfo = new RecordingHackInfo(1, 2, true, 3, 4, false, 5);
        RecordingMetadata metadata = new RecordingMetadata(
                "map",
                123,
                "time",
                "map-name",
                "game",
                "Game",
                "stage",
                List.of(new RecordingPlayerInfo("Steve", "Alias", 100, "Device", 1, 2, hackInfo)),
                "show"
        );

        String json = new JsonUtil().toJson(metadata);

        assertTrue(json.contains("\"baseGameVersion\":123"));
        assertTrue(json.contains("\"name\":\"Steve\""));
        assertTrue(json.contains("\"hack\":{\"kbH\":1,\"kbV\":2,\"enableHitbox\":true"));
        assertTrue(json.contains("\"enableSpeed\":false"));
        assertTrue(json.contains("\"speed\":5"));
    }
}
