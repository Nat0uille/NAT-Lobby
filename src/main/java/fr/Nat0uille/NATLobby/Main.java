package fr.Nat0uille.NATLobby;

import fr.Nat0uille.NATLobby.Listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getLogger().info(getDescription().getName() + " Par " + String.join(", ", getDescription().getAuthors()));
        getLogger().info("Version: " + getDescription().getVersion());


        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " est désactivé.");
    }
}
