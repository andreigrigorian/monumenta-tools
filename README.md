# Monumenta Tools

Client-side Fabric tools for Monumenta.

This mod is currently in development. The `v0.1.0` release is an early first release and may contain bugs, rough edges, or behavior that changes in later versions.

## Current Features

- Nightmare timer HUD for Monumenta Gallery maps, focused on Marina Noir by default.
- Automatic timer reset from detected non-player entity deaths.
- Optional squad sync markers through Monumenta chat channels.
- Configurable HUD position, scale, background, timer values, warning threshold, and sync behavior.
- Mod Menu integration through YetAnotherConfigLib.
- Client commands under `/monumentatools nightmare` for edit, reset, status, set, and toggle actions.

## Requirements

- Minecraft `1.20.4`
- Fabric Loader `0.16.9` or newer
- Fabric API `0.97.2+1.20.4` or newer
- YetAnotherConfigLib `3.6.6+1.20.4-fabric` or newer
- Java `17` or newer

Mod Menu `9.2.0` or newer is optional but recommended for in-game configuration.

## Installation

1. Download the jar from the latest GitHub release.
2. Put the jar in your Minecraft `mods` folder.
3. Make sure the required Fabric dependencies are installed.
4. Launch the Fabric `1.20.4` profile.

## Development

Build the mod locally with:

```powershell
.\gradlew.bat build
```

The built jars are written to `build/libs/`.

## Status

This is not a finished or guaranteed-stable mod. Please treat early releases as test builds, keep backups of your config if you edit it heavily, and expect fixes or compatibility changes as the mod develops.
