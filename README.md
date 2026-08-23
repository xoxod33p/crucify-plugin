# CrucifyPlugin ✝️

An immersive roleplay punishment plugin for Minecraft Paper & Spigot servers (1.20+). When a player is crucified, an aligned timber cross is built behind them, an ArmorStand mannequin copies their exact skin and armor with realistic hanging posture, and their camera seamlessly locks into a third-person (TPS) spectator view.

---

## ✨ Features

- **Third-Person (TPS) Spectator View**: Automatically shifts the bound player into Spectator mode and locks their camera facing their cross.
- **Realistic Hanging Mannequin**: Mounts an ArmorStand with outstretched arms along the crossbeam, slumped drooping head, forward leaning body, dangling limp legs, and the player's skull skin and equipped armor.
- **Directional Alignment**: Automatically detects the player's facing direction (North, South, East, West) and erects the cross behind them.
- **Automatic Return to FPS**: Releasing a player instantly destroys the cross/mannequin and restores their original Survival/Adventure GameMode.
- **Disconnect & Reconnect Persistence**: If a bound player disconnects, their cross and mannequin remain in the world. Upon reconnecting, they are immediately placed back on their cross in third-person view.
- **Offline Player Support**: Staff can release offline players (/release <player>). The structure is destroyed immediately, and the player logs back in restored to normal first-person survival.
- **LuckPerms & OP Support**: Uses permission node crucify.admin (granted to OPs by default, or assignable to non-OP staff via LuckPerms).
- **Custom Sound Effects**: Includes wood placement and chain locking/unlocking audio feedback.

---

## 📜 Commands & Permissions

| Command | Usage | Description | Permission |
| :--- | :--- | :--- | :--- |
| **/crucify** | /crucify [player] | Pins target (or self if omitted) to a cross in 3rd-person view | crucify.admin (default: OP) |
| **/release** | /release [player] | Releases target (or self if omitted) back to normal 1st-person view | crucify.admin (default: OP) |

---

## 🔑 Permissions & LuckPerms Guide

Server operators (OPs) have access by default.

To grant access to a **non-OP player** using LuckPerms:
```text
/lp user <player> permission set crucify.admin true
```

To grant access to a **staff rank/group** (e.g. `moderator`):
```text
/lp group moderator permission set crucify.admin true
```

---

## 🚀 Installation

1. Download the latest **`crucify-plugin.jar`** from [Releases](https://github.com/xoxod33p/crucify-plugin/releases).
2. Place the `.jar` file into your server's `plugins/` folder.
3. Restart or reload your server.

---

## 🔨 Building from Source

### Prerequisites
- Java JDK 17 or higher
- Apache Maven 3.8+

### Build via Batch Script (Windows)
Double-click `build.bat` in the project root.

### Build via Maven CLI
```bash
mvn clean package -DskipTests
```
The output JAR will be created in `target/crucify-plugin.jar`.
