package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.GhostyItem;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GhostyItemNbtTest {
    @Test
    void writesLittleEndianItemNbtWithGhostyPersistenceShape() {
        byte[] bytes = new GhostyItemNbt().write(new GhostyItem("minecraft:diamond_sword", 7, 1));
        Reader reader = new Reader(bytes);

        assertEquals(10, reader.u8());
        assertEquals("", reader.string());
        Map<String, Object> tags = reader.rootCompound();

        assertEquals("minecraft:diamond_sword", tags.get("Name"));
        assertEquals((short) 7, tags.get("Damage"));
        assertEquals((byte) 1, tags.get("Count"));
        assertEquals(bytes.length, reader.offset());
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

        private short i16() {
            short value = ByteBuffer.wrap(bytes, offset, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
            offset += Short.BYTES;
            return value;
        }

        private String string() {
            int length = i16() & 0xffff;
            String value = new String(bytes, offset, length, StandardCharsets.UTF_8);
            offset += length;
            return value;
        }

        private Map<String, Object> rootCompound() {
            Map<String, Object> tags = new HashMap<>();
            while (true) {
                int type = u8();
                if (type == 0) {
                    return tags;
                }
                String name = string();
                switch (type) {
                    case 1 -> tags.put(name, (byte) u8());
                    case 2 -> tags.put(name, i16());
                    case 8 -> tags.put(name, string());
                    default -> throw new IllegalArgumentException("Unsupported test tag type: " + type);
                }
            }
        }

        private int offset() {
            return offset;
        }
    }
}
