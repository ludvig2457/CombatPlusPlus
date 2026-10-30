package com.ludvig.combatplus;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class CombatPlusPlusPlugin extends JavaPlugin {

    private CombatManager combatManager;
    private LegacyCombatManager legacyCombatManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.combatManager = new CombatManager(this);
        this.legacyCombatManager = new LegacyCombatManager(this);
        this.combatManager.start();
        this.legacyCombatManager.start();

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new LegacyCombatListener(this), this);
        register("combat", new CombatCommand(this));

        getLogger().info("Combat++ enabled with mode " + combatManager.getMode().id() + ".");
    }

    @Override
    public void onDisable() {
        if (legacyCombatManager != null) {
            legacyCombatManager.stop();
        }
        if (combatManager != null) {
            combatManager.stop();
        }
    }

    public void reloadAll() {
        reloadConfig();
        combatManager.load();
        getServer().getOnlinePlayers().forEach(combatManager::refreshModeMetadata);
        getLogger().info("Combat++ configuration reloaded.");
    }

    private void register(String name, Object executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command '" + name + "' is missing from plugin.yml");
            return;
        }
        command.setExecutor((CommandExecutor) executor);
        if (executor instanceof TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public LegacyCombatManager getLegacyCombatManager() {
        return legacyCombatManager;
    }
}
