package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.LevelCustomEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GhostyRecordWriterTest {
    @Test
    void writesLevelRecordWithGhostyLittleEndianPayloads() {
        byte[] bytes = new GhostyRecordWriter().writeLevelRecord(20, List.of(new LevelCustomEvent(5, null)));
        LittleEndianRecordReader reader = new LittleEndianRecordReader(bytes);

        assertEquals(GhostyConstants.LEVEL_VERSION_2, reader.u8());
        assertEquals(0, reader.i32());
        assertEquals(3, reader.unsignedVarInt());

        assertEquals(0, reader.unsignedVarInt());
        assertEquals(GhostyConstants.LEVEL_UPDATE_TIME, reader.u8());
        assertEquals(0, reader.varInt());

        assertEquals(5, reader.unsignedVarInt());
        assertEquals(GhostyConstants.LEVEL_UPDATE_CUSTOM_EVENT, reader.u8());
        assertEquals("{}", reader.string());

        assertEquals(20, reader.unsignedVarInt());
        assertEquals(GhostyConstants.LEVEL_UPDATE_TIME, reader.u8());
        assertEquals(20, reader.varInt());
        assertEquals(bytes.length, reader.offset());
    }
}
