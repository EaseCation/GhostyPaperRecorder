package net.easecation.ghostypaperrecorder.model;

import com.google.gson.JsonObject;

public record LevelCustomEvent(int tick, JsonObject event) {
}
