package net.easecation.ghostypaperrecorder.model;

public record PlayerInfo(
        String name,
        String aliasName,
        Integer protocol,
        String deviceModel,
        Integer deviceOS,
        Integer inputMode,
        Object hack
) {
    public static PlayerInfo of(String name, String aliasName) {
        return new PlayerInfo(name, aliasName, null, null, null, null, null);
    }
}
