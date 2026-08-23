package com.example.crucify;

import org.bukkit.plugin.java.JavaPlugin;

public class CrucifyPlugin extends JavaPlugin {

    private CrucifyManager crucifyManager;

    @Override
    public void onEnable() {
        this.crucifyManager = new CrucifyManager(this);

        CrucifyCommand crucifyCommand = new CrucifyCommand(crucifyManager);
        if (getCommand("crucify") != null) {
            getCommand("crucify").setExecutor(crucifyCommand);
            getCommand("crucify").setTabCompleter(crucifyCommand);
        }
        if (getCommand("release") != null) {
            getCommand("release").setExecutor(crucifyCommand);
            getCommand("release").setTabCompleter(crucifyCommand);
        }

        getServer().getPluginManager().registerEvents(
                new CrucifyListener(this, crucifyManager), this);

        getLogger().info("CrucifyPlugin enabled.");
    }

    @Override
    public void onDisable() {
        if (crucifyManager != null) {
            crucifyManager.releaseAll();
        }
        getLogger().info("CrucifyPlugin disabled.");
    }
}
