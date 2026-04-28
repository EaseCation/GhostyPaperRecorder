package net.easecation.ghostypaperrecorder.model;

import net.easecation.ghostypaperrecorder.format.GhostyBinaryWriter;
import net.easecation.ghostypaperrecorder.format.GhostyConstants;
import net.easecation.ghostypaperrecorder.format.GhostyItemNbt;

public sealed interface PlayerUpdate permits PlayerUpdate.Position, PlayerUpdate.Rotation, PlayerUpdate.TagName,
        PlayerUpdate.DataFlags, PlayerUpdate.ItemUpdate, PlayerUpdate.Animate, PlayerUpdate.Ping, PlayerUpdate.Attack {
    int typeId();

    void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt);

    record Position(double x, double y, double z) implements PlayerUpdate {
        @Override
        public int typeId() {
            return GhostyConstants.PLAYER_UPDATE_POSITION_XYZ;
        }

        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeFloat((float) x);
            writer.writeFloat((float) y);
            writer.writeFloat((float) z);
        }
    }

    record Rotation(double yaw, double pitch) implements PlayerUpdate {
        @Override
        public int typeId() {
            return GhostyConstants.PLAYER_UPDATE_ROTATION;
        }

        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeFloat((float) yaw);
            writer.writeFloat((float) pitch);
        }
    }

    record TagName(String value) implements PlayerUpdate {
        @Override
        public int typeId() {
            return GhostyConstants.PLAYER_UPDATE_TAG_NAME;
        }

        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeString(value);
        }
    }

    record DataFlags(long flags) implements PlayerUpdate {
        @Override
        public int typeId() {
            return GhostyConstants.PLAYER_UPDATE_DATA_FLAGS;
        }

        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeVarLong(flags);
        }
    }

    record ItemUpdate(int typeId, GhostyItem item) implements PlayerUpdate {
        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeByteArray(itemNbt.write(item));
        }
    }

    record Animate(int action, float rowingTime) implements PlayerUpdate {
        @Override
        public int typeId() {
            return GhostyConstants.PLAYER_UPDATE_ANIMATE;
        }

        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeVarInt(action);
            writer.writeFloat(rowingTime);
        }
    }

    record Ping(int value) implements PlayerUpdate {
        @Override
        public int typeId() {
            return GhostyConstants.PLAYER_UPDATE_PING;
        }

        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeUnsignedVarInt(value);
        }
    }

    record Attack(long targetEntityId) implements PlayerUpdate {
        @Override
        public int typeId() {
            return GhostyConstants.PLAYER_UPDATE_ATTACK;
        }

        @Override
        public void writePayload(GhostyBinaryWriter writer, GhostyItemNbt itemNbt) {
            writer.writeEntityRuntimeId(targetEntityId);
        }
    }
}
