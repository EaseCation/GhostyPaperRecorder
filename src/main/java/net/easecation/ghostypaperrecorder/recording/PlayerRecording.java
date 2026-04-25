package net.easecation.ghostypaperrecorder.recording;

import net.easecation.ghostypaperrecorder.model.PlayerUpdate;
import net.easecation.ghostypaperrecorder.model.TimedPlayerUpdate;

import java.util.ArrayList;
import java.util.List;

public final class PlayerRecording {
    private final String playerName;
    private final long originEntityId;
    private final int protocol;
    private final List<TimedPlayerUpdate> updates = new ArrayList<>();
    private PlayerSnapshot last;

    public PlayerRecording(String playerName, long originEntityId, int protocol) {
        this.playerName = playerName;
        this.originEntityId = originEntityId;
        this.protocol = protocol;
    }

    public void record(int tick, PlayerSnapshot snapshot) {
        if (last == null || last.x() != snapshot.x() || last.y() != snapshot.y() || last.z() != snapshot.z()) {
            add(tick, new PlayerUpdate.Position(snapshot.x(), snapshot.y(), snapshot.z()));
        }
        if (last == null || last.yaw() != snapshot.yaw() || last.pitch() != snapshot.pitch()) {
            add(tick, new PlayerUpdate.Rotation(snapshot.yaw(), snapshot.pitch()));
        }
        if (last == null || !last.nameTag().equals(snapshot.nameTag())) {
            add(tick, new PlayerUpdate.TagName(snapshot.nameTag()));
        }
        if (last == null || !last.hand().equals(snapshot.hand())) {
            add(tick, new PlayerUpdate.ItemUpdate(net.easecation.ghostypaperrecorder.format.GhostyConstants.PLAYER_UPDATE_ITEM, snapshot.hand()));
        }
        if (last == null || !last.helmet().equals(snapshot.helmet())) {
            add(tick, new PlayerUpdate.ItemUpdate(net.easecation.ghostypaperrecorder.format.GhostyConstants.PLAYER_UPDATE_ARMOR_0, snapshot.helmet()));
        }
        if (last == null || !last.chestplate().equals(snapshot.chestplate())) {
            add(tick, new PlayerUpdate.ItemUpdate(net.easecation.ghostypaperrecorder.format.GhostyConstants.PLAYER_UPDATE_ARMOR_1, snapshot.chestplate()));
        }
        if (last == null || !last.leggings().equals(snapshot.leggings())) {
            add(tick, new PlayerUpdate.ItemUpdate(net.easecation.ghostypaperrecorder.format.GhostyConstants.PLAYER_UPDATE_ARMOR_2, snapshot.leggings()));
        }
        if (last == null || !last.boots().equals(snapshot.boots())) {
            add(tick, new PlayerUpdate.ItemUpdate(net.easecation.ghostypaperrecorder.format.GhostyConstants.PLAYER_UPDATE_ARMOR_3, snapshot.boots()));
        }
        if (last == null || !last.offhand().equals(snapshot.offhand())) {
            add(tick, new PlayerUpdate.ItemUpdate(net.easecation.ghostypaperrecorder.format.GhostyConstants.PLAYER_UPDATE_OFFHAND, snapshot.offhand()));
        }
        if (last == null || last.dataFlags() != snapshot.dataFlags()) {
            add(tick, new PlayerUpdate.DataFlags(snapshot.dataFlags()));
        }
        if (last == null || last.ping() != snapshot.ping()) {
            add(tick, new PlayerUpdate.Ping(snapshot.ping()));
        }
        last = snapshot;
    }

    public void recordAttack(int tick, long targetEntityId) {
        add(tick, new PlayerUpdate.Attack(targetEntityId));
    }

    private void add(int tick, PlayerUpdate update) {
        updates.add(new TimedPlayerUpdate(tick, update));
    }

    public String playerName() {
        return playerName;
    }

    public long originEntityId() {
        return originEntityId;
    }

    public int protocol() {
        return protocol;
    }

    public List<TimedPlayerUpdate> updates() {
        return updates;
    }
}
