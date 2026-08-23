package com.example.crucify;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CrucifyListener implements Listener {

    private final CrucifyPlugin plugin;
    private final CrucifyManager manager;

    public CrucifyListener(CrucifyPlugin plugin, CrucifyManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location pin = manager.getPinLocation(event.getPlayer().getUniqueId());
        if (pin == null) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location locked = to.clone();
            locked.setX(pin.getX());
            locked.setY(pin.getY());
            locked.setZ(pin.getZ());
            event.setTo(locked);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (manager.isCrucified(player.getUniqueId())) {
            manager.rebindOnReconnect(player);
        } else {
            org.bukkit.GameMode pending = manager.checkPendingOfflineRestore(player.getUniqueId());
            if (pending != null) {
                player.setGameMode(pending);
                player.setSpectatorTarget(null);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.JUMP);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (manager.isCrucified(event.getPlayer().getUniqueId())) {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
                event.setCancelled(true);
            }
        }
    }
}
