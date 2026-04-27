package net.easecation.ghostypaperrecorder;

import net.easecation.ghostypaperrecorder.api.GhostyRecorderApi;
import net.easecation.ghostypaperrecorder.api.RecordingMetadata;
import net.easecation.ghostypaperrecorder.api.RecordingStartRequest;
import net.easecation.ghostypaperrecorder.api.RecordingStatus;
import net.easecation.ghostypaperrecorder.api.RecordingStopResult;
import net.easecation.ghostypaperrecorder.command.GhostyRecordCommand;
import net.easecation.ghostypaperrecorder.format.GhostyPackWriter;
import net.easecation.ghostypaperrecorder.recording.PaperItemMapper;
import net.easecation.ghostypaperrecorder.recording.RecordingSession;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class GhostyPaperRecorderPlugin extends JavaPlugin implements Listener, GhostyRecorderApi {
    public static final String MANUAL_SESSION_ID = "manual";

    private final PaperItemMapper itemMapper = new PaperItemMapper();
    private final GhostyPackWriter packWriter = new GhostyPackWriter();
    private final Map<String, RecordingSession> sessions = new LinkedHashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getServicesManager().register(GhostyRecorderApi.class, this, this, ServicePriority.Normal);
        GhostyRecordCommand command = new GhostyRecordCommand(this);
        PluginCommand pluginCommand = getCommand("ghostyrecord");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        getLogger().info("GhostyPaperRecorder enabled.");
    }

    @Override
    public void onDisable() {
        for (RecordingSession activeSession : List.copyOf(sessions.values())) {
            if (activeSession.isStopped()) {
                continue;
            }
            try {
                RecordingStopResult result = activeSession.stopAndSave();
                getLogger().info("Recording saved on disable: " + result.outputPath());
            } catch (IOException exception) {
                getLogger().severe("Failed to save recording on disable: " + exception.getMessage());
            }
        }
        sessions.clear();
        getServer().getServicesManager().unregister(GhostyRecorderApi.class, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        for (RecordingSession activeSession : List.copyOf(sessions.values())) {
            if (!activeSession.isStopped() && activeSession.containsParticipant(attacker.getUniqueId())) {
                activeSession.recordAttack(attacker, event.getEntity());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }
        Player player = event.getPlayer();
        for (RecordingSession activeSession : List.copyOf(sessions.values())) {
            if (!activeSession.isStopped() && activeSession.containsParticipant(player.getUniqueId())) {
                activeSession.recordSwing(player);
            }
        }
    }

    @Override
    public RecordingStatus startRecording(RecordingStartRequest request) throws IOException {
        requirePrimaryThread();
        if (request == null) {
            throw new IllegalArgumentException("Recording request cannot be null.");
        }
        String sessionId = safeId(request.sessionId(), MANUAL_SESSION_ID);
        String recordName = safeId(request.recordName(), sessionId);
        if (sessions.containsKey(sessionId)) {
            throw new IllegalStateException("A recording session already exists: " + sessionId);
        }
        Path output = outputPath(recordName, request.outputFileName());
        if (output.toFile().exists() || sessions.values().stream().anyMatch(session -> session.outputFile().equals(output))) {
            throw new IOException("Recording file already exists: " + output);
        }
        RecordingSession session = new RecordingSession(this, itemMapper, packWriter, sessionId, output, recordName,
                request.participants(), request.metadata());
        sessions.put(sessionId, session);
        return session.status();
    }

    @Override
    public RecordingStopResult stopRecording(String sessionId) throws IOException {
        requirePrimaryThread();
        RecordingSession session = sessionOrThrow(sessionId);
        RecordingStopResult result = session.stopAndSave();
        sessions.remove(session.sessionId());
        return result;
    }

    @Override
    public RecordingStatus status(String sessionId) {
        requirePrimaryThread();
        return sessionOrThrow(sessionId).status();
    }

    @Override
    public Collection<RecordingStatus> activeSessions() {
        requirePrimaryThread();
        List<RecordingStatus> result = new ArrayList<>();
        for (RecordingSession session : sessions.values()) {
            if (!session.isStopped()) {
                result.add(session.status());
            }
        }
        return List.copyOf(result);
    }

    @Override
    public boolean isRecording(String sessionId) {
        requirePrimaryThread();
        RecordingSession session = sessions.get(safeId(sessionId, MANUAL_SESSION_ID));
        return session != null && !session.isStopped();
    }

    @Override
    public void setMetadata(String sessionId, RecordingMetadata metadata) {
        requirePrimaryThread();
        sessionOrThrow(sessionId).setMetadata(metadata);
    }

    @Override
    public void mergeMetadata(String sessionId, RecordingMetadata metadata) {
        requirePrimaryThread();
        sessionOrThrow(sessionId).mergeMetadata(metadata);
    }

    public boolean isRecording() {
        requirePrimaryThread();
        return !activeSessions().isEmpty();
    }

    public RecordingSession session() {
        requirePrimaryThread();
        if (sessions.size() != 1) {
            return null;
        }
        return sessions.values().iterator().next();
    }

    public Path startRecording(String name) throws IOException {
        Set<UUID> participants = onlinePlayerIds();
        RecordingStatus status = startRecording(RecordingStartRequest.of(MANUAL_SESSION_ID, name, participants));
        return status.outputPath();
    }

    public Path stopRecording() throws IOException {
        RecordingSession session = onlyActiveSession();
        return stopRecording(session.sessionId()).outputPath();
    }

    public RecordingSession onlyActiveSession() {
        requirePrimaryThread();
        List<RecordingSession> active = sessions.values().stream().filter(session -> !session.isStopped()).toList();
        if (active.isEmpty()) {
            throw new IllegalStateException("No recording is running.");
        }
        if (active.size() > 1) {
            throw new IllegalStateException("Multiple recording sessions are running. Specify a session id.");
        }
        return active.getFirst();
    }

    public Set<UUID> onlinePlayerIds() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toUnmodifiableSet());
    }

    private RecordingSession sessionOrThrow(String sessionId) {
        String normalized = safeId(sessionId, MANUAL_SESSION_ID);
        RecordingSession session = sessions.get(normalized);
        if (session == null || session.isStopped()) {
            throw new IllegalArgumentException("Recording session does not exist: " + normalized);
        }
        return session;
    }

    private Path outputPath(String recordName, String outputFileName) {
        String baseName = outputFileName == null || outputFileName.trim().isEmpty() ? recordName : outputFileName.trim();
        if (baseName.endsWith(".tar.zstd")) {
            baseName = baseName.substring(0, baseName.length() - ".tar.zstd".length());
        }
        return getDataFolder().toPath().resolve("records").resolve(safeName(baseName) + ".tar.zstd");
    }

    private static void requirePrimaryThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("GhostyPaperRecorder API must be called on the Bukkit main thread.");
        }
    }

    private static String safeId(String value, String fallback) {
        String trimmed = value == null ? fallback : value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static String safeName(String name) {
        String trimmed = name == null ? "record" : name.trim();
        if (trimmed.isEmpty()) {
            trimmed = "record";
        }
        return trimmed.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
