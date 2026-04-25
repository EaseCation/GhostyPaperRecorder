package net.easecation.ghostypaperrecorder.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class GhostyBinaryWriterTest {
    @Test
    void writesFixedLittleEndianIntForGhostyVarIntCompatibility() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeInt(0x01020304);
        assertArrayEquals(new byte[]{0x04, 0x03, 0x02, 0x01}, writer.toByteArray());
    }
}
