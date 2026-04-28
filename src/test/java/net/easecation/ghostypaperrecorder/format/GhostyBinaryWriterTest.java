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
    void writesNukkitCompatibleUnsignedVarInt() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeUnsignedVarInt(300);
        assertArrayEquals(new byte[]{(byte) 0xac, 0x02}, writer.toByteArray());
    }

    @Test
    void writesNukkitCompatibleString() {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeString("abc");
        assertArrayEquals(new byte[]{0x03, 0x61, 0x62, 0x63}, writer.toByteArray());
    }
}
