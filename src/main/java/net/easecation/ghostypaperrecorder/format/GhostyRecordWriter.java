package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.recording.PlayerRecording;

public final class GhostyRecordWriter {
    private final GhostyItemNbt itemNbt = new GhostyItemNbt();

    public byte[] writeLevelRecord(int lastTick) {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeByte(GhostyConstants.LEVEL_VERSION_2);
        writer.writeInt(0);
        writer.writeInt(2);
        writeTimeUpdate(writer, 0, 0);
        writeTimeUpdate(writer, Math.max(1, lastTick), Math.max(1, lastTick));
        return writer.toByteArray();
    }

    private void writeTimeUpdate(GhostyBinaryWriter writer, int tick, int time) {
        writer.writeInt(tick);
        writer.writeByte(GhostyConstants.LEVEL_UPDATE_TIME);
        writer.writeInt(time);
    }

    public byte[] writePlayerRecord(PlayerRecording recording) {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeByte(GhostyConstants.PLAYER_RECORD_SKINLESS_V3);
        writer.writeInt(recording.protocol());
        writer.writeString(recording.playerName());
        writer.writeEntityRuntimeId(recording.originEntityId());
        writer.writeInt(recording.updates().size());
        recording.updates().forEach(entry -> {
            writer.writeInt(entry.tick());
            writer.writeByte(entry.update().typeId());
            entry.update().writePayload(writer, itemNbt);
        });
        return writer.toByteArray();
    }
}
