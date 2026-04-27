package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.api.RecordingHackInfo;
import net.easecation.ghostypaperrecorder.api.RecordingMetadata;
import net.easecation.ghostypaperrecorder.api.RecordingPlayerInfo;

import java.util.Iterator;
import java.util.List;

public final class JsonUtil {
    public String toJson(RecordingMetadata metadata) {
        StringBuilder builder = new StringBuilder(256);
        builder.append('{');
        field(builder, "map", metadata.map()).append(',');
        nullableNumber(builder, "baseGameVersion", metadata.baseGameVersion()).append(',');
        field(builder, "time", metadata.time()).append(',');
        field(builder, "mapName", metadata.mapName()).append(',');
        field(builder, "game", metadata.game()).append(',');
        field(builder, "gameName", metadata.gameName()).append(',');
        field(builder, "stage", metadata.stage()).append(',');
        players(builder, metadata.players()).append(',');
        field(builder, "showStage", metadata.showStage());
        builder.append('}');
        return builder.toString();
    }

    private static StringBuilder players(StringBuilder builder, List<RecordingPlayerInfo> players) {
        builder.append("\"players\":");
        if (players == null) {
            builder.append("null");
            return builder;
        }
        builder.append('[');
        Iterator<RecordingPlayerInfo> iterator = players.iterator();
        while (iterator.hasNext()) {
            RecordingPlayerInfo player = iterator.next();
            player(builder, player);
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(']');
        return builder;
    }

    private static StringBuilder player(StringBuilder builder, RecordingPlayerInfo player) {
        builder.append('{');
        field(builder, "name", player.name()).append(',');
        field(builder, "aliasName", player.aliasName()).append(',');
        nullableNumber(builder, "protocol", player.protocol()).append(',');
        field(builder, "deviceModel", player.deviceModel()).append(',');
        nullableNumber(builder, "deviceOS", player.deviceOS()).append(',');
        nullableNumber(builder, "inputMode", player.inputMode()).append(',');
        hack(builder, "hack", player.hack());
        builder.append('}');
        return builder;
    }

    private static StringBuilder hack(StringBuilder builder, String key, RecordingHackInfo hack) {
        builder.append('"').append(escape(key)).append("\":");
        if (hack == null) {
            builder.append("null");
            return builder;
        }
        builder.append('{');
        number(builder, "kbH", hack.kbH()).append(',');
        number(builder, "kbV", hack.kbV()).append(',');
        bool(builder, "enableHitbox", hack.enableHitbox()).append(',');
        number(builder, "hitboxV", hack.hitboxV()).append(',');
        number(builder, "hitboxH", hack.hitboxH()).append(',');
        bool(builder, "enableSpeed", hack.enableSpeed()).append(',');
        number(builder, "speed", hack.speed());
        builder.append('}');
        return builder;
    }

    private static StringBuilder field(StringBuilder builder, String key, String value) {
        builder.append('"').append(escape(key)).append("\":");
        if (value == null) {
            builder.append("null");
        } else {
            builder.append('"').append(escape(value)).append('"');
        }
        return builder;
    }

    private static StringBuilder nullableNumber(StringBuilder builder, String key, Number value) {
        builder.append('"').append(escape(key)).append("\":");
        builder.append(value == null ? "null" : value.toString());
        return builder;
    }

    private static StringBuilder number(StringBuilder builder, String key, Number value) {
        builder.append('"').append(escape(key)).append("\":").append(value);
        return builder;
    }

    private static StringBuilder bool(StringBuilder builder, String key, boolean value) {
        builder.append('"').append(escape(key)).append("\":").append(value);
        return builder;
    }

    private static String escape(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                }
            }
        }
        return builder.toString();
    }

    public JsonUtil() {
    }
}
