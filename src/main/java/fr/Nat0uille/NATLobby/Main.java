package fr.Nat0uille.NATLobby;

import fr.Nat0uille.NATLobby.Commands.*;
import fr.Nat0uille.NATLobby.Listeners.*;
import fr.Nat0uille.NATLobby.TabCompleter.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info(getDescription().getName() + " Par " + String.join(", ", getDescription().getAuthors()));
        getLogger().info("Version: " + getDescription().getVersion());


        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldsListener(), this);

        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("nat-lobby").setExecutor(new NatLobbyCommand(this));
        getCommand("nat-lobby").setTabCompleter(new NatLobbyTabCompleter());
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " est désactivé.");
    }
}
