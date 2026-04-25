package net.easecation.ghostypaperrecorder.model;

import java.time.LocalDateTime;
import java.util.List;

public record PlaybackMetadata(
        String map,
        Integer baseGameVersion,
        String time,
        String mapName,
        String game,
        String gameName,
        String stage,
        List<PlayerInfo> players,
        String showStage
) {
    public static PlaybackMetadata create(String recordName, String worldName, List<PlayerInfo> players) {
        return new PlaybackMetadata(
                worldName,
                null,
                LocalDateTime.now().toString(),
                worldName,
                "paper-pvp",
                "Paper PvP",
                recordName,
                players,
                null
        );
    }
}
