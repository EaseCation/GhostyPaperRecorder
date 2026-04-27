package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.api.RecordingMetadata;
import net.easecation.ghostypaperrecorder.recording.PlayerRecording;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public final class GhostyPackWriter {
    private final GhostyRecordWriter recordWriter = new GhostyRecordWriter();
    private final JsonUtil jsonUtil = new JsonUtil();

    public void write(OutputStream outputStream, int lastTick, Collection<PlayerRecording> players, RecordingMetadata metadata) throws IOException {
        try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new ZstdCompressorOutputStream(outputStream, 19))) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            insert(tar, "level_record.ecrecl", recordWriter.writeLevelRecord(lastTick));
            int index = 0;
            for (PlayerRecording player : players) {
                insert(tar, "player/player_record_" + index + "_" + safeName(player.playerName()) + ".ecrecp", recordWriter.writePlayerRecord(player));
                index++;
            }
            insert(tar, "metadata.json", jsonUtil.toJson(metadata).getBytes(StandardCharsets.UTF_8));
            tar.finish();
        }
    }

    private static void insert(TarArchiveOutputStream tar, String name, byte[] bytes) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(bytes.length);
        tar.putArchiveEntry(entry);
        tar.write(bytes);
        tar.closeArchiveEntry();
    }

    private static String safeName(String name) {
        return name.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
