package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.api.RecordingMetadata;
import net.easecation.ghostypaperrecorder.api.RecordingPlayerInfo;
import net.easecation.ghostypaperrecorder.recording.PlayerRecording;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostyPackWriterTest {
    @Test
    void writesRequiredGhostyPackEntries() throws Exception {
        GhostyPackWriter packWriter = new GhostyPackWriter();
        PlayerRecording player = new PlayerRecording("Steve", 123L, 0);
        player.recordAnimate(3, 1, 0.0f);
        RecordingMetadata metadata = RecordingMetadata.createDefault("test", "world", List.of(RecordingPlayerInfo.of("Steve", "Steve")));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        packWriter.write(outputStream, 20, List.of(player), metadata);

        Map<String, byte[]> entries = new HashMap<>();
        try (TarArchiveInputStream tar = new TarArchiveInputStream(new ZstdCompressorInputStream(new ByteArrayInputStream(outputStream.toByteArray())))) {
            org.apache.commons.compress.archivers.tar.TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                entries.put(entry.getName(), tar.readAllBytes());
            }
        }

        assertTrue(entries.containsKey("level_record.ecrecl"));
        String playerEntryName = entries.keySet().stream()
                .filter(name -> name.startsWith("player/player_record_0_Steve"))
                .findFirst()
                .orElseThrow();
        assertTrue(entries.containsKey("metadata.json"));

        LittleEndianRecordReader levelReader = new LittleEndianRecordReader(entries.get("level_record.ecrecl"));
        assertEquals(GhostyConstants.LEVEL_VERSION_2, levelReader.u8());
        assertEquals(0, levelReader.i32());
        assertEquals(2, levelReader.unsignedVarInt());
        assertEquals(0, levelReader.unsignedVarInt());
        assertEquals(GhostyConstants.LEVEL_UPDATE_TIME, levelReader.u8());
        assertEquals(0, levelReader.varInt());
        assertEquals(20, levelReader.unsignedVarInt());
        assertEquals(GhostyConstants.LEVEL_UPDATE_TIME, levelReader.u8());
        assertEquals(20, levelReader.varInt());
        assertEquals(entries.get("level_record.ecrecl").length, levelReader.offset());

        LittleEndianRecordReader playerReader = new LittleEndianRecordReader(entries.get(playerEntryName));
        assertEquals(GhostyConstants.PLAYER_RECORD_SKINLESS_V3, playerReader.u8());
        assertEquals(0, playerReader.i32());
        assertEquals("Steve", playerReader.string());
        assertEquals(123L, playerReader.unsignedVarLong());
        assertEquals(1, playerReader.unsignedVarInt());
        assertEquals(3, playerReader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_ANIMATE, playerReader.u8());
        assertEquals(1, playerReader.varInt());
        assertEquals(0.0f, playerReader.f32());
        assertEquals(entries.get(playerEntryName).length, playerReader.offset());

        String metadataJson = new String(entries.get("metadata.json"), StandardCharsets.UTF_8);
        assertTrue(metadataJson.contains("\"map\":\"world\""));
        assertTrue(metadataJson.contains("\"name\":\"Steve\""));
    }
}
