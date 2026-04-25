package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.PlaybackMetadata;
import net.easecation.ghostypaperrecorder.model.PlayerInfo;
import net.easecation.ghostypaperrecorder.recording.PlayerRecording;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GhostyPackWriterTest {
    @Test
    void writesRequiredGhostyPackEntries() throws Exception {
        GhostyPackWriter packWriter = new GhostyPackWriter();
        PlayerRecording player = new PlayerRecording("Steve", 123L, 0);
        PlaybackMetadata metadata = PlaybackMetadata.create("test", "world", List.of(PlayerInfo.of("Steve", "Steve")));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        packWriter.write(outputStream, 20, List.of(player), metadata);

        Set<String> entries = new HashSet<>();
        try (TarArchiveInputStream tar = new TarArchiveInputStream(new ZstdCompressorInputStream(new ByteArrayInputStream(outputStream.toByteArray())))) {
            org.apache.commons.compress.archivers.tar.TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("level_record.ecrecl"));
        assertTrue(entries.stream().anyMatch(name -> name.startsWith("player/player_record_0_Steve")));
        assertTrue(entries.contains("metadata.json"));
    }
}
