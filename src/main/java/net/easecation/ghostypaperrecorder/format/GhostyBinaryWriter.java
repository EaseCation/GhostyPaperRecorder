package net.easecation.ghostypaperrecorder.format;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 写出 Ghosty 当前 LittleEndianBinaryStream 兼容的基础类型。
 */
public final class GhostyBinaryWriter {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public void writeByte(int value) {
        out.write(value & 0xff);
    }

    public void writeBoolean(boolean value) {
        writeByte(value ? 1 : 0);
    }

    public void writeInt(int value) {
        writeByte(value);
        writeByte(value >>> 8);
        writeByte(value >>> 16);
        writeByte(value >>> 24);
    }

    public void writeLong(long value) {
        writeByte((int) value);
        writeByte((int) (value >>> 8));
        writeByte((int) (value >>> 16));
        writeByte((int) (value >>> 24));
        writeByte((int) (value >>> 32));
        writeByte((int) (value >>> 40));
        writeByte((int) (value >>> 48));
        writeByte((int) (value >>> 56));
    }

    public void writeUnsignedVarInt(long value) {
        long remaining = value & 0xffffffffL;
        do {
            int temp = (int) (remaining & 0x7f);
            remaining >>>= 7;
            if (remaining != 0) {
                temp |= 0x80;
            }
            writeByte(temp);
        } while (remaining != 0);
    }

    public void writeVarInt(int value) {
        writeUnsignedVarInt((((long) value << 1) ^ (value >> 31)) & 0xffffffffL);
    }

    public void writeUnsignedVarLong(long value) {
        long remaining = value;
        do {
            int temp = (int) (remaining & 0x7f);
            remaining >>>= 7;
            if (remaining != 0) {
                temp |= 0x80;
            }
            writeByte(temp);
        } while (remaining != 0);
    }

    public void writeVarLong(long value) {
        writeUnsignedVarLong((value << 1) ^ (value >> 63));
    }

    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public void writeString(String value) {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        writeByteArray(bytes);
    }

    public void writeByteArray(byte[] bytes) {
        writeUnsignedVarInt(bytes.length);
        out.writeBytes(bytes);
    }

    public void writeVector3f(float x, float y, float z) {
        writeFloat(x);
        writeFloat(y);
        writeFloat(z);
    }

    public void writeBlockVector3(int x, int y, int z) {
        writeInt(x);
        writeInt(y);
        writeInt(z);
    }

    public void writeEntityRuntimeId(long value) {
        writeUnsignedVarLong(value);
    }

    public void writeEntityUniqueId(long value) {
        writeLong(value);
    }

    public static long stableEntityId(UUID uuid) {
        long value = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        return value == 0L ? 1L : value;
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}
