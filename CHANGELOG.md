# Changelog

## v0.1.0 - First Development Release

This is the first public development release of Monumenta Tools. It is intended for early testing and may contain bugs.

### Added

- Client-side Nightmare timer HUD for Monumenta Gallery maps.
- Default Marina Noir map scope with configurable support for Sanguine Halls, both Gallery maps, or any world.
- Automatic timer reset from detected non-player living entity deaths.
- Round-start chat detection and optional loot-room hiding behavior.
- Squad sync markers with configurable receive, broadcast, heartbeat, channel, and reset-window options.
- Draggable HUD editor, configurable scale, background, warning threshold, and debug line.
- Mod Menu and YetAnotherConfigLib configuration screen.
- `/monumentatools nightmare` client commands for editing, resetting, status checks, manual timer values, and toggling.

### Notes

- This release is still in development and may have bugs.
- Detection behavior is based on current map/world and message patterns and may need adjustment as Monumenta changes.
- The downloadable release jar is the remapped Fabric jar built for Minecraft `1.20.4`.
