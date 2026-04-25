package net.easecation.ghostypaperrecorder.recording;

import net.easecation.ghostypaperrecorder.format.GhostyBinaryWriter;
import net.easecation.ghostypaperrecorder.format.GhostyPackWriter;
import net.easecation.ghostypaperrecorder.model.PlaybackMetadata;
import net.easecation.ghostypaperrecorder.model.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RecordingSession {
    private static final int FLAG_ON_FIRE = 0;
    private static final int FLAG_SNEAKING = 1;
    private static final int FLAG_SPRINTING = 3;
    private static final int FLAG_INVISIBLE = 5;
    private static final int FLAG_GLIDING = 32;
    private static final int FLAG_SWIMMING = 56;

    private final Plugin plugin;
    private final PaperItemMapper itemMapper;
    private final GhostyPackWriter packWriter;
    private final Path outputFile;
    private final String recordName;
    private final Map<UUID, PlayerRecording> players = new LinkedHashMap<>();
    private final List<PlayerInfo> playerInfo = new ArrayList<>();
    private final BukkitTask task;
    private String worldName = "paper";
    private int tick = 0;
    private boolean stopped = false;

    public RecordingSession(Plugin plugin, PaperItemMapper itemMapper, GhostyPackWriter packWriter, Path outputFile, String recordName) {
        this.plugin = plugin;
        this.itemMapper = itemMapper;
        this.packWriter = packWriter;
        this.outputFile = outputFile;
        this.recordName = recordName;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::recordTick, 1L, 1L);
    }

    public void recordTick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            worldName = world.getName();
            playerRecording(player).record(tick, snapshot(player));
        }
        tick++;
    }

    public void recordAttack(Player attacker, Player target) {
        if (stopped) {
            return;
        }
        PlayerRecording attackerRecording = playerRecording(attacker);
        PlayerRecording targetRecording = playerRecording(target);
        attackerRecording.recordAttack(tick, targetRecording.originEntityId());
    }

    public Path stopAndSave() throws IOException {
        if (stopped) {
            return outputFile;
        }
        stopped = true;
        task.cancel();
        Files.createDirectories(outputFile.getParent());
        PlaybackMetadata metadata = PlaybackMetadata.create(recordName, worldName, playerInfo);
        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            packWriter.write(outputStream, Math.max(1, tick), players.values(), metadata);
        }
        return outputFile;
    }

    public int tick() {
        return tick;
    }

    public int playerCount() {
        return players.size();
    }

    public Collection<PlayerRecording> players() {
        return players.values();
    }

    public boolean isStopped() {
        return stopped;
    }

    public Path outputFile() {
        return outputFile;
    }

    private PlayerRecording playerRecording(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), uuid -> {
            playerInfo.add(PlayerInfo.of(player.getName(), player.getName()));
            return new PlayerRecording(player.getName(), GhostyBinaryWriter.stableEntityId(uuid), 0);
        });
    }

    private PlayerSnapshot snapshot(Player player) {
        PlayerInventory inventory = player.getInventory();
        return new PlayerSnapshot(
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch(),
                player.getName(),
                itemMapper.map(inventory.getItemInMainHand()),
                itemMapper.map(inventory.getHelmet()),
                itemMapper.map(inventory.getChestplate()),
                itemMapper.map(inventory.getLeggings()),
                itemMapper.map(inventory.getBoots()),
                itemMapper.map(inventory.getItemInOffHand()),
                flags(player),
                player.getPing()
        );
    }

    private static long flags(Player player) {
        long flags = 0L;
        flags = set(flags, FLAG_ON_FIRE, player.getFireTicks() > 0);
        flags = set(flags, FLAG_SNEAKING, player.isSneaking());
        flags = set(flags, FLAG_SPRINTING, player.isSprinting());
        flags = set(flags, FLAG_INVISIBLE, player.isInvisible());
        flags = set(flags, FLAG_GLIDING, player.isGliding());
        flags = set(flags, FLAG_SWIMMING, player.isSwimming());
        return flags;
    }

    private static long set(long flags, int bit, boolean value) {
        if (!value) {
            return flags;
        }
        return flags | (1L << bit);
    }
}
