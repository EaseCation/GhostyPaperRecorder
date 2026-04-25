package net.easecation.ghostypaperrecorder.format;

import net.easecation.ghostypaperrecorder.model.GhostyItem;

public final class GhostyItemNbt {
    public byte[] write(GhostyItem item) {
        LittleEndianNbtWriter nbt = new LittleEndianNbtWriter();
        nbt.beginRootCompound();
        nbt.writeStringTag("Name", item.identifier());
        nbt.writeShortTag("Damage", item.damage());
        nbt.writeByteTag("Count", item.count());
        nbt.endCompound();
        return nbt.toByteArray();
    }
}
