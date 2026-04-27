package net.easecation.ghostypaperrecorder.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecordingMetadataTest {
    @Test
    void mergePrefersNewNonNullFieldsAndPlayerInfo() {
        RecordingHackInfo hackInfo = new RecordingHackInfo(1, 2, true, 3, 4, false, 5);
        RecordingMetadata base = new RecordingMetadata(
                "old-map",
                1,
                "old-time",
                null,
                "old-game",
                "Old Game",
                "old-stage",
                List.of(new RecordingPlayerInfo("Steve", null, 100, null, null, null, null)),
                null
        );
        RecordingMetadata patch = new RecordingMetadata(
                null,
                2,
                null,
                "new-map-name",
                null,
                "New Game",
                null,
                List.of(new RecordingPlayerInfo("Steve", "Alias", null, "Device", null, 1, hackInfo)),
                "show"
        );

        RecordingMetadata merged = base.mergePreferNew(patch);
        RecordingPlayerInfo player = merged.players().getFirst();

        assertEquals("old-map", merged.map());
        assertEquals(2, merged.baseGameVersion());
        assertEquals("old-time", merged.time());
        assertEquals("new-map-name", merged.mapName());
        assertEquals("old-game", merged.game());
        assertEquals("New Game", merged.gameName());
        assertEquals("old-stage", merged.stage());
        assertEquals("show", merged.showStage());
        assertEquals("Steve", player.name());
        assertEquals("Alias", player.aliasName());
        assertEquals(100, player.protocol());
        assertEquals("Device", player.deviceModel());
        assertEquals(1, player.inputMode());
        assertEquals(hackInfo, player.hack());
    }

    @Test
    void filterAndFillPlayersKeepsRecordedPlayersOnly() {
        RecordingMetadata metadata = RecordingMetadata.createDefault("stage", "world", List.of(
                new RecordingPlayerInfo("Steve", "Alias", 100, null, null, null, null),
                RecordingPlayerInfo.of("Alex", "Alex")
        ));

        RecordingMetadata filtered = metadata.filterAndFillPlayers(List.of("Steve", "Missing"));

        assertEquals(2, filtered.players().size());
        assertEquals("Steve", filtered.players().get(0).name());
        assertEquals("Alias", filtered.players().get(0).aliasName());
        assertEquals("Missing", filtered.players().get(1).name());
        assertEquals("Missing", filtered.players().get(1).aliasName());
        assertNotNull(filtered.players());
    }

    @Test
    void filterAndFillCreatesPlayersWhenOriginalPlayersIsNull() {
        RecordingMetadata metadata = new RecordingMetadata("map", null, null, null, null, null, null, null, null);

        RecordingMetadata filtered = metadata.filterAndFillPlayers(List.of("Steve"));

        assertEquals(1, filtered.players().size());
        assertEquals("Steve", filtered.players().getFirst().name());
        assertNull(filtered.players().getFirst().hack());
    }
}
