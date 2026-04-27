package net.easecation.ghostypaperrecorder.api;

public record RecordingPlayerInfo(
        String name,
        String aliasName,
        Integer protocol,
        String deviceModel,
        Integer deviceOS,
        Integer inputMode,
        RecordingHackInfo hack
) {
    public static RecordingPlayerInfo of(String name, String aliasName) {
        return new RecordingPlayerInfo(name, aliasName, null, null, null, null, null);
    }

    public RecordingPlayerInfo mergePreferNew(RecordingPlayerInfo newer) {
        if (newer == null) {
            return this;
        }
        return new RecordingPlayerInfo(
                nonNull(newer.name, name),
                nonNull(newer.aliasName, aliasName),
                nonNull(newer.protocol, protocol),
                nonNull(newer.deviceModel, deviceModel),
                nonNull(newer.deviceOS, deviceOS),
                nonNull(newer.inputMode, inputMode),
                nonNull(newer.hack, hack)
        );
    }

    private static <T> T nonNull(T newer, T older) {
        return newer != null ? newer : older;
    }
}
