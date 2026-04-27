package net.easecation.ghostypaperrecorder;

import net.easecation.ghostypaperrecorder.command.GhostyRecordCommand;
import net.easecation.ghostypaperrecorder.format.GhostyPackWriter;
import net.easecation.ghostypaperrecorder.recording.PaperItemMapper;
import net.easecation.ghostypaperrecorder.recording.RecordingSession;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;

public final class GhostyPaperRecorderPlugin extends JavaPlugin implements Listener {
    private final PaperItemMapper itemMapper = new PaperItemMapper();
    private final GhostyPackWriter packWriter = new GhostyPackWriter();
    private RecordingSession session;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
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
        if (session != null && !session.isStopped()) {
            try {
                Path output = session.stopAndSave();
                getLogger().info("Recording saved on disable: " + output);
            } catch (IOException exception) {
                getLogger().severe("Failed to save recording on disable: " + exception.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        RecordingSession activeSession = session;
        if (activeSession == null || activeSession.isStopped()) {
            return;
        }
        activeSession.recordAttack(attacker, event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }
        RecordingSession activeSession = session;
        if (activeSession == null || activeSession.isStopped()) {
            return;
        }
        activeSession.recordSwing(event.getPlayer());
    }

    public boolean isRecording() {
        return session != null && !session.isStopped();
    }

    public RecordingSession session() {
        return session;
    }

    public Path startRecording(String name) throws IOException {
        if (isRecording()) {
            throw new IllegalStateException("A recording is already running.");
        }
        Path output = getDataFolder().toPath().resolve("records").resolve(safeName(name) + ".tar.zstd");
        if (output.toFile().exists()) {
            throw new IOException("Recording file already exists: " + output);
        }
        session = new RecordingSession(this, itemMapper, packWriter, output, name);
        return output;
    }

    public Path stopRecording() throws IOException {
        if (!isRecording()) {
            throw new IllegalStateException("No recording is running.");
        }
        Path output = session.stopAndSave();
        session = null;
        return output;
    }

    private static String safeName(String name) {
        String trimmed = name == null ? "record" : name.trim();
        if (trimmed.isEmpty()) {
            trimmed = "record";
        }
        return trimmed.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
