package com.example.crucify;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrucifyManager {

    private final CrucifyPlugin plugin;
    private final Map<UUID, CrucifyRecord> active = new HashMap<>();
    private final Map<UUID, GameMode> pendingOfflineRestores = new HashMap<>();

    public CrucifyManager(CrucifyPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isCrucified(UUID uuid) {
        return active.containsKey(uuid);
    }

    public UUID findCrucifiedUUIDByName(String name) {
        for (Map.Entry<UUID, CrucifyRecord> entry : active.entrySet()) {
            if (entry.getValue().playerName().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ArmorStand getCameraStand(UUID uuid) {
        CrucifyRecord record = active.get(uuid);
        return record == null ? null : record.cameraStand();
    }

    public GameMode checkPendingOfflineRestore(UUID uuid) {
        return pendingOfflineRestores.remove(uuid);
    }

    public void crucify(Player target) {
        if (isCrucified(target.getUniqueId())) {
            return;
        }

        Location base = target.getLocation().getBlock().getLocation();
        BlockFace facing = target.getFacing();
        if (facing != BlockFace.NORTH && facing != BlockFace.SOUTH && facing != BlockFace.EAST && facing != BlockFace.WEST) {
            facing = BlockFace.SOUTH;
        }
        BlockFace back = facing.getOppositeFace();

        Location foot = base.clone().add(back.getModX(), 0, back.getModZ());
        Location[] crossBlocks = buildCross(foot, facing);

        Map<Location, Material> originalBlocks = new HashMap<>();
        for (Location loc : crossBlocks) {
            Block block = loc.getBlock();
            originalBlocks.put(loc, block.getType());
            block.setType(Material.DARK_OAK_FENCE);
        }

        float yaw = switch (facing) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };

        double manX = foot.getX() + 0.5 + (facing.getModX() * 0.28);
        double manZ = foot.getZ() + 0.5 + (facing.getModZ() * 0.28);
        double manY = base.getY() + 0.65;
        Location mannequinLoc = new Location(target.getWorld(), manX, manY, manZ, yaw, 0f);

        ArmorStand mannequin = target.getWorld().spawn(mannequinLoc, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setCustomName(target.getName());
            stand.setCustomNameVisible(true);
            stand.setInvulnerable(true);
            stand.setPersistent(true);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                skull.setItemMeta(meta);
            }
            if (stand.getEquipment() != null) {
                stand.getEquipment().setHelmet(skull);
                stand.getEquipment().setChestplate(target.getInventory().getChestplate());
                stand.getEquipment().setLeggings(target.getInventory().getLeggings());
                stand.getEquipment().setBoots(target.getInventory().getBoots());
            }

            stand.setLeftArmPose(new EulerAngle(Math.toRadians(-10), 0, Math.toRadians(270)));
            stand.setRightArmPose(new EulerAngle(Math.toRadians(-10), 0, Math.toRadians(90)));
            stand.setHeadPose(new EulerAngle(Math.toRadians(28), Math.toRadians(5), Math.toRadians(8)));
            stand.setBodyPose(new EulerAngle(Math.toRadians(8), 0, 0));
            stand.setLeftLegPose(new EulerAngle(Math.toRadians(12), Math.toRadians(-4), Math.toRadians(3)));
            stand.setRightLegPose(new EulerAngle(Math.toRadians(14), Math.toRadians(4), Math.toRadians(-3)));
        });

        double camX = foot.getX() + 0.5 + (facing.getModX() * 3.3);
        double camZ = foot.getZ() + 0.5 + (facing.getModZ() * 3.3);
        double camY = base.getY() + 2.0;
        float camYaw = (yaw + 180f) % 360f;
        Location camLoc = new Location(target.getWorld(), camX, camY, camZ, camYaw, 6f);

        ArmorStand cameraStand = target.getWorld().spawn(camLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setPersistent(true);
            stand.setCollidable(false);
        });

        GameMode originalGameMode = target.getGameMode();

        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false));

        CrucifyRecord record = new CrucifyRecord(
                target.getName(),
                base.clone(),
                camLoc.clone(),
                originalBlocks,
                originalGameMode,
                cameraStand,
                mannequin
        );
        active.put(target.getUniqueId(), record);

        target.getWorld().playSound(mannequinLoc, org.bukkit.Sound.BLOCK_WOOD_PLACE, 1.0f, 0.8f);
        target.getWorld().playSound(mannequinLoc, org.bukkit.Sound.BLOCK_CHAIN_PLACE, 1.0f, 0.9f);

        target.setGameMode(GameMode.SPECTATOR);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (isCrucified(target.getUniqueId()) && cameraStand.isValid()) {
                target.setSpectatorTarget(cameraStand);
            }
        });

        target.sendMessage("§cYou have been bound in third-person view.");
    }

    public void rebindOnReconnect(Player target) {
        CrucifyRecord record = active.get(target.getUniqueId());
        if (record == null) {
            return;
        }

        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false));

        target.setGameMode(GameMode.SPECTATOR);

        ArmorStand cam = record.cameraStand();
        if (cam == null || !cam.isValid()) {
            ArmorStand newCam = target.getWorld().spawn(record.camLocation(), ArmorStand.class, stand -> {
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setMarker(true);
                stand.setInvulnerable(true);
                stand.setPersistent(true);
                stand.setCollidable(false);
            });
            CrucifyRecord updated = new CrucifyRecord(
                    record.playerName(),
                    record.anchor(),
                    record.camLocation(),
                    record.originalBlocks(),
                    record.originalGameMode(),
                    newCam,
                    record.mannequin()
            );
            active.put(target.getUniqueId(), updated);
            cam = newCam;
        }

        ArmorStand finalCam = cam;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (isCrucified(target.getUniqueId()) && finalCam != null && finalCam.isValid()) {
                target.setSpectatorTarget(finalCam);
                target.sendMessage("§cYou are still bound to the cross.");
            }
        }, 2L);
    }

    public void release(Player target) {
        CrucifyRecord record = active.remove(target.getUniqueId());
        if (record == null) {
            return;
        }

        for (Map.Entry<Location, Material> entry : record.originalBlocks().entrySet()) {
            entry.getKey().getBlock().setType(entry.getValue());
        }

        GameMode restoreMode = record.originalGameMode();
        if (restoreMode == null || restoreMode == GameMode.SPECTATOR) {
            restoreMode = GameMode.SURVIVAL;
        }

        target.setSpectatorTarget(null);
        target.setGameMode(restoreMode);
        target.teleport(record.anchor().clone().add(0.5, 0, 0.5));

        if (record.cameraStand() != null && record.cameraStand().isValid()) {
            record.cameraStand().remove();
        }
        if (record.mannequin() != null && record.mannequin().isValid()) {
            record.mannequin().remove();
        }

        target.removePotionEffect(PotionEffectType.JUMP);
        target.removePotionEffect(PotionEffectType.SLOW);

        target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.BLOCK_CHAIN_BREAK, 1.0f, 1.0f);
        target.sendMessage("§aYou have been released.");
    }

    public void releaseOffline(UUID uuid) {
        CrucifyRecord record = active.remove(uuid);
        if (record == null) {
            return;
        }

        for (Map.Entry<Location, Material> entry : record.originalBlocks().entrySet()) {
            entry.getKey().getBlock().setType(entry.getValue());
        }

        if (record.cameraStand() != null && record.cameraStand().isValid()) {
            record.cameraStand().remove();
        }
        if (record.mannequin() != null && record.mannequin().isValid()) {
            record.mannequin().remove();
        }

        pendingOfflineRestores.put(uuid, record.originalGameMode() != null ? record.originalGameMode() : GameMode.SURVIVAL);
    }

    public Location getPinLocation(UUID uuid) {
        CrucifyRecord record = active.get(uuid);
        return record == null ? null : record.anchor().clone().add(0.5, 0, 0.5);
    }

    public void releaseAll() {
        for (UUID uuid : Map.copyOf(active).keySet()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) {
                release(p);
            } else {
                CrucifyRecord record = active.remove(uuid);
                if (record != null) {
                    if (record.cameraStand() != null && record.cameraStand().isValid()) {
                        record.cameraStand().remove();
                    }
                    if (record.mannequin() != null && record.mannequin().isValid()) {
                        record.mannequin().remove();
                    }
                    for (Map.Entry<Location, Material> entry : record.originalBlocks().entrySet()) {
                        entry.getKey().getBlock().setType(entry.getValue());
                    }
                }
            }
        }
    }

    private Location[] buildCross(Location foot, BlockFace facing) {
        int rightX = -facing.getModZ();
        int rightZ = facing.getModX();

        return new Location[]{
                foot.clone(),
                foot.clone().add(0, 1, 0),
                foot.clone().add(0, 2, 0),
                foot.clone().add(0, 3, 0),
                foot.clone().add(rightX, 2, rightZ),
                foot.clone().add(-rightX, 2, -rightZ)
        };
    }

    private record CrucifyRecord(
            String playerName,
            Location anchor,
            Location camLocation,
            Map<Location, Material> originalBlocks,
            GameMode originalGameMode,
            ArmorStand cameraStand,
            ArmorStand mannequin
    ) {}
}
