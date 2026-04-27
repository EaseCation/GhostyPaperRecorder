package net.easecation.ghostypaperrecorder.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RecordingMetadata(
        String map,
        Integer baseGameVersion,
        String time,
        String mapName,
        String game,
        String gameName,
        String stage,
        List<RecordingPlayerInfo> players,
        String showStage
) {
    public RecordingMetadata {
        players = players == null ? null : List.copyOf(players);
    }

    public static RecordingMetadata createDefault(String recordName, String worldName, List<RecordingPlayerInfo> players) {
        return new RecordingMetadata(
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

    public RecordingMetadata mergePreferNew(RecordingMetadata newer) {
        if (newer == null) {
            return this;
        }
        return new RecordingMetadata(
                nonNull(newer.map, map),
                nonNull(newer.baseGameVersion, baseGameVersion),
                nonNull(newer.time, time),
                nonNull(newer.mapName, mapName),
                nonNull(newer.game, game),
                nonNull(newer.gameName, gameName),
                nonNull(newer.stage, stage),
                mergePlayers(players, newer.players),
                nonNull(newer.showStage, showStage)
        );
    }

    public RecordingMetadata filterAndFillPlayers(List<String> recordedNames) {
        List<RecordingPlayerInfo> filtered = new ArrayList<>();
        Map<String, RecordingPlayerInfo> byName = new LinkedHashMap<>();
        if (players != null) {
            for (RecordingPlayerInfo player : players) {
                if (player.name() != null) {
                    byName.put(player.name(), player);
                }
            }
        }
        for (String name : recordedNames) {
            RecordingPlayerInfo player = byName.get(name);
            filtered.add(player == null ? RecordingPlayerInfo.of(name, name) : player);
        }
        return new RecordingMetadata(map, baseGameVersion, time, mapName, game, gameName, stage, filtered, showStage);
    }

    private static List<RecordingPlayerInfo> mergePlayers(List<RecordingPlayerInfo> older, List<RecordingPlayerInfo> newer) {
        if (newer == null) {
            return older;
        }
        if (older == null) {
            return newer;
        }
        Map<String, RecordingPlayerInfo> merged = new LinkedHashMap<>();
        for (RecordingPlayerInfo player : older) {
            if (player.name() != null) {
                merged.put(player.name(), player);
            }
        }
        for (RecordingPlayerInfo player : newer) {
            if (player.name() == null) {
                continue;
            }
            RecordingPlayerInfo current = merged.get(player.name());
            merged.put(player.name(), current == null ? player : current.mergePreferNew(player));
        }
        return List.copyOf(merged.values());
    }

    private static <T> T nonNull(T newer, T older) {
        return newer != null ? newer : older;
    }
}
