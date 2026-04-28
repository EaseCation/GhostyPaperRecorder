package net.easecation.ghostypaperrecorder.format;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

final class LittleEndianRecordReader {
    private final byte[] bytes;
    private int offset;

    LittleEndianRecordReader(byte[] bytes) {
        this.bytes = bytes;
    }

    int u8() {
        return bytes[offset++] & 0xff;
    }

    int i32() {
        int value = ByteBuffer.wrap(bytes, offset, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
        offset += Integer.BYTES;
        return value;
    }

    long i64() {
        long value = ByteBuffer.wrap(bytes, offset, Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).getLong();
        offset += Long.BYTES;
        return value;
    }

    float f32() {
        float value = ByteBuffer.wrap(bytes, offset, Float.BYTES).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        offset += Float.BYTES;
        return value;
    }

    String string() {
        byte[] value = byteArray();
        return new String(value, StandardCharsets.UTF_8);
    }

    byte[] byteArray() {
        int length = unsignedVarInt();
        byte[] value = new byte[length];
        System.arraycopy(bytes, offset, value, 0, length);
        offset += length;
        return value;
    }

    int varInt() {
        return i32();
    }

    int unsignedVarInt() {
        return i32();
    }

    long varLong() {
        return i64();
    }

    long unsignedVarLong() {
        return i64();
    }

    int offset() {
        return offset;
    }
}
