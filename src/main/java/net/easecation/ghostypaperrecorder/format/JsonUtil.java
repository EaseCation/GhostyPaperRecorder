package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.PlaybackMetadata;
import net.easecation.ghostypaperrecorder.model.PlayerInfo;

import java.util.Iterator;

public final class JsonUtil {
    public String toJson(PlaybackMetadata metadata) {
        StringBuilder builder = new StringBuilder(256);
        builder.append('{');
        field(builder, "map", metadata.map()).append(',');
        nullableNumber(builder, "baseGameVersion", metadata.baseGameVersion()).append(',');
        field(builder, "time", metadata.time()).append(',');
        field(builder, "mapName", metadata.mapName()).append(',');
        field(builder, "game", metadata.game()).append(',');
        field(builder, "gameName", metadata.gameName()).append(',');
        field(builder, "stage", metadata.stage()).append(',');
        builder.append("\"players\":[");
        Iterator<PlayerInfo> iterator = metadata.players().iterator();
        while (iterator.hasNext()) {
            PlayerInfo player = iterator.next();
            builder.append('{');
            field(builder, "name", player.name()).append(',');
            field(builder, "aliasName", player.aliasName()).append(',');
            nullableNumber(builder, "protocol", player.protocol()).append(',');
            field(builder, "deviceModel", player.deviceModel()).append(',');
            nullableNumber(builder, "deviceOS", player.deviceOS()).append(',');
            nullableNumber(builder, "inputMode", player.inputMode()).append(',');
            builder.append("\"hack\":null");
            builder.append('}');
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append("],");
        field(builder, "showStage", metadata.showStage());
        builder.append('}');
        return builder.toString();
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
