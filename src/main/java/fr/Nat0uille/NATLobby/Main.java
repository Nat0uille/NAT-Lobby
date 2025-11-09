package fr.Nat0uille.NATLobby;

import fr.Nat0uille.NATLobby.Commands.*;
import fr.Nat0uille.NATLobby.Listeners.*;
import fr.Nat0uille.NATLobby.TabCompleter.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private File statsFile;
    private FileConfiguration statsConfig;
    private final Set<UUID> buildPlayers = new HashSet<>();
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        createStatsFile();

        getLogger().info(getDescription().getName() + " Par " + String.join(", ", getDescription().getAuthors()));
        getLogger().info("Version: " + getDescription().getVersion());


        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(new WorldsListener(), this);

        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("build").setExecutor(new fr.Nat0uille.NATLobby.Commands.BuildCommand(this));
        getCommand("nat-lobby").setExecutor(new NatLobbyCommand(this));
        getCommand("nat-lobby").setTabCompleter(new NatLobbyTabCompleter());
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    @Override
    public void onDisable() {
        saveStats();
        getLogger().info(getDescription().getName() + " est désactivé.");
    }

    public void createStatsFile() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        statsFile = new File(getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                boolean created = statsFile.createNewFile();
                statsConfig = YamlConfiguration.loadConfiguration(statsFile);
                statsConfig.save(statsFile);
                if (!created) getLogger().warning("stats.yml already existed or couldn't be created");
            } catch (IOException e) {
                getLogger().severe("Impossible de créer stats.yml: " + e.getMessage());
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
            getLogger().severe("Impossible de sauvegarder stats.yml: " + e.getMessage());
         }
     }

    public boolean isBuild(java.util.UUID uuid) {
        return buildPlayers.contains(uuid);
    }

    public void setBuild(java.util.UUID uuid, boolean enabled) {
        if (enabled) buildPlayers.add(uuid);
        else buildPlayers.remove(uuid);
    }
}
