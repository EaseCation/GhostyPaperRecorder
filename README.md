# GhostyPaperRecorder

GhostyPaperRecorder is a standalone Paper plugin that records a minimal PvP-focused replay and writes a Ghosty-compatible `.tar.zstd` pack for the existing Nukkit playback stack.

## Scope

The first version intentionally records only player-focused anti-cheat evidence:

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
/ghostyrecord start <name>
/ghostyrecord stop
/ghostyrecord status
```

Permission:

```text
ghostypaperrecorder.command
```

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
