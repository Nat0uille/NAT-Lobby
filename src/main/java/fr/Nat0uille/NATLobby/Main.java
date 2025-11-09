package fr.Nat0uille.NATLobby;

import fr.Nat0uille.NATLobby.Commands.*;
import fr.Nat0uille.NATLobby.Listeners.*;
import fr.Nat0uille.NATLobby.TabCompleter.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin {

    private File statsFile;
    private FileConfiguration statsConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        createStatsFile();

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
        saveStats();
        getLogger().info(getDescription().getName() + " est désactivé.");
    }

    // Gestion de stats.yml pour persister l'état du fly
    public void createStatsFile() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        statsFile = new File(getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
                statsConfig = YamlConfiguration.loadConfiguration(statsFile);
                statsConfig.save(statsFile);
            } catch (IOException e) {
                getLogger().severe("Impossible de créer stats.yml");
                e.printStackTrace();
            }
        } else {
            statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        }
    }

    public FileConfiguration getStats() {
        if (statsConfig == null) createStatsFile();
        return statsConfig;
    }

    public void saveStats() {
        if (statsConfig == null || statsFile == null) return;
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            getLogger().severe("Impossible de sauvegarder stats.yml");
            e.printStackTrace();
        }
    }
}
