package fr.Nat0uille.NATLobby.Listeners;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PlayerListener implements Listener {

    private final Main main;
    MiniMessage mm = MiniMessage.miniMessage();

    public PlayerListener(Main main) {
        this.main = main;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        event.getPlayer().getInventory().clear();
        event.getPlayer().getInventory().setArmorContents(null);
        event.getPlayer().setHealth(20.0);
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setExp(0);
        event.getPlayer().setLevel(0);
        event.getPlayer().setGameMode(GameMode.ADVENTURE);

        Component joinMessage = mm.deserialize(
                main.getConfig().getString("onjoin.message")
                        .replace("{pseudo}", event.getPlayer().getName())
                        .replace("{joueur}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("{joueurmax}", String.valueOf(Bukkit.getMaxPlayers()))
        );
        event.getPlayer().sendMessage(joinMessage);

        ConfigurationSection tp = main.getConfig().getConfigurationSection("onjoin.teleport");
        if (tp != null) {
            String worldName = tp.getString("world");
            double x = tp.getDouble("x");
            double y = tp.getDouble("y");
            double z = tp.getDouble("z");
            float yaw = (float) tp.getDouble("yaw");
            float pitch = (float) tp.getDouble("pitch");

            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location loc = new Location(world, x, y, z, yaw, pitch);
                event.getPlayer().teleport(loc);
            }
        }

        String materialName = main.getConfig().getString("onjoin.item.material");
        String name = main.getConfig().getString("onjoin.item.name");
        List<String> lore = main.getConfig().getStringList("onjoin.item.lore");
        int slot = main.getConfig().getInt("onjoin.item.slot");

        Material material = Material.valueOf(materialName);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        meta.lore(lore.stream().map(mm::deserialize).toList());
        item.setItemMeta(meta);

        event.getPlayer().getInventory().setItem(slot, item);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Component quitMessage = mm.deserialize(
                main.getConfig().getString("onquit")
                        .replace("{pseudo}", event.getPlayer().getName())
                        .replace("{joueur}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("{joueurmax}", String.valueOf(Bukkit.getMaxPlayers()))
        );
        event.getPlayer().sendMessage(quitMessage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            int slot = main.getConfig().getInt("item.slot");
            if (event.getSlot() == slot) {
                event.setCancelled(true);
                String command = main.getConfig().getString("item.command");
                player.performCommand(command);
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}
