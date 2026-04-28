package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.LevelCustomEvent;
import net.easecation.ghostypaperrecorder.recording.PlayerRecording;

import java.util.Comparator;
import java.util.List;

public final class GhostyRecordWriter {
    private final GhostyItemNbt itemNbt = new GhostyItemNbt();

    public byte[] writeLevelRecord(int lastTick) {
        return writeLevelRecord(lastTick, List.of());
    }

    public byte[] writeLevelRecord(int lastTick, List<LevelCustomEvent> customEvents) {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeByte(GhostyConstants.LEVEL_VERSION_2);
        writer.writeInt(0);
        int eventCount = customEvents == null ? 0 : customEvents.size();
        writer.writeUnsignedVarInt(2L + eventCount);
        writeTimeUpdate(writer, 0, 0);
        if (customEvents != null) {
            customEvents.stream()
                    .sorted(Comparator.comparingInt(LevelCustomEvent::tick))
                    .forEach(event -> writeCustomEvent(writer, event));
        }
        writeTimeUpdate(writer, Math.max(1, lastTick), Math.max(1, lastTick));
        return writer.toByteArray();
    }

    private void writeTimeUpdate(GhostyBinaryWriter writer, int tick, int time) {
        writer.writeUnsignedVarInt(tick);
        writer.writeByte(GhostyConstants.LEVEL_UPDATE_TIME);
        writer.writeVarInt(time);
    }

    private void writeCustomEvent(GhostyBinaryWriter writer, LevelCustomEvent event) {
        writer.writeUnsignedVarInt(Math.max(0, event.tick()));
        writer.writeByte(GhostyConstants.LEVEL_UPDATE_CUSTOM_EVENT);
        writer.writeString(event.event() == null ? "{}" : event.event().toString());
    }

    public byte[] writePlayerRecord(PlayerRecording recording) {
        GhostyBinaryWriter writer = new GhostyBinaryWriter();
        writer.writeByte(GhostyConstants.PLAYER_RECORD_SKINLESS_V3);
        writer.writeInt(recording.protocol());
        writer.writeString(recording.playerName());
        writer.writeEntityRuntimeId(recording.originEntityId());
        writer.writeUnsignedVarInt(recording.updates().size());
        recording.updates().forEach(entry -> {
            writer.writeUnsignedVarInt(entry.tick());
            writer.writeByte(entry.update().typeId());
            entry.update().writePayload(writer, itemNbt);
        });
        return writer.toByteArray();
    }
}
