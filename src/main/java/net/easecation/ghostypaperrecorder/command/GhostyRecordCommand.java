package net.easecation.ghostypaperrecorder.command;

import net.easecation.ghostypaperrecorder.GhostyPaperRecorderPlugin;
import net.easecation.ghostypaperrecorder.recording.RecordingSession;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
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
            case "stop" -> stop(sender);
            case "status" -> status(sender);
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void start(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ghostyrecord start <name>");
            return;
        }
        try {
            Path output = plugin.startRecording(args[1]);
            sender.sendMessage(ChatColor.GREEN + "Recording started. Output: " + output);
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.RED + "Failed to start recording: " + exception.getMessage());
        }
    }

    private void stop(CommandSender sender) {
        try {
            Path output = plugin.stopRecording();
            sender.sendMessage(ChatColor.GREEN + "Recording saved: " + output);
        } catch (Exception exception) {
            sender.sendMessage(ChatColor.RED + "Failed to stop recording: " + exception.getMessage());
        }
    }

    private void status(CommandSender sender) {
        RecordingSession session = plugin.session();
        if (session == null || session.isStopped()) {
            sender.sendMessage(ChatColor.YELLOW + "No recording is running.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Recording: tick=" + session.tick() + ", players=" + session.playerCount() + ", output=" + session.outputFile());
    }

    private static void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <start <name>|stop|status>");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = List.of("start", "stop", "status");
            String prefix = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String option : options) {
                if (option.startsWith(prefix)) {
                    matches.add(option);
                }
            }
            return matches;
        }
        return List.of();
    }
}
