package net.easecation.ghostypaperrecorder.command;

import net.easecation.ghostypaperrecorder.GhostyPaperRecorderPlugin;
import net.easecation.ghostypaperrecorder.api.RecordingStartRequest;
import net.easecation.ghostypaperrecorder.api.RecordingStatus;
import net.easecation.ghostypaperrecorder.api.RecordingStopResult;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GhostyRecordCommand implements CommandExecutor, TabCompleter {
    private final GhostyPaperRecorderPlugin plugin;

    public GhostyRecordCommand(GhostyPaperRecorderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start" -> start(sender, args);
            case "stop" -> stop(sender, args);
            case "status" -> status(sender, args);
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void start(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ghostyrecord start <recordName> OR /ghostyrecord start <sessionId> <recordName>");
            return;
        }
        String sessionId = args.length >= 3 ? args[1] : GhostyPaperRecorderPlugin.MANUAL_SESSION_ID;
        String recordName = args.length >= 3 ? args[2] : args[1];
        try {
            RecordingStatus status = plugin.startRecording(RecordingStartRequest.of(sessionId, recordName, plugin.onlinePlayerIds()));
            sender.sendMessage(ChatColor.GREEN + "Recording started. Session: " + status.sessionId() + ", output: " + status.outputPath());
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.RED + "Failed to start recording: " + exception.getMessage());
        }
    }

    private void stop(CommandSender sender, String[] args) {
        try {
            RecordingStopResult result = args.length >= 2 ? plugin.stopRecording(args[1]) : plugin.stopRecording(plugin.onlyActiveSession().sessionId());
            sender.sendMessage(ChatColor.GREEN + "Recording saved. Session: " + result.sessionId() + ", output: " + result.outputPath());
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.RED + "Failed to stop recording: " + exception.getMessage());
        }
    }

    private void status(CommandSender sender, String[] args) {
        try {
            if (args.length >= 2) {
                sendStatus(sender, plugin.status(args[1]));
                return;
            }
            List<RecordingStatus> active = List.copyOf(plugin.activeSessions());
            if (active.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "No recording is running.");
                return;
            }
            for (RecordingStatus status : active) {
                sendStatus(sender, status);
            }
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.RED + "Failed to read recording status: " + exception.getMessage());
        }
    }

    private static void sendStatus(CommandSender sender, RecordingStatus status) {
        sender.sendMessage(ChatColor.GREEN + "Recording: session=" + status.sessionId()
                + ", tick=" + status.tick()
                + ", players=" + status.playerCount()
                + ", participants=" + status.participants().size()
                + ", output=" + status.outputPath());
    }

    private static void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <start <recordName>|start <sessionId> <recordName>|stop [sessionId]|status [sessionId]>");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return matches(List.of("start", "stop", "status"), args[0]);
        }
        if ((args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("status")) && args.length == 2) {
            List<String> sessionIds = plugin.activeSessions().stream().map(RecordingStatus::sessionId).toList();
            return matches(sessionIds, args[1]);
        }
        return List.of();
    }

    private static List<String> matches(List<String> options, String value) {
        String prefix = value.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(prefix)) {
                result.add(option);
            }
        }
        return result;
    }
}
