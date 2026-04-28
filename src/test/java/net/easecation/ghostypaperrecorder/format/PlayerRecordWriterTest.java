package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.GhostyItem;
import net.easecation.ghostypaperrecorder.model.PlayerUpdate;
import net.easecation.ghostypaperrecorder.recording.PlayerRecording;
import net.easecation.ghostypaperrecorder.recording.PlayerSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerRecordWriterTest {
    @Test
    void writesAnimateUpdateWithGhostyPayload() {
        PlayerRecording recording = new PlayerRecording("Alex", 42L, 0);
        recording.recordAnimate(7, 1, 0.0f);

        byte[] bytes = new GhostyRecordWriter().writePlayerRecord(recording);
        LittleEndianRecordReader reader = new LittleEndianRecordReader(bytes);
        assertEquals(GhostyConstants.PLAYER_RECORD_SKINLESS_V3, reader.u8());
        assertEquals(0, reader.i32());
        assertEquals("Alex", reader.string());
        assertEquals(42L, reader.unsignedVarLong());
        assertEquals(1, reader.unsignedVarInt());
        assertEquals(7, reader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_ANIMATE, reader.u8());
        assertEquals(1, reader.varInt());
        assertEquals(0.0f, reader.f32());
        assertEquals(bytes.length, reader.offset());
    }

    @Test
    void writesAttackUpdateWithGhostyPayload() {
        PlayerRecording recording = new PlayerRecording("Alex", 42L, 0);
        recording.recordAttack(8, 123456789L);

        byte[] bytes = new GhostyRecordWriter().writePlayerRecord(recording);
        LittleEndianRecordReader reader = new LittleEndianRecordReader(bytes);
        assertEquals(GhostyConstants.PLAYER_RECORD_SKINLESS_V3, reader.u8());
        assertEquals(0, reader.i32());
        assertEquals("Alex", reader.string());
        assertEquals(42L, reader.unsignedVarLong());
        assertEquals(1, reader.unsignedVarInt());
        assertEquals(8, reader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_ATTACK, reader.u8());
        assertEquals(123456789L, reader.unsignedVarLong());
        assertEquals(bytes.length, reader.offset());
    }

    @Test
    void writesSnapshotUpdatesWithGhostyLittleEndianPayloads() {
        PlayerRecording recording = new PlayerRecording("Steve", 0x0102030405060708L, 0);
        PlayerSnapshot snapshot = new PlayerSnapshot(
                1.25, 64.0, -3.5,
                180.0, 30.0,
                "Steve",
                new GhostyItem("minecraft:diamond_sword", 0, 1),
                GhostyItem.AIR,
                GhostyItem.AIR,
                GhostyItem.AIR,
                GhostyItem.AIR,
                GhostyItem.AIR,
                123L,
                45
        );
        recording.record(20, snapshot);

        byte[] bytes = new GhostyRecordWriter().writePlayerRecord(recording);
        LittleEndianRecordReader reader = new LittleEndianRecordReader(bytes);
        assertEquals(GhostyConstants.PLAYER_RECORD_SKINLESS_V3, reader.u8());
        assertEquals(0, reader.i32());
        assertEquals("Steve", reader.string());
        assertEquals(0x0102030405060708L, reader.unsignedVarLong());
        assertEquals(11, reader.unsignedVarInt());

        assertEquals(20, reader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_POSITION_XYZ, reader.u8());
        assertEquals(1.25f, reader.f32());
        assertEquals(64.0f, reader.f32());
        assertEquals(-3.5f, reader.f32());

        assertEquals(20, reader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_ROTATION, reader.u8());
        assertEquals(180.0f, reader.f32());
        assertEquals(30.0f, reader.f32());

        assertEquals(20, reader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_TAG_NAME, reader.u8());
        assertEquals("Steve", reader.string());

        for (int typeId = GhostyConstants.PLAYER_UPDATE_ITEM; typeId <= GhostyConstants.PLAYER_UPDATE_OFFHAND; typeId++) {
            assertEquals(20, reader.unsignedVarInt());
            assertEquals(typeId, reader.u8());
            assertTrue(reader.byteArray().length > 0);
        }

        assertEquals(20, reader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_DATA_FLAGS, reader.u8());
        assertEquals(123L, reader.varLong());

        assertEquals(20, reader.unsignedVarInt());
        assertEquals(GhostyConstants.PLAYER_UPDATE_PING, reader.u8());
        assertEquals(45, reader.unsignedVarInt());
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
}
