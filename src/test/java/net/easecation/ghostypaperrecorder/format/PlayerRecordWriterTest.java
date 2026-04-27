package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.GhostyItem;
import net.easecation.ghostypaperrecorder.model.PlayerUpdate;
import net.easecation.ghostypaperrecorder.recording.PlayerRecording;
import net.easecation.ghostypaperrecorder.recording.PlayerSnapshot;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerRecordWriterTest {
    @Test
    void writesAnimateUpdateWithGhostyPayload() {
        PlayerRecording recording = new PlayerRecording("Alex", 42L, 0);
        recording.recordAnimate(7, 1, 0.0f);

        byte[] bytes = new GhostyRecordWriter().writePlayerRecord(recording);
        Reader reader = new Reader(bytes);
        assertEquals(GhostyConstants.PLAYER_RECORD_SKINLESS_V3, reader.u8());
        assertEquals(0, reader.i32());
        assertEquals("Alex", reader.string());
        assertEquals(42L, reader.i64());
        assertEquals(1, reader.i32());
        assertEquals(7, reader.i32());
        assertEquals(GhostyConstants.PLAYER_UPDATE_ANIMATE, reader.u8());
        assertEquals(1, reader.i32());
        assertEquals(0.0f, reader.f32());
        assertEquals(bytes.length, reader.offset());
    }

    @Test
    void writesAttackUpdateWithGhostyPayload() {
        PlayerRecording recording = new PlayerRecording("Alex", 42L, 0);
        recording.recordAttack(8, 123456789L);

        byte[] bytes = new GhostyRecordWriter().writePlayerRecord(recording);
        Reader reader = new Reader(bytes);
        assertEquals(GhostyConstants.PLAYER_RECORD_SKINLESS_V3, reader.u8());
        assertEquals(0, reader.i32());
        assertEquals("Alex", reader.string());
        assertEquals(42L, reader.i64());
        assertEquals(1, reader.i32());
        assertEquals(8, reader.i32());
        assertEquals(GhostyConstants.PLAYER_UPDATE_ATTACK, reader.u8());
        assertEquals(123456789L, reader.i64());
        assertEquals(bytes.length, reader.offset());
    }

    @Test
    void forceRecordWritesFinalStateEvenWhenNothingChanged() {
        PlayerRecording recording = new PlayerRecording("Alex", 42L, 0);
        PlayerSnapshot snapshot = new PlayerSnapshot(
                1.0, 2.0, 3.0,
                90.0, 10.0,
                "Alex",
                GhostyItem.AIR,
                GhostyItem.AIR,
                GhostyItem.AIR,
                GhostyItem.AIR,
                GhostyItem.AIR,
                GhostyItem.AIR,
                0L,
                5
        );
        recording.record(0, snapshot);
        int before = recording.updates().size();
        recording.forceRecord(100, snapshot);

        assertTrue(recording.updates().size() > before);
        assertTrue(recording.updates().stream().anyMatch(update -> update.tick() == 100 && update.update() instanceof PlayerUpdate.Position));
        assertTrue(recording.updates().stream().anyMatch(update -> update.tick() == 100 && update.update() instanceof PlayerUpdate.Rotation));
    }

    private static final class Reader {
        private final byte[] bytes;
        private int offset;

        private Reader(byte[] bytes) {
            this.bytes = bytes;
        }

        private int u8() {
            return bytes[offset++] & 0xff;
        }

        private int i32() {
            int value = ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            offset += 4;
            return value;
        }

        private long i64() {
            long value = ByteBuffer.wrap(bytes, offset, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
            offset += 8;
            return value;
        }

        private float f32() {
            float value = ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            offset += 4;
            return value;
        }

        private String string() {
            int length = i32();
            String value = new String(bytes, offset, length, java.nio.charset.StandardCharsets.UTF_8);
            offset += length;
            return value;
        }

        private int offset() {
            return offset;
        }
    }
}
