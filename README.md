# CrucifyPlugin

A Minecraft plugin that binds a player to a cross structure with a third-person (TPS) camera view.

## Features

- Third-person (TPS) spectator camera view when bound
- Posed hanging mannequin with player skin and armor
- Directional cross building based on player facing direction
- Reconnection persistence (players stay bound across disconnects)
- Automatic return to first-person (FPS) view on release
- Sound effects on crucify and release
- LuckPerms and operator (OP) permission support

## Commands

| Command | Usage | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/crucify` | `/crucify [player]` | Crucifies target (or self if omitted) | `crucify.admin` (default: OP) |
| `/release` | `/release [player]` | Releases target (or self if omitted) | `crucify.admin` (default: OP) |

## Permissions

Permission node: `crucify.admin` (granted to OPs by default).

To give permission using LuckPerms:
```text
/lp user <player> permission set crucify.admin true
```

## Installation

1. Download `crucify-plugin.jar` from [Releases](https://github.com/xoxod33p/crucify-plugin/releases).
2. Put the `.jar` into your server's `plugins/` folder.
3. Restart your server.

## Building

Run `build.bat` on Windows or run:
```bash
mvn clean package -DskipTests
```
The JAR file will be in `target/crucify-plugin.jar`.
