package com.example.crucify;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrucifyCommand implements TabExecutor {

    private final CrucifyManager manager;

    public CrucifyCommand(CrucifyManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cYou do not have permission to do that.");
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage("§cUsage: /" + label + " <player>");
                return true;
            }
        } else if (args.length == 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                if (label.equalsIgnoreCase("release")) {
                    java.util.UUID offlineUUID = manager.findCrucifiedUUIDByName(args[0]);
                    if (offlineUUID != null) {
                        manager.releaseOffline(offlineUUID);
                        sender.sendMessage("§a" + args[0] + " (offline) has been released.");
                        return true;
                    }
                }
                sender.sendMessage("§cPlayer not found or offline.");
                return true;
            }
        } else {
            sender.sendMessage("§cUsage: /" + label + " [player]");
            return true;
        }

        boolean isSelf = sender instanceof Player && target.equals(sender);

        if (label.equalsIgnoreCase("crucify")) {
            if (manager.isCrucified(target.getUniqueId())) {
                sender.sendMessage("§c" + (isSelf ? "You are" : target.getName() + " is") + " already bound.");
                return true;
            }
            manager.crucify(target);
            if (!isSelf) {
                sender.sendMessage("§a" + target.getName() + " has been bound.");
            }
        } else if (label.equalsIgnoreCase("release")) {
            if (!manager.isCrucified(target.getUniqueId())) {
                sender.sendMessage("§c" + (isSelf ? "You are" : target.getName() + " is") + " not currently bound.");
                return true;
            }
            manager.release(target);
            if (!isSelf) {
                sender.sendMessage("§a" + target.getName() + " has been released.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String search = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
