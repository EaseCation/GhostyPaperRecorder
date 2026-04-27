# GhostyPaperRecorder

GhostyPaperRecorder is a standalone Paper plugin that records minimal PvP-focused replay evidence and writes Ghosty-compatible `.tar.zstd` packs for the existing Nukkit playback stack.

## Scope

The recorder currently captures:

- Player position and rotation every tick when changed
- Name tag
- Main hand, offhand, and armor slots
- Basic Bedrock-compatible data flags: on fire, sneaking, sprinting, invisible, gliding, swimming
- Player ping
- Arm swing animations from `PlayerAnimationEvent`
- Successful hits from `EntityDamageByEntityEvent` against players, chickens, and sheep

It does not record blocks, projectiles, drops, sounds, particles, or world packet effects. Empty swings are represented as arm swing animation updates.

## Build

```bash
./gradlew build
```

The shaded plugin jar is generated at:

```text
build/libs/GhostyPaperRecorder-0.1.0.jar
```

The Paper API version is configured in `gradle.properties`:

```properties
paperApiVersion=1.21.4-R0.1-SNAPSHOT
```

## Commands

```text
/ghostyrecord start <recordName>
/ghostyrecord start <sessionId> <recordName>
/ghostyrecord stop [sessionId]
/ghostyrecord status [sessionId]
```

Permission:

```text
ghostypaperrecorder.command
```

The one-argument `start` command keeps the old behavior and creates the `manual` session with all currently online players as participants.

## API

Other plugins should use Bukkit services instead of casting the plugin main class. All API calls must run on the Bukkit main thread.

### Get the API

```java
import net.easecation.ghostypaperrecorder.api.GhostyRecorderApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<GhostyRecorderApi> provider =
        Bukkit.getServicesManager().getRegistration(GhostyRecorderApi.class);
if (provider == null) {
    return;
}
GhostyRecorderApi api = provider.getProvider();
```

### Start a recording

Each recording is identified by a unique `sessionId`. The recorder can run multiple sessions at the same time, and each session only records the player UUIDs passed in `participants`.

```java
import net.easecation.ghostypaperrecorder.api.RecordingMetadata;
import net.easecation.ghostypaperrecorder.api.RecordingPlayerInfo;
import net.easecation.ghostypaperrecorder.api.RecordingStartRequest;
import net.easecation.ghostypaperrecorder.api.RecordingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

Set<UUID> playerUuids = Set.of(playerA.getUniqueId(), playerB.getUniqueId());

RecordingMetadata metadata = new RecordingMetadata(
        "arena/duel_01",
        null,
        LocalDateTime.now().toString(),
        "Duel Arena 01",
        "duel",
        "Duel",
        "match-123",
        List.of(
                RecordingPlayerInfo.of(playerA.getName(), playerA.getName()),
                RecordingPlayerInfo.of(playerB.getName(), playerB.getName())
        ),
        "Match #123"
);

RecordingStatus status = api.startRecording(new RecordingStartRequest(
        "match-123",   // sessionId
        "match-123",   // recordName; used for default stage and output file name
        playerUuids,   // participants
        metadata,      // optional custom metadata
        null           // optional output file name override
));
```

The output path is available from `status.outputPath()`.

### Update metadata

Use `setMetadata` to replace the whole metadata object. Use `mergeMetadata` to update only non-null top-level fields. When merging `players`, entries are matched by `name`, and non-null fields from the newer value win.

```java
api.mergeMetadata("match-123", new RecordingMetadata(
        null,
        null,
        null,
        null,
        null,
        null,
        "match-123-finished",
        List.of(new RecordingPlayerInfo(
                playerA.getName(),
                playerA.getName(),
                null,
                null,
                null,
                null,
                null
        )),
        "Match #123 Finished"
));
```

### Stop a recording

```java
import net.easecation.ghostypaperrecorder.api.RecordingStopResult;

RecordingStopResult result = api.stopRecording("match-123");
Path output = result.outputPath();
RecordingMetadata finalMetadata = result.metadata();
List<String> recordedPlayers = result.playerNames();
```

Before saving, metadata `players` is filtered to the player records that were actually written. If metadata is missing a recorded player, the recorder fills a default `RecordingPlayerInfo(name, aliasName=name, ...)`.

### Status and lifecycle

```java
boolean running = api.isRecording("match-123");
RecordingStatus status = api.status("match-123");
Collection<RecordingStatus> all = api.activeSessions();
```

API methods throw `IllegalStateException` for invalid lifecycle operations, such as duplicate `sessionId` or async-thread access. They throw `IllegalArgumentException` when the target session does not exist.

### API types

- `RecordingStartRequest(sessionId, recordName, participants, metadata, outputFileName)`
- `RecordingStatus(sessionId, recordName, outputPath, tick, playerCount, participants)`
- `RecordingStopResult(sessionId, recordName, outputPath, lastTick, playerNames, metadata)`
- `RecordingMetadata(map, baseGameVersion, time, mapName, game, gameName, stage, players, showStage)`
- `RecordingPlayerInfo(name, aliasName, protocol, deviceModel, deviceOS, inputMode, hack)`
- `RecordingHackInfo(kbH, kbV, enableHitbox, hitboxV, hitboxH, enableSpeed, speed)`

## Output

Recordings are written to:

```text
plugins/GhostyPaperRecorder/records/<name>.tar.zstd
```

The pack contains:

- `level_record.ecrecl`
- `player/player_record_{index}_{name}.ecrecp`
- `metadata.json`

The level record is intentionally minimal and only keeps the Nukkit Ghosty playback timeline alive. Non-player hit targets are currently ID-mapped for attack payloads only; they are not emitted as `entity/*.ecrece` records.

## Compatibility Notes

- The plugin does not depend on Nukkit or CodeFunCore at runtime.
- Item mapping is deliberately limited to common PvP items. Unknown items are written as air.
- The output format targets Ghosty's `SkinlessPlayerRecord V3` and current `.tar.zstd` pack layout.
- Metadata fields align with CodeFunCore's `PlaybackMetadata`, including optional player hack information.
