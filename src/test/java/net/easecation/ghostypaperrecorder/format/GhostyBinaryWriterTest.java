package net.easecation.ghostypaperrecorder.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class GhostyBinaryWriterTest {
    @Test
    void writesLittleEndianInt() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeInt(0x01020304);
        assertArrayEquals(new byte[]{0x04, 0x03, 0x02, 0x01}, writer.toByteArray());
    }

    @Test
    void writesGhostyLittleEndianUnsignedVarInt() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeUnsignedVarInt(300);
        assertArrayEquals(new byte[]{0x2c, 0x01, 0x00, 0x00}, writer.toByteArray());
    }

    @Test
    void writesGhostyLittleEndianUnsignedVarLong() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeUnsignedVarLong(0x0102030405060708L);
        assertArrayEquals(new byte[]{0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01}, writer.toByteArray());
    }

    @Test
    void writesGhostyLittleEndianEntityRuntimeId() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeEntityRuntimeId(42L);
        assertArrayEquals(new byte[]{0x2a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, writer.toByteArray());
    }

    @Test
    void writesGhostyLittleEndianString() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeString("abc");
        assertArrayEquals(new byte[]{0x03, 0x00, 0x00, 0x00, 0x61, 0x62, 0x63}, writer.toByteArray());
    }
}
