package net.easecation.ghostypaperrecorder.recording;

import net.easecation.ghostypaperrecorder.format.GhostyBinaryWriter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityIdMapperTest {
    @Test
    void returnsStableGhostyEntityIdForSameUuid() {
        UUID uuid = UUID.randomUUID();
        EntityIdMapper mapper = new EntityIdMapper();

        long first = mapper.id(uuid);
        long second = mapper.id(uuid);

        assertEquals(GhostyBinaryWriter.stableEntityId(uuid), first);
        assertEquals(first, second);
    }
}
