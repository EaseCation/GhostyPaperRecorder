package net.easecation.ghostypaperrecorder.format;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 最小 little-endian NBT 写入器，仅覆盖 Ghosty 物品记录需要的标签。
 */
public final class LittleEndianNbtWriter {
    private static final int TAG_END = 0;
    private static final int TAG_BYTE = 1;
    private static final int TAG_SHORT = 2;
    private static final int TAG_INT = 3;
    private static final int TAG_STRING = 8;
    private static final int TAG_COMPOUND = 10;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public void beginRootCompound() {
        writeByte(TAG_COMPOUND);
        writeStringPayload("");
    }

    public void endCompound() {
        writeByte(TAG_END);
    }

    public void writeByteTag(String name, int value) {
        writeNamedHeader(TAG_BYTE, name);
        writeByte(value);
    }

    public void writeShortTag(String name, int value) {
        writeNamedHeader(TAG_SHORT, name);
        writeShort(value);
    }

    public void writeIntTag(String name, int value) {
        writeNamedHeader(TAG_INT, name);
        writeInt(value);
    }

    public void writeStringTag(String name, String value) {
        writeNamedHeader(TAG_STRING, name);
        writeStringPayload(value == null ? "" : value);
    }

    public void beginCompoundTag(String name) {
        writeNamedHeader(TAG_COMPOUND, name);
    }

    private void writeNamedHeader(int type, String name) {
        writeByte(type);
        writeStringPayload(name);
    }

    private void writeStringPayload(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeShort(bytes.length);
        out.writeBytes(bytes);
    }

    private void writeByte(int value) {
        out.write(value & 0xff);
    }

    private void writeShort(int value) {
        writeByte(value);
        writeByte(value >>> 8);
    }

    private void writeInt(int value) {
        writeByte(value);
        writeByte(value >>> 8);
        writeByte(value >>> 16);
        writeByte(value >>> 24);
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}
