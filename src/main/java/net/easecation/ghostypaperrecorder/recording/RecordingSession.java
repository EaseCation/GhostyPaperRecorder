package net.easecation.ghostypaperrecorder.recording;

import net.easecation.ghostypaperrecorder.api.RecordingMetadata;
import net.easecation.ghostypaperrecorder.api.RecordingPlayerInfo;
import net.easecation.ghostypaperrecorder.api.RecordingStatus;
import net.easecation.ghostypaperrecorder.api.RecordingStopResult;
import net.easecation.ghostypaperrecorder.format.GhostyBinaryWriter;
import net.easecation.ghostypaperrecorder.format.GhostyPackWriter;
import net.easecation.ghostypaperrecorder.model.LevelCustomEvent;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

public final class RecordingSession {
    private static final int FLAG_ON_FIRE = 0;
    private static final int FLAG_SNEAKING = 1;
    private static final int FLAG_SPRINTING = 3;
    private static final int FLAG_INVISIBLE = 5;
    private static final int FLAG_GLIDING = 32;
    private static final int FLAG_SWIMMING = 56;
    private static final int ANIMATE_SWING_ARM = 1;

    private final PaperItemMapper itemMapper;
    private final GhostyPackWriter packWriter;
    private final String sessionId;
    private final Path outputFile;
    private final String recordName;
    private final Set<UUID> participants;
    private final Map<UUID, RecordingPlayerInfo> playerIdentities;
    private final Map<UUID, PlayerRecording> players = new LinkedHashMap<>();
    private final List<LevelCustomEvent> customEvents = new ArrayList<>();
    private final EntityIdMapper entityIdMapper = new EntityIdMapper();
    private final BukkitTask task;
    private RecordingMetadata metadata;
    private String worldName = "paper";
    private int tick = 0;
    private boolean stopped = false;
    private RecordingStopResult stopResult;

    public RecordingSession(Plugin plugin, PaperItemMapper itemMapper, GhostyPackWriter packWriter, String sessionId,
                            Path outputFile, String recordName, Set<UUID> participants, RecordingMetadata metadata) {
        this(plugin, itemMapper, packWriter, sessionId, outputFile, recordName, participants, metadata, Map.of());
    }

    public RecordingSession(Plugin plugin, PaperItemMapper itemMapper, GhostyPackWriter packWriter, String sessionId,
                            Path outputFile, String recordName, Set<UUID> participants, RecordingMetadata metadata,
                            Map<UUID, RecordingPlayerInfo> playerIdentities) {
        this.itemMapper = itemMapper;
        this.packWriter = packWriter;
        this.sessionId = sessionId;
        this.outputFile = outputFile;
        this.recordName = recordName;
        this.participants = new LinkedHashSet<>(participants);
        this.playerIdentities = playerIdentities == null ? new LinkedHashMap<>() : new LinkedHashMap<>(playerIdentities);
        this.metadata = metadata;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, this::recordTick, 1L, 1L);
    }

    public void recordTick() {
        for (UUID participant : participants) {
            Player player = Bukkit.getPlayer(participant);
            if (player == null || !player.isOnline()) {
                continue;
            }
            World world = player.getWorld();
            worldName = world.getName();
            playerRecording(player).record(tick, snapshot(player));
        }
        tick++;
    }

    public void recordSwing(Player player) {
        if (stopped || !isParticipant(player)) {
            return;
        }
        playerRecording(player).recordAnimate(tick, ANIMATE_SWING_ARM, 0.0f);
    }

    public void recordAttack(Player attacker, Entity target) {
        if (stopped || !isParticipant(attacker)) {
            return;
        }
        OptionalLong targetId = targetEntityId(target);
        if (targetId.isEmpty()) {
            return;
        }
        playerRecording(attacker).recordAttack(tick, targetId.getAsLong());
    }

    public void recordCustomEvent(JsonObject event) {
        if (stopped || event == null) {
            return;
        }
        customEvents.add(new LevelCustomEvent(tick, event.deepCopy()));
    }

    public RecordingStopResult stopAndSave() throws IOException {
        if (stopped) {
            return stopResult;
        }
        stopped = true;
        task.cancel();
        forceFinalSnapshots();
        Files.createDirectories(outputFile.getParent());
        List<String> recordedNames = recordedPlayerNames();
        RecordingMetadata finalMetadata = finalMetadata(recordedNames);
        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            packWriter.write(outputStream, Math.max(1, tick), players.values(), finalMetadata, List.copyOf(customEvents));
        }
        stopResult = new RecordingStopResult(sessionId, recordName, outputFile, Math.max(1, tick), recordedNames, finalMetadata);
        return stopResult;
    }

    public RecordingStatus status() {
        return new RecordingStatus(sessionId, recordName, outputFile, tick, players.size(), participants);
    }

    public RecordingStatus addParticipant(UUID participant, RecordingPlayerInfo playerInfo) {
        if (stopped) {
            throw new IllegalStateException("Recording session has already stopped: " + sessionId);
        }
        Objects.requireNonNull(participant, "participant");
        participants.add(participant);
        if (playerInfo != null) {
            playerIdentities.put(participant, playerInfo);
        }
        return status();
    }

    public void setMetadata(RecordingMetadata metadata) {
        this.metadata = metadata;
    }

    public void mergeMetadata(RecordingMetadata metadata) {
        if (metadata == null) {
            return;
        }
        RecordingMetadata base = this.metadata == null ? defaultMetadata(List.of()) : this.metadata;
        this.metadata = base.mergePreferNew(metadata);
    }

    public boolean containsParticipant(UUID uuid) {
        return participants.contains(uuid);
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

    public String sessionId() {
        return sessionId;
    }

    public String recordName() {
        return recordName;
    }

    public Set<UUID> participants() {
        return Set.copyOf(participants);
    }

    public RecordingMetadata metadata() {
        return metadata;
    }

    long mappedEntityIdForTest(Entity entity) {
        return nonPlayerEntityId(entity);
    }

    private void forceFinalSnapshots() {
        int finalTick = Math.max(1, tick);
        for (UUID participant : participants) {
            Player player = Bukkit.getPlayer(participant);
            if (player == null || !player.isOnline()) {
                continue;
            }
            PlayerRecording recording = players.get(player.getUniqueId());
            if (recording != null) {
                recording.forceRecord(finalTick, snapshot(player));
            }
        }
    }

    private RecordingMetadata finalMetadata(List<String> recordedNames) {
        RecordingMetadata effective = metadata == null ? defaultMetadata(defaultPlayerInfo(recordedNames)) : metadata;
        return effective.filterAndFillPlayers(recordedNames);
    }

    private RecordingMetadata defaultMetadata(List<RecordingPlayerInfo> players) {
        return RecordingMetadata.createDefault(recordName, worldName, players);
    }

    private List<RecordingPlayerInfo> defaultPlayerInfo(List<String> recordedNames) {
        List<RecordingPlayerInfo> result = new ArrayList<>();
        for (String name : recordedNames) {
            result.add(RecordingPlayerInfo.of(name, name));
        }
        return result;
    }

    private List<String> recordedPlayerNames() {
        List<String> names = new ArrayList<>();
        for (PlayerRecording player : players.values()) {
            names.add(player.playerName());
        }
        return List.copyOf(names);
    }

    private OptionalLong targetEntityId(Entity entity) {
        if (entity instanceof Player player) {
            if (!isParticipant(player)) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(playerRecording(player).originEntityId());
        }
        if (entity instanceof Chicken || entity instanceof Sheep) {
            return OptionalLong.of(nonPlayerEntityId(entity));
        }
        return OptionalLong.empty();
    }

    private long nonPlayerEntityId(Entity entity) {
        return entityIdMapper.id(entity.getUniqueId());
    }

    private boolean isParticipant(Player player) {
        return participants.contains(player.getUniqueId());
    }

    private PlayerRecording playerRecording(Player player) {
        return players.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerRecording(recordedPlayerName(player), GhostyBinaryWriter.stableEntityId(uuid), 0));
    }

    private PlayerSnapshot snapshot(Player player) {
        PlayerInventory inventory = player.getInventory();
        return new PlayerSnapshot(
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch(),
                displayPlayerName(player),
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

    private String recordedPlayerName(Player player) {
        RecordingPlayerInfo identity = playerIdentity(player);
        return isBlank(identity.name()) ? player.getName() : identity.name();
    }

    private String displayPlayerName(Player player) {
        RecordingPlayerInfo identity = playerIdentity(player);
        return isBlank(identity.aliasName()) ? player.getName() : identity.aliasName();
    }

    private RecordingPlayerInfo playerIdentity(Player player) {
        RecordingPlayerInfo identity = playerIdentities.get(player.getUniqueId());
        return identity == null ? RecordingPlayerInfo.of(player.getName(), player.getName()) : identity;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
