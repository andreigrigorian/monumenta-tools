# Monumenta Tools

Client-side Fabric tools for Monumenta.

Currently supports Minecraft `1.20.4` only.

## Current Features

- Mod Menu integration through YetAnotherConfigLib.
- Nightmare timer HUD for Monumenta Gallery maps, focused on Marina Noir by default.
  - Automatic timer reset from detected non-player entity deaths.
  - Optional squad sync markers through Monumenta chat channels.
  - Configurable HUD position, scale, and background.
  - Client commands under `/monumentatools nightmare` for edit, reset, status, set, and toggle actions.

## Requirements

- Fabric Loader `0.16.9` or newer
- Fabric API `0.97.2+1.20.4` or newer
- YetAnotherConfigLib `3.6.6+1.20.4-fabric` or newer

Mod Menu `9.2.0` or newer is optional but highly recommended for in-game configuration.

## Development

Build the mod locally with:

```powershell
.\gradlew.bat build
```

The built jars are written to `build/libs/`.

## Status

This is not a finished or fully stable mod yet. Please expect fixes or compatibility changes as the mod develops.
