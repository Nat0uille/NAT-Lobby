package fr.Nat0uille.NATLobby.Listeners;

import fr.Nat0uille.NATLobby.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

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
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            meta.lore(lore.stream().map(mm::deserialize).toList());

            NamespacedKey key = new NamespacedKey(JavaPlugin.getProvidingPlugin(getClass()), "menu");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }

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
        if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey menu = new NamespacedKey(JavaPlugin.getProvidingPlugin(getClass()), "menu");

        if (meta.getPersistentDataContainer().has(menu, PersistentDataType.BYTE)) {
            event.setCancelled(true);

            String command = main.getConfig().getString("onjoin.item.command");
            if (command != null && !command.isEmpty()) {
                player.performCommand(command);
            }
        }
    }
}
